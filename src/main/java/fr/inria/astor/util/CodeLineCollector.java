package fr.inria.astor.util;

import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.chain.CtQuery;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CodeLineCollector  {
    public static Set<CtClass> clazzElements = new HashSet<>();
    public static Set<CtVariableAccess> varElements = new HashSet<>();

    public static void getVarsInscope() {
        for (CtClass clz :clazzElements) {
            varElements.addAll(clz.getElements(new TypeFilter<>(CtFieldAccess.class)));
            List<CtMethod> methods = clz.getElements(new TypeFilter<>(CtMethod.class));
            for (CtMethod mth :methods) {
                int start = mth.getPosition().getLine();
                int end = mth.getPosition().getEndLine();
                if (findMethod(start, end)) {
                    varElements.addAll(mth.getElements(new TypeFilter<>(CtVariableAccess.class)));
                }
            }
        }
        assert varElements.size() != 0;
    }

    public static boolean findMethod(int start, int end) {
        for (GroundTruth gt :ReadGT.GTs) {
            if (gt.isOnlyOneLine()) {
                int lineNumber = gt.getLinenumber();
                return isBetweenLine(lineNumber, start, end);
            } else {
                int s = gt.getStartLineNumber();
                int e = gt.getEndLineNumber();
                return start < s && e < end;
            }
        }
        return false;
    }

    public static void getClazzElements() {
        Factory spoon = MutationSupporter.getFactory();
        ReadGT.getGTs(ReadGT.getInfos());
        for (GroundTruth gt :ReadGT.GTs) {
            String filePath = gt.getLocation();
            CtClass clazz = (CtClass) spoon.Type().get(filePath);
            clazzElements.add(clazz);
        }
        assert clazzElements.size() != 0;
    }

    public static CtClass isGTClass(String qualifiedName) {
        for (CtClass clz :clazzElements) {
            String name = clz.getQualifiedName();
            if (qualifiedName.equals(name)) {
                return clz;
            }
        }
        return null;
    }

    public static boolean isLapping(int s1, int e1, int s2, int e2) {
        return (s1 <= s2 || e1 >= s2) && (s1 >= s2 || e2 >= s1);
    }

    public static boolean isBetweenLine(int compareStart, int compareEnd) {
        for (GroundTruth gt :ReadGT.GTs) {
            if (gt.isOnlyOneLine()) {
                int lineNumber = gt.getLinenumber();
                return isBetweenLine(lineNumber, compareStart, compareEnd);
            } else {
                int start = gt.getStartLineNumber();
                int end = gt.getEndLineNumber();
                if (compareStart == compareEnd) {
                    return isBetweenLine(compareStart, start, end);
                } else {
                    return isLapping(compareStart, compareEnd, start, end);
                }
            }
        }
        return false;
    }
    public static boolean isBetweenLine(int lineNumber, int compareStart, int compareEnd) {
        return compareStart <= lineNumber && compareEnd >= lineNumber;
    }

    public static boolean isInScope(int lineNumber) {
        if (ReadGT.GTs.size() == 0)
            return true;
        boolean flag = false;
        for (GroundTruth gt :ReadGT.GTs) {
            if (gt.isOnlyOneLine()) {
                flag = lineNumber == gt.getLinenumber();
            } else {
                int start = gt.getStartLineNumber();
                int end = gt.getEndLineNumber();
                flag = isBetweenLine(lineNumber, start, end);
            }
            if (flag) {
                break;
            }
        }
        return flag;
    }

    public static void parseClass() {
        List<CtStatement> codes = new ArrayList<>();
        for (CtClass clazz :clazzElements) {
            //TODO: get method name
            CtQuery query = clazz.map((CtClass cl)->cl.getMethods())
                    .filterChildren(new Filter<CtStatement>() {
                        @Override
                        public boolean matches(CtStatement element) {
                            if (element instanceof CtBlock || element instanceof CtComment)
                                return false;
                            return true;
                        }
                    });//TODO
            for (Object element :query.list()) {
                SourcePosition sourcePosition = ((CtStatement)element).getPosition();
                int lineNumber = sourcePosition.getLine();
                int endLine = sourcePosition.getEndLine();
                if (isBetweenLine(lineNumber, endLine)) {
                    codes.add((CtStatement) element);
                }
            }
        }
    }
}
