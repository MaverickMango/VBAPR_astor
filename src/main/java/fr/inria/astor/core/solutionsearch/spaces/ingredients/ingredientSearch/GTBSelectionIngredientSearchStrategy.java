package fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch;

import fr.inria.astor.approaches.jgenprog.extension.CodeAddFactory;
import fr.inria.astor.approaches.jgenprog.extension.ReplaceExpressionOp;
import fr.inria.astor.approaches.jgenprog.extension.ReplaceTypeOp;
import fr.inria.astor.approaches.jgenprog.operators.InsertAfterOp;
import fr.inria.astor.approaches.jgenprog.operators.InsertBeforeOp;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.RandomManager;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.stats.Stats;
import fr.inria.astor.util.MapList;
import fr.inria.astor.util.ReadFileUtil;
import fr.inria.astor.util.StringUtil;
import org.apache.log4j.Logger;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtBreakImpl;
import spoon.support.reflect.code.CtInvocationImpl;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class GTBSelectionIngredientSearchStrategy extends SimpleRandomSelectionIngredientStrategy {

    private static final Boolean DESACTIVATE_CACHE = ConfigurationProperties
            .getPropertyBool("desactivateingredientcache");
    protected Logger log = Logger.getLogger(this.getClass().getName());

    public GTBSelectionIngredientSearchStrategy(IngredientPool space) {
        super(space);
    }

    protected Ingredient getRandomVarFromContext(List<Ingredient> fixSpace, List<CtVariable> context) {
        if (context == null || context.size() == 0)
            return null;
        int size = context.size();
        int index = RandomManager.nextInt(size);
        Ingredient ingredient = fixSpace.get(RandomManager.nextInt(fixSpace.size()));
        ingredient.setCode(context.get(index).getReference());
        return ingredient;
    }

    protected int getRandomFromSpace(List<Ingredient> fixSpace) {
        if (fixSpace == null)
            return -1;
        int size = fixSpace.size();
        int index = RandomManager.nextInt(size);
        return index;
    }

    List<Ingredient> getstmts(List<Ingredient> base, AstorOperator operationType) {//context
        if (base == null || base.isEmpty()) {
            return null;
        }
        List<Ingredient> stmts = new ArrayList<>();
        for (Ingredient in :base) {
            if (in.getCode() instanceof CtInvocationImpl) {//discard super&this mthcall
                String name = ((CtInvocationImpl) in.getCode()).getExecutable().getSimpleName();
                if (name.equals("<init>")) {//
                    continue;
                }
            }
            if (operationType instanceof InsertBeforeOp && in.getCode() instanceof CtReturn)//
                continue;
            if (in.getCode().getParent() instanceof CtBlock && ( (in.getCode() instanceof CtInvocation)
                    || (in.getCode() instanceof CtAssignment) || (in.getCode() instanceof CtOperatorAssignment)
                    || (in.getCode() instanceof CtIf) || (in.getCode() instanceof CtReturn)
                    || (in.getCode() instanceof CtBreak)) ) {
                stmts.add(in);
            }
        }
        return stmts;
    }

    List<Ingredient> getExpIngredients(List<Ingredient> base, ModificationPoint point) {
        if (base == null || base.isEmpty()) {
            return null;
        }
        List<Ingredient> exps = new ArrayList<>();
        if (point.getCodeElement() instanceof CtBreakImpl) {
            Ingredient ingredient = new Ingredient(CodeAddFactory.createContinue());
            exps.add(ingredient);
            return exps;
        }
        for (Ingredient in :base) {
            if (in.getChacheCodeString().equals(point.getCodeElement().toString().replaceAll("\n","")))
                continue;
            if (in.getCode() instanceof CtStatement && !(in.getCode() instanceof CtExpression)) {//discard super&this mthcall
                continue;
            }
            if (in.getCode() instanceof CtExpression && point.getCodeElement() instanceof CtExpression) {
                CtTypeReference a = ((CtExpression) in.getCode()).getType();
                CtTypeReference b = ((CtExpression) point.getCodeElement()).getType();
                if (a.equals(b) || (a.isPrimitive() && b.isPrimitive())) {
                    exps.add(in);
                }
                continue;
            }
            if (in.getCode().getClass().equals(point.getCodeElement().getClass())) {
                exps.add(in);
            }
        }
        return exps;
    }

    List<Ingredient> getTypeChangeIngredients(List<Ingredient> base, ModificationPoint point) {
        if (base == null || base.isEmpty()) {
            return null;
        }
        Map<String, Set<CtTypeReference>> typeMap = new HashMap<>();
        typeMap.put("primitive", new HashSet<>());
        typeMap.put("object", new HashSet<>());
//        CtTypeReference oldType = ((CtLocalVariable) point.getCodeElement()).getType();
//        for (Ingredient in :base) {
//            assert in.getCode() instanceof CtLocalVariable;
//            CtTypeReference inType = ((CtLocalVariable) in.getCode()).getType();
//            if (inType.isPrimitive())
//                typeMap.get("primitive").add(inType);
//            else
//                typeMap.get("object").add(inType);
//        }
        return CodeAddFactory.createTypeChangeInredients((CtLocalVariable) point.getCodeElement(), typeMap);
    }

    List<Ingredient> limitIngredients(List<Ingredient> base, ModificationPoint point) {
        List<Ingredient> exps = new ArrayList<>();
        CtElement parent = point.getCodeElement().getParent();
        if (parent instanceof CtAssignment) {
            exps.addAll(base);
        }
        if (point.getCodeElement() instanceof CtConditional) {
            CtConditional cond = (CtConditional) point.getCodeElement();
            Ingredient in = new Ingredient(CodeAddFactory.createExpression(cond.getThenExpression(), cond.getParent()));
            exps.add(in);
            in = new Ingredient(CodeAddFactory.createExpression(cond.getElseExpression(), cond.getParent()));
            exps.add(in);
        }
        if (point.getCodeElement() instanceof CtTypedElement) {
            CtTypedElement mpvar = (CtTypedElement) point.getCodeElement();
            List<CtVariable> contexts = point.getContextOfModificationPoint();
            for (CtVariable context : contexts) {//target
                if (context.getSimpleName().equals(point.getCodeElement().toString()))
                    continue;
                if (!mpvar.getType().isPrimitive() && !mpvar.getType().equals(context.getType()))
                    continue;
                Ingredient ingredient = new Ingredient(CodeAddFactory.createVariableRead(context));
                exps.add(ingredient);
            }
        }
        if (point.getCodeElement() instanceof CtVariableRead && ((CtVariableRead<?>) point.getCodeElement()).getType().isPrimitive()) {//
            CtTypeReference typeReference = MutationSupporter.getFactory().Core().createTypeReference();
            typeReference.setSimpleName("double");
            Ingredient ingredient = new Ingredient(CodeAddFactory.createTypeCast((CtVariableRead) point.getCodeElement(), typeReference));
            exps.add(ingredient);
        }
        if (point.getCodeElement() instanceof CtUnaryOperator && ((CtUnaryOperator) point.getCodeElement()).getType().isPrimitive()) {//
            CtTypeReference typeReference = MutationSupporter.getFactory().Core().createTypeReference();
            typeReference.setSimpleName("double");
            Ingredient ingredient = new Ingredient(CodeAddFactory.createTypeCast((CtUnaryOperator) point.getCodeElement(), typeReference));
            exps.add(ingredient);
        }
        if (parent instanceof CtIf && point.getCodeElement() instanceof CtBinaryOperator
                && (((CtBinaryOperator<?>) point.getCodeElement()).getKind().equals(BinaryOperatorKind.AND)
                    ||((CtBinaryOperator<?>) point.getCodeElement()).getKind().equals(BinaryOperatorKind.OR) )) {
            Ingredient ingredient = new Ingredient(CodeAddFactory.deleteCondition((CtBinaryOperator) point.getCodeElement()));
            exps.add(ingredient);
        }
        if (point.getCodeElement() instanceof CtBinaryOperator &&
                (((CtBinaryOperator<?>) point.getCodeElement()).getLeftHandOperand() instanceof CtBinaryOperator
                    ||((CtBinaryOperator<?>) point.getCodeElement()).getRightHandOperand() instanceof CtBinaryOperator)) {
            Ingredient ingredient = new Ingredient(CodeAddFactory.createBinaryOperator((CtBinaryOperator) point.getCodeElement()));
            exps.add(ingredient);
        }
        if (parent instanceof CtAssignment) {
            CtTypeReference typeReference = ((CtAssignment)parent).getAssigned().getType();
            Ingredient ingredient = new Ingredient(CodeAddFactory.createTypeCast((CtExpression) point.getCodeElement(), typeReference));
            exps.add(ingredient);
        }
        if (parent instanceof CtReturn) {
            CtTypeReference typeReference = ((CtReturn)parent).getParent(CtMethod.class).getType();
            Ingredient ingredient = new Ingredient(CodeAddFactory.createTypeCast((CtExpression) point.getCodeElement(), typeReference));
            exps.add(ingredient);
        }
        if (point.getCodeElement() instanceof CtInvocation) {
            CtExecutableReference pexe = ((CtInvocation<?>) point.getCodeElement()).getExecutable();
            List<CtTypeReference<?>> argsType = pexe.getParameters();
            Collection<CtExecutableReference<?>> methods = point.getCodeElement().getParent(new TypeFilter<>(CtClass.class)).getAllExecutables();
            for (CtExecutableReference exe :methods) {
                List<CtTypeReference<?>> paras = exe.getParameters();
                if (exe.getSimpleName().equals(pexe.getSimpleName())) {
                    List<CtExpression<?>> args = new ArrayList<>(((CtInvocation)point.getCodeElement()).getArguments());
                    Ingredient ingredient = new Ingredient(
                            CodeAddFactory.createInvocationWithVars((CtInvocation) point.getCodeElement()
                                    , exe, args));
                    if (ingredient.getCode() != null)// && !contains(exps, ingredient)
                        exps.add(ingredient);
                } else if (isParasSame(paras, argsType) &&
                        ((CtInvocation<?>) point.getCodeElement()).getExecutable().getType().equals(exe.getType())) {
                    Ingredient ingredient = new Ingredient(CodeAddFactory.createInvocationSameArgs((CtInvocation) point.getCodeElement(), exe));
                    if (ingredient.getCode() != null)// && !contains(exps, ingredient)
                        exps.add(ingredient);
                }
            }
            if (((CtInvocation<?>) point.getCodeElement()).getArguments() != null) {
                for (CtExpression arg :((CtInvocation<?>) point.getCodeElement()).getArguments()) {
                    if (!(arg.getType().equals(point.getCodeElement()))
                            && (!arg.getType().isPrimitive()
                                || !((CtInvocation<?>) point.getCodeElement()).getType().isPrimitive())) {
                        continue;
                    }
                    Ingredient ingredient = new Ingredient(CodeAddFactory.createExpression(arg, point.getCodeElement().getParent()));
                    if (ingredient.getCode() != null)// && !contains(exps, ingredient)
                        exps.add(ingredient);
                }
            }
        }

        for (Ingredient in :base) {
            if ((point.getCodeElement() instanceof CtVariableRead || point.getCodeElement() instanceof CtLiteral)
                    && in.getCode() instanceof CtInvocation) {
                CtInvocation ininv = (CtInvocation) in.getCode();
                if (ininv.getExecutable().getParameters().size() == 1) {
                    CtTypeReference intype = (CtTypeReference) ininv
                            .getExecutable().getParameters().get(0);
                    if (intype.equals(((CtExpression<?>) point.getCodeElement()).getType())
                            || (intype.isPrimitive() && ((CtExpression<?>) point.getCodeElement()).getType().isPrimitive())) {
                        Ingredient ingredient = new Ingredient(
                                CodeAddFactory.createInvocationWithVar(ininv, (CtExpression) point.getCodeElement()));
                        if (ingredient.getCode() != null)// && !contains(exps, ingredient)
                            exps.add(ingredient);
                    }
                }
            }

            if (point.getCodeElement() instanceof CtInvocation && in.getCode() instanceof CtInvocation) {
                CtInvocation pinv = (CtInvocation) point.getCodeElement();
                CtInvocation ininv = (CtInvocation) in.getCode();
                if (pinv.getExecutable().getSimpleName().equals(ininv.getExecutable().getSimpleName()))
                    continue;
                if (ininv.getExecutable().getType().equals(pinv.getExecutable().getType())) {
                    String fixName = ((CtInvocation) in.getCode()).getExecutable().getSimpleName();
                    if (pinv.getExecutable().getSimpleName().equals(fixName)) {
                        if (!pinv.toString().equals(ininv.toString())) {
                            Ingredient ingredient = new Ingredient(CodeAddFactory.createInvocationSameName(pinv, ininv));
                            if (ingredient.getCode() != null)// && !contains(exps, ingredient)
                                exps.add(ingredient);
                        }
                    } else {
                        CtExecutableReference ine = ininv.getExecutable();
                        CtExecutableReference pe = pinv.getExecutable();
                        if (isParasSame(ine.getParameters(), pe.getParameters())) {
                            Ingredient ingredient = new Ingredient(CodeAddFactory.createInvocationSameArgs(pinv, ininv.getExecutable()));
                            if (ingredient.getCode() != null)// && !contains(exps, ingredient)
                                exps.add(ingredient);
                        }
                    }
                }
            }

            if ((parent instanceof CtIf || parent instanceof CtReturn)) {// && !contains(exps, in)
                exps.add(in);
            } else if (!(in.getCode() instanceof CtVariableRead)) {
                if ("target".equalsIgnoreCase(String.valueOf(point.getCodeElement().getRoleInParent()))
                        && in.getCode() instanceof CtLiteral)
                    continue;
                exps.add(in);
            }

//            if (!(point.getCodeElement() instanceof CtVariableRead
//                    || point.getCodeElement() instanceof CtVariableReference
//                    || point.getCodeElement() instanceof CtLiteral)) {
//                if (in.getCode() instanceof CtVariableRead || in.getCode() instanceof CtVariableReference)
//                    continue;
//                exps.add(in);
//            } else

            if (parent instanceof CtIf) {
                if (in.getCode() instanceof CtInvocation) {
                    String intype = ((CtInvocation) in.getCode()).getType().getSimpleName();
                    if (!intype.equalsIgnoreCase("boolean"))
                        continue;
                } else if (!(in.getCode() instanceof CtBinaryOperator))
                    continue;
                List<CtBinaryOperator> list = CodeAddFactory.createCondition(point.getCodeElement(), in.getCode());
                for (CtBinaryOperator ctb :list) {
                    Ingredient ingredient = new Ingredient(ctb);
                    if (ingredient.getCode() != null)// && !contains(exps, ingredient)
                        exps.add(ingredient);
                }
            }

        }
        return deduplicate(exps);
    }

    boolean isArgsSame(List<CtExpression> args1, List<CtExpression> args2) {
        boolean flag = args1.size() == args2.size();
        if (!flag || args1.isEmpty() || args2.isEmpty())
            return flag;
        for (CtExpression exp1 :args1) {
            if (!args2.contains(exp1)) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    boolean isParasSame(List<CtTypeReference<?>> p1, List<CtTypeReference<?>> p2) {
        boolean flag = (p1.isEmpty() && p2.isEmpty()) || (p1.size() == p2.size() && p1.containsAll(p2));
        return flag;
    }

    boolean contains(List<Ingredient> bases, Ingredient in) {
        boolean flag = false;
        for (Ingredient base :bases) {
            try {
                String baseCode = base.getChacheCodeString();
                String inCode = in.getChacheCodeString();
                if (baseCode.equals(inCode)) {
                    flag = true;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    List<Ingredient> deduplicate(List<Ingredient> bases) {
        HashSet<Ingredient> set = new HashSet<>(bases);
        bases.clear();
        bases.addAll(set);
        return bases;
    }

    private Map<String, List<String>> elementSet = new HashMap();


    /**
     * Method that returns an Ingredient from the ingredient space given a
     * modification point and a Operator
     *
     * @param modificationPoint point to be modified using an ingredient
     * @param operationType     operation applied to the modif point
     * @return an ingredient
     */
    @Override
    public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {

        int attemptsBaseIngredients = 0;

        List<Ingredient> baseElements = geIngredientsFromSpace(modificationPoint, operationType);

        if (operationType instanceof InsertBeforeOp || operationType instanceof InsertAfterOp)
            baseElements = getstmts(baseElements, operationType);

        if (operationType instanceof ReplaceExpressionOp)//same return type
            baseElements = getExpIngredients(baseElements, modificationPoint);

        if (operationType instanceof ReplaceTypeOp) //extract typereference
            baseElements = getTypeChangeIngredients(baseElements, modificationPoint);


        CtElement parent = modificationPoint.getCodeElement().getParent();
        if (operationType instanceof ReplaceExpressionOp &&
                (modificationPoint.getCodeElement() instanceof CtInvocation
                || modificationPoint.getCodeElement() instanceof CtVariableRead
                || modificationPoint.getCodeElement() instanceof CtBinaryOperator
                || modificationPoint.getCodeElement() instanceof CtUnaryOperator
                || modificationPoint.getCodeElement() instanceof CtConditional
                || modificationPoint.getCodeElement() instanceof CtConstructorCall
                || parent instanceof CtIf || parent instanceof CtAssignment
                || parent instanceof CtReturn))
            baseElements = limitIngredients(baseElements, modificationPoint);


        if (baseElements == null || baseElements.isEmpty()) {
            log.debug("Any element available for mp " + modificationPoint);
            return null;
        }

        String eleKey = getKey(modificationPoint, operationType);
        if (!elementSet.containsKey(eleKey)) {
            elementSet.put(eleKey, new ArrayList<>());
            StringBuilder stringBuilder = new StringBuilder();
            Logger detailLog = Logger.getLogger("DetailLog");
            stringBuilder.append("BaseElements for " + eleKey + " : " + modificationPoint + ":");
            stringBuilder.append(" [");
            for (Ingredient in :baseElements) {
                stringBuilder.append("\"").append(in.getChacheCodeString()).append("\",");
            }
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(),"]");
            detailLog.debug(stringBuilder);
        }

        if (!elementSet.get(eleKey).isEmpty()) {
            removeUsed(baseElements, eleKey);
        }

        int elementsFromFixSpace = baseElements.size();
        log.debug("Templates availables" + elementsFromFixSpace);

        Stats.currentStat.getIngredientsStats().addSize(Stats.currentStat.getIngredientsStats().ingredientSpaceSize,
                baseElements.size());

        while (attemptsBaseIngredients < elementsFromFixSpace) {

            attemptsBaseIngredients++;
            log.debug(String.format("Attempts Base Ingredients  %d total %d", attemptsBaseIngredients,
                    elementsFromFixSpace));

            int idx = getRandomFromSpace(baseElements);
            if (idx == -1)
                continue;
            Ingredient baseIngredient = baseElements.get(idx);

            boolean flag = false;
            for (ModificationPoint mp :modificationPoint.getProgramVariant().getModificationPoints()) {
                if (mp == modificationPoint) {
                    String orig = modificationPoint.getCodeElement().toString().replaceAll("\\n", "");
                    String modi = baseIngredient.getChacheCodeString();
                    flag = flag || orig.equals(modi);
                }
                flag = flag || mp.getCodeElement() == baseIngredient.getCode();
            }
            if (flag)
                continue;

//            String newingredientkey = getKey(modificationPoint, operationType);

            if (baseIngredient != null && baseIngredient.getCode() != null) {

                elementSet.get(eleKey).add(baseIngredient.getChacheCodeString());
                return baseIngredient;
//                // check if the element was already used
//                if (DESACTIVATE_CACHE || !this.cache.containsKey(newingredientkey)
//                        || !this.cache.get(newingredientkey).contains(baseIngredient.getChacheCodeString())) {
//                    this.cache.add(newingredientkey, baseIngredient.getChacheCodeString());
//                }

            }

        } // End while

        log.debug("--- no mutation left to apply in element "
                + StringUtil.trunc(modificationPoint.getCodeElement().getShortRepresentation())
                + ", search space size: " + elementsFromFixSpace);
        return null;
    }


    public String getKey(ModificationPoint modPoint, AstorOperator operator) {
        String lockey = null;
        if (modPoint instanceof SuspiciousModificationPoint)
            lockey = ((SuspiciousModificationPoint)modPoint).getSuspicious().getLineNumber() + "-"
                + modPoint.getCodeElement().toString().replaceAll("\n", "") + "-"
//                + modPoint.getCodeElement().getParent() + "-"
                + operator.toString();
        else
            lockey = modPoint.getCodeElement().getPosition().toString() + "-"
                + modPoint.getCodeElement().toString().replaceAll("\n", "") + "-"
//                + modPoint.getCodeElement().getParent() + "-"
                + operator.toString();
        return lockey;
    }

    private void removeUsed(List<Ingredient> bases, String eleKey) {
        for (String used: elementSet.get(eleKey)) {
            for (Ingredient in :bases) {
                if (used.equals(in.getChacheCodeString())) {
                    bases.remove(in);
                    break;
                }
            }
        }
    }
}
