package fr.inria.astor.core.entities;

import fr.inria.astor.core.setup.RandomManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WeightElementWithTwo<T>{

    public T element = null;
    public double weight = 0;
    public double accum = 0;
    public double simWeight = 0;
    public double simAccum = 0;

    public WeightElementWithTwo(Object element, double weight, double simWeight) {
        this.element = (T) element;
        this.weight = weight;
        this.simWeight = simWeight;
    }

    @Override
    public String toString() {
        return "WE [element=" + element + ", weight=" + weight + ", sim=" + simWeight +  ", accum " + accum + "]";
    }

    private static void sortByWeight(List<WeightElementWithTwo<?>> we, boolean useSim) {
        Collections.sort(we, new Compwt(useSim));
    }

    public static double getProb(WeightElementWithTwo we) {
        return we.accum == 0 ? we.simAccum : we.accum;
    }

    public static void feedAccumulativeWithWeight(List<WeightElementWithTwo<?>> we) {
        WeightElementWithTwo.sortByWeight(we, false);
        if (we.isEmpty())
            return;
        // Calculate Accumulative
        we.get(0).accum = we.get(0).weight;
        //
        for (int i = 1; i < we.size(); i++) {
            WeightElementWithTwo<?> e = we.get(i);
            if (e.weight == -1d)
                break;
            WeightElementWithTwo<?> elast = we.get(i - 1);

            e.accum = (elast.accum + e.weight);
        }
    }

    public static void feedAccumulativeWithSim(List<WeightElementWithTwo<?>> we) {
        WeightElementWithTwo.sortByWeight(we, true);
        if (we.isEmpty())
            return;
        // Calculate Accumulative
        we.get(0).simAccum = we.get(0).simWeight;
        //
        for (int i = 1; i < we.size(); i++) {
            WeightElementWithTwo<?> e = we.get(i);
            WeightElementWithTwo<?> elast = we.get(i - 1);

            e.simAccum = (elast.simAccum + e.simWeight);
        }
    }

    public static WeightElementWithTwo<?> selectElementWeightBalancedWithTwo(List<WeightElementWithTwo<?>> we) {
        WeightElementWithTwo<?> selected = null;
        double d = RandomManager.nextDouble();

        for (int i = 0; (selected == null && i < we.size()); i++) {
            if (d <= getProb(we.get(i))) {
                selected = we.get(i);
            }
        }
        if (selected == null) {
            return null;
        }
        return selected;
    }

    static class Compwt implements Comparator<WeightElementWithTwo<?>> {
        boolean useSim = false;
        public Compwt(boolean useSim) {
            this.useSim = useSim;
        }

        @Override
        public int compare(WeightElementWithTwo<?> o1, WeightElementWithTwo<?> o2) {
            // Decreasing
            if (!useSim)
                return Double.compare(o2.weight, o1.weight);
            else
                return Double.compare(o2.simWeight, o1.simWeight);
        }

    }
}
