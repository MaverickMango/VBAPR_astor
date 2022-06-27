package fr.inria.astor.core.solutionsearch.population;

import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.RandomManager;
import org.apache.log4j.Logger;

import java.util.*;

public class DiffBasedFitnessPopulationController implements PopulationController {

    private Logger log = Logger.getLogger(Thread.currentThread().getName());

    public FitnessComparator comparator = new FitnessComparator();
    private int _generation;

    @Override
    public List<ProgramVariant> selectProgramVariantsForNextGeneration(List<ProgramVariant> parentVariants, List<ProgramVariant> childVariants, int populationSize, ProgramVariantFactory variantFactory, ProgramVariant original, int generation) {

        _generation = generation;
        List<ProgramVariant> solutionsFromGeneration = new ArrayList<ProgramVariant>();

        List<ProgramVariant> newPopulation = new ArrayList<>(childVariants);

        if (ConfigurationProperties.getProperty("reintroduce").contains(PopulationConformation.PARENTS.toString())) {
            newPopulation.addAll(parentVariants);
        }

        Collections.sort(newPopulation, comparator);

        for (ProgramVariant programVariant : newPopulation) {
            if (programVariant.isSolution()) {
                solutionsFromGeneration.add(programVariant);
            }
        }

        if (!ConfigurationProperties.getProperty("reintroduce").contains(PopulationConformation.SOLUTIONS.toString())) {
            newPopulation.removeAll(solutionsFromGeneration);
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
            nextVariants.addAll(weightRandomChoose(newPopulation, hadbeenModi, populationSize - nextVariants.size()));

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

    public List<ProgramVariant> weightRandomChoose(List<ProgramVariant> remaining, Set<Integer> hadbennModi, int targetSize) {//
        assert targetSize >= 0;
        if (targetSize == 0) {
            return null;
        }
        List<ProgramVariant> candidates = new ArrayList<>(remaining);
        List<ProgramVariant> found = new ArrayList<>();
        for (int i = 0; found.size() < targetSize && !remaining.isEmpty();) {//would it be dead loop? if the threshold will be always bigger than the prob?
            double sum = 0.0;
            for (ProgramVariant pv :remaining) {
                double fitness = pv.getFitness();
                sum += 1.0 + fitness;//add 1.0 to guarantee the solution not always be chosen.
            }
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

    class FitnessComparator implements Comparator<ProgramVariant> {

        @Override
        public int compare(ProgramVariant o1, ProgramVariant o2) {
            int fitness = Double.compare(o1.getFitness(), o2.getFitness());
            if (fitness != 0)
                return fitness;
            if (!o1.getOperations().isEmpty() && !o2.getOperations().isEmpty()) {
                int res = Integer.compare(o1.getOperations().size(), o2.getOperations().size());
                if (res != 0)
                    return res;
                if (o1.getOperations().get(_generation) != null)
                    return -1;
                if (o2.getOperations().get(_generation) != null)
                    return 1;
            }
            if (!o1.getOperations().isEmpty())
                return -1;
            if (!o2.getOperations().isEmpty())
                return 1;
            return Integer.compare(o2.getId(), o1.getId());
        }

    }
}
