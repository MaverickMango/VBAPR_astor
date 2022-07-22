package fr.inria.astor.core.solutionsearch.population;

import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.WeightElement;
import fr.inria.astor.core.entities.WeightElementWithTwo;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.RandomManager;
import org.apache.log4j.Logger;

import java.util.*;

public class DiffBasedFitnessPopulationController implements PopulationController {

    private Logger log = Logger.getLogger(Thread.currentThread().getName());

    public FitnessComparator comparator = new FitnessComparator();

    @Override
    public List<ProgramVariant> selectProgramVariantsForNextGeneration(List<ProgramVariant> parentVariants,
                List<ProgramVariant> childVariants, int populationSize, ProgramVariantFactory variantFactory,
                    ProgramVariant original, int generation) {

        List<ProgramVariant> solutionsFromGeneration = new ArrayList<ProgramVariant>();

        List<ProgramVariant> newPopulation = new ArrayList<>(childVariants);

        if (ConfigurationProperties.getProperty("reintroduce").contains(PopulationConformation.PARENTS.toString())) {
            newPopulation.addAll(parentVariants);
        }

        if (!ConfigurationProperties.getProperty("reintroduce").contains(PopulationConformation.SOLUTIONS.toString())) {
            newPopulation.removeAll(solutionsFromGeneration);
        }

        for (ProgramVariant programVariant : newPopulation) {
            if (programVariant.isSolution()) {
                solutionsFromGeneration.add(programVariant);
            }
        }


        int totalInstances = newPopulation.size();
        List<ProgramVariant> nextVariants = new ArrayList<>();
        if (totalInstances > populationSize) {
            Set<Integer> hadbeenModi = new HashSet<>();
//            List<ProgramVariant> remaining = new ArrayList<>(newPopulation);
//            //1. for all solutions, choose some of them
//            for (int i = 0; i < newPopulation.size(); i ++) {
//                ProgramVariant pv = newPopulation.get(i);
//                if (pv.isSolution()) {
//                    nextVariants.add(pv);
//                    remaining.remove(pv);
//                }
//            }
//            nextVariants = weightRandomChooseSolutions(nextVariants, hadbeenModi);

//            //2. for those who have operations
//            newPopulation = new ArrayList<>(remaining);//without solutions
//            nextVariants.addAll(weightRandomChoose(newPopulation));

//            while (nextVariants.size() < populationSize) {}

            //3. for all left
            if (ConfigurationProperties.getPropertyBool("addsimilaritycomparasion")) {
                nextVariants.addAll(rouletteWheelSelection(newPopulation, populationSize - nextVariants.size()));
            } else {
                try {
                    newPopulation.sort(comparator);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                nextVariants.addAll(weightRandomChoose(newPopulation, hadbeenModi, populationSize - nextVariants.size()));
            }
        } else {
            nextVariants.addAll(newPopulation);
        }

        if (ConfigurationProperties.getProperty("reintroduce").contains(PopulationConformation.ORIGINAL.toString())) {
            if (!nextVariants.isEmpty()) {
                nextVariants.remove(nextVariants.size() - 1);
            }
        }

        while (nextVariants.size() < populationSize) {
            ProgramVariant originalVariant = variantFactory.createProgramVariantFromAnother(original, generation);
            originalVariant.getOperations().clear();
            originalVariant.setParent(null);
            nextVariants.add(originalVariant);
        }

        return nextVariants;
    }

    public List<ProgramVariant> weightRandomChoose(List<ProgramVariant> wholepvs) {
        List<ProgramVariant> remaining = new ArrayList<>(wholepvs);
        List<ProgramVariant> found = new ArrayList<>();
        double sum = 0.0;
        for (ProgramVariant pv :remaining) {
            double fitness = pv.getFitness();
            sum += 1.0 + fitness;//add 1.0 to guarantee the solution not always be chosen.
        }
        for (int i = 0; i < wholepvs.size() && Double.compare(sum, 0.0) > 0; i ++) {
            ProgramVariant pv = wholepvs.get(i);
            double prob = 1.0 - (1.0 + pv.getFitness()) / sum;
            double threshold = RandomManager.nextDouble();
            if (prob >= threshold) {
                found.add(pv);
                remaining.remove(pv);
            }
        }
        return found;
    }


    public List<ProgramVariant> weightRandomChooseSolutions(List<ProgramVariant> wholepvs, Set<Integer> hadbeenModi) {
        if (wholepvs.size() <= 1) {
            return wholepvs;
        }
        List<ProgramVariant> remaining = new ArrayList<>(wholepvs);
        List<ProgramVariant> found = new ArrayList<>();
        List<String> patchdiffs = new ArrayList<>();
        double sum = 0.0;
        for (ProgramVariant pv :remaining) {
            double fitness = pv.getFitness();
            sum += 1.0 + fitness;//add 1.0 to guarantee the solution not always be chosen.
        }
        for (int i = 0; i < wholepvs.size() && Double.compare(sum, 0.0) > 0; i ++) {
            ProgramVariant pv = wholepvs.get(i);
            double prob = 1.0 - (1.0 + pv.getFitness()) / sum;
            double threshold = RandomManager.nextDouble();
            if (prob >= threshold) {
                String patchdiff = pv.getPatchDiff().getFormattedDiff();
                if (!patchdiffs.contains(patchdiff)) {
                    patchdiffs.add(patchdiff);
                    found.add(pv);
                    remaining.remove(pv);
                    Map<Integer, List<OperatorInstance>> operatorInstances = pv.getOperations();
                    for (Integer gen :operatorInstances.keySet()) {
                        for (OperatorInstance oi: operatorInstances.get(gen)) {
                            int id = oi.getModificationPoint().identified;
                            hadbeenModi.add(id);
                        }
                    }
                }
            }
        }
        return found;
    }

    public List<ProgramVariant> rouletteWheelSelection(List<ProgramVariant> remaining, int targetSize) {
        assert targetSize >= 0;
        if (targetSize == 0) {
            return null;
        }
        List<ProgramVariant> found = new ArrayList<>();
        for (int i = 0; found.size() < targetSize && !remaining.isEmpty();) {//this process might cost much.
            ProgramVariant pv = weightedSelectWithNormalize(remaining, found);
            if (pv != null) {
                if (pv.getModificationPoints().isEmpty()) {
                    remaining.remove(pv);
                    continue;
                }
                found.add(pv);
                remaining.remove(pv);
            }
        }
        found.sort(new Comparator<ProgramVariant>() {
            @Override
            public int compare(ProgramVariant p1, ProgramVariant p2) {
                return Double.compare(p1.getFitness(), p2.getFitness());
            }
        });
        return found;
    }

    public List<ProgramVariant> weightRandomChoose(List<ProgramVariant> remaining, Set<Integer> hadbennModi, int targetSize) {//
        assert targetSize >= 0;
        if (targetSize == 0) {
            return null;
        }
        List<ProgramVariant> candidates = new ArrayList<>(remaining);
        List<ProgramVariant> found = new ArrayList<>();
        for (int i = 0; found.size() < targetSize && !remaining.isEmpty();) {
            double sum = getSumOfProgramVariant(remaining);
            if (Double.compare(sum, 0.0) != 0) {
                ProgramVariant pv = candidates.get(i);
                double prob = 1.0 - (1.0 + pv.getFitness()) / sum;//
                double threshold = RandomManager.nextDouble();
                if (prob >= threshold) {
                    boolean flag = pv.getOperations().isEmpty();
                    if (!flag) {
                        Map<Integer, List<OperatorInstance>> operatorInstances = pv.getOperations();
                        for (Integer gen :operatorInstances.keySet()) {
                            for (int j = operatorInstances.get(gen).size() - 1; j >= 0 && !flag; j --) {
                                OperatorInstance oi = operatorInstances.get(gen).get(j);
                                int id = oi.getModificationPoint().identified;
                                if (!hadbennModi.contains(id)) {
                                    hadbennModi.add(id);
                                    flag = true;
                                }
                            }
                        }
                    }
                    if (flag) {
                        found.add(pv);
                        remaining.remove(pv);
                    }

                }
                if (++i >= candidates.size())
                    i = 0;
            } else {
                break;
            }
        }
        return found;
    }

    private double getSumOfProgramVariant(List<ProgramVariant> remaining) {
        double sum = 0d;
        for (ProgramVariant pv :remaining) {
            double weight = pv.getFitness();
            sum += 1.0 + weight;//add 1.0 to guarantee the solution not always be chosen.
        }
        return sum;
    }

    private ProgramVariant weightedSelectWithNormalize(List<ProgramVariant> remaining, List<ProgramVariant> found) {
        List<WeightElement<?>> wes = new ArrayList<>();
        Map<String, Double> diversity = new HashMap<>();
        double sum1 = 0d, sum2 = 0d;
        double max1 = 0, max2 = 0;
        double min1 = Double.MAX_VALUE, min2 = Double.MAX_VALUE;
        for (ProgramVariant pv :remaining) {
            double score1 = pv.getFitness(), score2 = pv.getSimilarity();
            if (score1 != Double.MAX_VALUE) {
                max1 = Math.max(max1, score1);
                min1 = Math.min(min1, score1);
            }
            max2 = Math.max(max2, score2);
            min2 = Math.min(min2, score2);
            String opType = pv.getLastOp();
            if (opType != null && !diversity.containsKey(opType)) {
                diversity.put(opType, 1d);
            }
        }
        for (ProgramVariant pv :found) {
            if (diversity.containsKey(pv.getLastOp())) {
                diversity.put(pv.getLastOp(), diversity.get(pv.getLastOp()) + 1);
            }
        }
        diversity.put(null, 1d);
        double min3 = Collections.min(diversity.values()), max3 = Collections.max(diversity.values());
        for (ProgramVariant pv :remaining) {
            double score1 = pv.getFitness(), score2 = pv.getSimilarity();
            double score3 = diversity.get(pv.getLastOp());
            double nor1 = 0d, nor2 = 0d, nor3 = 0d;
            WeightElement<?> we = null;
            nor2 = (score2 - min2) / (max2 - min2 == 0 ? 1 : max2 - min2) + 1;
            nor3 = 2 - (score3 - min3) / (max3 - min3 == 0 ? 1 : max3 - min3);
            if (score1 != Double.MAX_VALUE) {
                nor1 = 2 - (score1 - min1) / (max1 - min1 == 0 ? 1 : max1 - min1);
                we= new WeightElement<>(pv, nor1 + nor2 + nor3);
                sum1 += nor1;
            } else {
                we = new WeightElement<>(pv, nor2 + nor3);
            }
            sum1 += nor2 + nor3;
            wes.add(we);
        }
        WeightElement<?> selected = null;
        if (sum1 != 0) {
            for (WeightElement<?> we : wes) {
                we.weight = we.weight / sum1;
            }
            WeightElement.feedAccumulative(wes);

            selected = WeightElement.selectElementWeightBalanced(wes);
        }
        return selected == null ? null : (ProgramVariant) selected.element;
    }

    class FitnessComparator implements Comparator<ProgramVariant> {

        @Override
        public int compare(ProgramVariant o1, ProgramVariant o2) {
            int fitness = Double.compare(o1.getFitness(), o2.getFitness());
            if (fitness != 0)
                return fitness;
            if (!o1.getOperations().isEmpty() || !o2.getOperations().isEmpty()) {
//                int res1 = Integer.compare(o1.getOperations().size(), o2.getOperations().size());
//                if (res1 != 0)
//                    return res1;
                return Integer.compare(o1.getOperationsSize(), o2.getOperationsSize());
            }
            return Integer.compare(o2.getId(), o1.getId());
        }

    }
}
