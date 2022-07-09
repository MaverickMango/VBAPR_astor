package fr.inria.astor.core.solutionsearch.navigation;

import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.setup.RandomManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ForceOrderSuspiciousNavitation implements SuspiciousNavigationStrategy{
    @Override
    public List<ModificationPoint> getSortedModificationPointsList(List<ModificationPoint> modificationPoints) {
        return getSortedModificationPointsList(modificationPoints, -1);
    }

    public List<ModificationPoint> getSortedModificationPointsList(List<ModificationPoint> modificationPoints, int mpsidx) {
        List<ModificationPoint> remaining = new ArrayList<>(modificationPoints);
        if (mpsidx == -1) {
            Collections.shuffle(remaining);
            return remaining;
        }
        List<ModificationPoint> sorted = new ArrayList<>();
        sorted.add(remaining.remove(mpsidx>=remaining.size()? RandomManager.nextInt(remaining.size()): mpsidx));
        Collections.shuffle(remaining);
        sorted.addAll(remaining);
        return sorted;
    }
}
