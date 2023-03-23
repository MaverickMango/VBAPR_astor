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
import fr.inria.astor.util.StringUtil;
import org.apache.log4j.Logger;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtBlockImpl;
import spoon.support.reflect.code.CtBreakImpl;
import spoon.support.reflect.code.CtInvocationImpl;

import java.lang.reflect.Constructor;
import java.util.*;

public class GTBSelectionIngredientSearchStrategy extends SimpleRandomSelectionIngredientStrategy {

    private static final Boolean DESACTIVATE_CACHE = ConfigurationProperties
            .getPropertyBool("desactivateingredientcache");
    protected Logger log = Logger.getLogger(this.getClass().getName());

    MapList<String, Ingredient> baseIngredientsCache = new MapList<>();

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

    List<Ingredient> getstmts(List<Ingredient> base, AstorOperator operationType) {
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
            /* && ( (in.getCode() instanceof CtInvocation)
                    || (in.getCode() instanceof CtAssignment) || (in.getCode() instanceof CtOperatorAssignment)
                    || (in.getCode() instanceof CtIf) || (in.getCode() instanceof CtReturn)
                    || (in.getCode() instanceof CtBreak)) */
            if (in.getCode().getParent() instanceof CtBlockImpl) {
                in.setDerivedFrom(in.getCode().getParent());
                stmts.add(in);
            }
        }
        return stmts;
    }

    List<Ingredient> getExpIngredientsSameType(List<Ingredient> base, ModificationPoint point) throws Exception {
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
            if (in.getChacheCodeString().equals(point.getCodeElement().toString().replaceAll("\\s+"," ")))
                continue;
            if ((in.getCode() instanceof CtStatement && !(in.getCode() instanceof CtExpression) )|| in.getCode() instanceof CtAssignment) {//discard super&this mthcall
                continue;
            }
            if (in.getCode() instanceof CtExpression && point.getCodeElement() instanceof CtExpression) {
                CtTypeReference a = ((CtExpression) in.getCode()).getType();
                CtTypeReference b = ((CtExpression) point.getCodeElement()).getType();
                if (a.equals(b) || isCompatiblePrimitive((CtTypedElement) in.getCode(), (CtTypedElement) point.getCodeElement())) {
                    in.setDerivedFrom(in.getCode().getParent());
                    exps.add(in);
                }
                continue;
            }
            if (in.getCode().getClass().equals(point.getCodeElement().getClass())) {
                in.setDerivedFrom(in.getCode().getParent());
                exps.add(in);
            }
        }
        return exps;
    }

    List<Ingredient> getTypeChangeIngredients(List<Ingredient> base, ModificationPoint point) throws Exception {
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

    List<Ingredient> limitIngredientsByElementType(List<Ingredient> base, ModificationPoint point) throws Exception {
        List<Ingredient> exps = new ArrayList<>();
        CtElement parent = point.getCodeElement().getParent();
        if (point.getCodeElement() instanceof CtBreakImpl) {
            Ingredient ingredient = new Ingredient(CodeAddFactory.createContinue());
            ingredient.setDerivedFrom(point.getCodeElement());
            exps.add(ingredient);
            exps.addAll(getstmts(base, null));
            return exps;
        }
        if (point.getCodeElement() instanceof CtConditional) {
            CtConditional cond = (CtConditional) point.getCodeElement();
            Ingredient in = new Ingredient(CodeAddFactory.createExpression(cond.getThenExpression(), cond.getParent()));
            in.setDerivedFrom(point.getCodeElement());
            exps.add(in);
            in = new Ingredient(CodeAddFactory.createExpression(cond.getElseExpression(), cond.getParent()));
            in.setDerivedFrom(point.getCodeElement());
            exps.add(in);
        }
        if (point.getCodeElement() instanceof CtConstructorCall) {
            try {
                Class type = Class.forName(((CtConstructorCall<?>) point.getCodeElement()).getExecutable().getType().getQualifiedName());
                Constructor[] cts = type.getConstructors();
                List<CtVariable> contexts = point.getContextOfModificationPoint();
                for (int i = 0; i < cts.length; i++) {
                    Class[] paracls = cts[i].getParameterTypes();
                    List<String> paras = new ArrayList<>();
                    for (int j = 0; j < paracls.length; j++) {
                        paras.add(paracls[j].getName());
                    }
                    Ingredient ingredient = new Ingredient(
                            CodeAddFactory.createConstructorCall(paras,
                                    contexts, (CtConstructorCall) point.getCodeElement()));
                    if (ingredient.getCode() != null) {// && !contains(exps, ingredient)
                        ingredient.setDerivedFrom(point.getCodeElement());
                        exps.add(ingredient);
                    }
                }
            } catch (ClassNotFoundException ignored) {

            }
        }
        if (point.getCodeElement() instanceof CtTypedElement) {
            CtTypedElement mpvar = (CtTypedElement) point.getCodeElement();
            List<CtVariable> contexts = point.getContextOfModificationPoint();
            for (CtVariable context : contexts) {//target
                if (context.getSimpleName().equals(point.getCodeElement().toString()))
                    continue;
                if (!isCompatiblePrimitive(mpvar, context) && !mpvar.getType().equals(context.getType()))//[poi]check special type
                    continue;
                Ingredient ingredient = new Ingredient(CodeAddFactory.createVariableRead(context));
                ingredient.setDerivedFrom(context);
                exps.add(ingredient);
            }
        }
        if (point.getCodeElement() instanceof CtVariableRead &&
                (((CtVariableRead<?>) point.getCodeElement()).getType().isPrimitive()
                        && !((CtVariableRead<?>) point.getCodeElement()).getType().getSimpleName().equalsIgnoreCase("boolean"))) {
            CtTypeReference typeReference = MutationSupporter.getFactory().Core().createTypeReference();
            typeReference.setSimpleName("double");
            Ingredient ingredient = new Ingredient(CodeAddFactory.createTypeCast((CtVariableRead) point.getCodeElement(), typeReference));
            ingredient.setDerivedFrom(point.getCodeElement());
            exps.add(ingredient);
        }
        if (point.getCodeElement() instanceof CtUnaryOperator &&
                ((CtUnaryOperator) point.getCodeElement()).getType().isPrimitive()
                    && ((CtUnaryOperator<?>) point.getCodeElement()).getType().getSimpleName().equalsIgnoreCase("boolean")) {//
            CtTypeReference typeReference = MutationSupporter.getFactory().Core().createTypeReference();
            typeReference.setSimpleName("double");
            Ingredient ingredient = new Ingredient(CodeAddFactory.createTypeCast((CtUnaryOperator) point.getCodeElement(), typeReference));
            ingredient.setDerivedFrom(point.getCodeElement());
            exps.add(ingredient);
        }
        if (parent instanceof CtIf && point.getCodeElement() instanceof CtBinaryOperator
                && (((CtBinaryOperator<?>) point.getCodeElement()).getKind().equals(BinaryOperatorKind.AND)
                    ||((CtBinaryOperator<?>) point.getCodeElement()).getKind().equals(BinaryOperatorKind.OR) )) {
            Ingredient ingredient = new Ingredient(CodeAddFactory.deleteCondition((CtBinaryOperator) point.getCodeElement()));
            ingredient.setDerivedFrom(point.getCodeElement());
            exps.add(ingredient);
        }
        if (point.getCodeElement() instanceof CtBinaryOperator &&
                (((CtBinaryOperator<?>) point.getCodeElement()).getLeftHandOperand() instanceof CtBinaryOperator
                    ||((CtBinaryOperator<?>) point.getCodeElement()).getRightHandOperand() instanceof CtBinaryOperator)) {
            Ingredient ingredient = new Ingredient(CodeAddFactory.createBinaryOperator((CtBinaryOperator) point.getCodeElement()));
            ingredient.setDerivedFrom(point.getCodeElement());
            exps.add(ingredient);
        }
        if (parent instanceof CtAssignment) {
            CtTypeReference typeReference = ((CtAssignment)parent).getAssigned().getType();
            Ingredient ingredient = new Ingredient(CodeAddFactory.createTypeCast((CtExpression) point.getCodeElement(), typeReference));
            ingredient.setDerivedFrom(typeReference);
            exps.add(ingredient);
        }
        if (parent instanceof CtReturn) {
            CtTypeReference typeReference = ((CtReturn)parent).getParent(CtMethod.class).getType();
            Ingredient ingredient = new Ingredient(CodeAddFactory.createTypeCast((CtExpression) point.getCodeElement(), typeReference));
            ingredient.setDerivedFrom(typeReference);
            exps.add(ingredient);
        }
        if (point.getCodeElement() instanceof CtInvocation) {
            CtExecutableReference pexe = ((CtInvocation<?>) point.getCodeElement()).getExecutable();
            List<CtTypeReference<?>> argsType = pexe.getParameters();
            Collection<CtExecutableReference<?>> methods = point.getCodeElement().getParent(new TypeFilter<>(CtClass.class)).getAllExecutables();
            for (CtExecutableReference exe :methods) {
                List<CtTypeReference<?>> paras = exe.getParameters();
                if (exe.getSimpleName().equals(pexe.getSimpleName())) {
                    continue;
//                    List<CtExpression<?>> args = new ArrayList<>(((CtInvocation)point.getCodeElement()).getArguments());
//                    Ingredient ingredient = new Ingredient(
//                            CodeAddFactory.createInvocationWithVars((CtInvocation) point.getCodeElement()
//                                    , exe, args));
//                    if (ingredient.getCode() != null) {// && !contains(exps, ingredient)
//                        ingredient.setDerivedFrom(exe);
//                        exps.add(ingredient);
//                    }
                } else if (isParasSame(paras, argsType) &&
                        ((CtInvocation<?>) point.getCodeElement()).getExecutable().getType().equals(exe.getType())) {
                    Ingredient ingredient = new Ingredient(CodeAddFactory.createInvocationSameArgs((CtInvocation) point.getCodeElement(), exe));
                    if (ingredient.getCode() != null) {// && !contains(exps, ingredient)
                        ingredient.setDerivedFrom(exe);
                        exps.add(ingredient);
                    }
                }
            }
            if (((CtInvocation<?>) point.getCodeElement()).getArguments() != null) {
                for (CtExpression arg :((CtInvocation<?>) point.getCodeElement()).getArguments()) {
                    if (!(arg.getType().equals(point.getCodeElement()))
                            && !isCompatiblePrimitive(arg, (CtTypedElement) point.getCodeElement())) {
                        continue;
                    }
                    Ingredient ingredient = new Ingredient(CodeAddFactory.createExpression(arg, point.getCodeElement().getParent()));
                    if (ingredient.getCode() != null) {// && !contains(exps, ingredient)
                        ingredient.setDerivedFrom(arg);
                        exps.add(ingredient);
                    }
                }
            }
        }

        for (Ingredient in :base) {
            if (in.getChacheCodeString().equals(point.getCodeElement().toString().replaceAll("\\s+"," ")))
                continue;
            if ((in.getCode() instanceof CtStatement && in.getCode().getParent() instanceof CtBlock
                    && (parent instanceof CtAssignment) || parent instanceof CtInvocation)) {//discard super&this mthcall
                exps.add(in);
            }
            if (in.getCode() instanceof CtExpression && point.getCodeElement() instanceof CtExpression) {
                CtTypeReference a = ((CtExpression) in.getCode()).getType();
                CtTypeReference b = ((CtExpression) point.getCodeElement()).getType();
                if (!a.equals(b) || !isCompatiblePrimitive((CtTypedElement) in.getCode(), (CtTypedElement) point.getCodeElement())) {
                    continue;
                }
            } else if (!in.getCode().getClass().equals(point.getCodeElement().getClass())) {
                continue;
            }
            if ((point.getCodeElement() instanceof CtVariableRead || point.getCodeElement() instanceof CtLiteral)
                    && in.getCode() instanceof CtInvocation) {
                CtInvocation ininv = (CtInvocation) in.getCode();
                if (ininv.getExecutable().getParameters().size() == 1) {
                    CtTypeReference intype = (CtTypeReference) ininv
                            .getExecutable().getParameters().get(0);
                    if (intype.equals(((CtExpression<?>) point.getCodeElement()).getType())
                            || isCompatiblePrimitive(ininv, (CtTypedElement) point.getCodeElement())) {//
                        Ingredient ingredient = new Ingredient(
                                CodeAddFactory.createInvocationWithVar(ininv, (CtExpression) point.getCodeElement()));
                        if (ingredient.getCode() != null) {// && !contains(exps, ingredient)
                            ingredient.setDerivedFrom(ininv);
                            exps.add(ingredient);
                        }
                    }
                }
            }

            if (point.getCodeElement() instanceof CtInvocation && in.getCode() instanceof CtInvocation) {
                CtInvocation pinv = (CtInvocation) point.getCodeElement();
                CtInvocation ininv = (CtInvocation) in.getCode();
                if (pinv.getExecutable().getSimpleName().equals(ininv.getExecutable().getSimpleName()))
                    continue;
                if (ininv.getExecutable().getType().equals(pinv.getExecutable().getType())) {//
                    String fixName = ((CtInvocation) in.getCode()).getExecutable().getSimpleName();
                    if (pinv.getExecutable().getSimpleName().equals(fixName)) {
                        if (!pinv.toString().equals(ininv.toString())) {
                            Ingredient ingredient = new Ingredient(CodeAddFactory.createInvocationSameName(pinv, ininv));
                            if (ingredient.getCode() != null) {// && !contains(exps, ingredient)
                                ingredient.setDerivedFrom(ininv);
                                exps.add(ingredient);
                            }
                        }
                    } else {
                        CtExecutableReference ine = ininv.getExecutable();
                        CtExecutableReference pe = pinv.getExecutable();
                        if (isParasSame(ine.getParameters(), pe.getParameters())) {
                            Ingredient ingredient = new Ingredient(CodeAddFactory.createInvocationSameArgs(pinv, ininv.getExecutable()));
                            if (ingredient.getCode() != null) {// && !contains(exps, ingredient)
                                ingredient.setDerivedFrom(ininv);
                                exps.add(ingredient);
                            }
                        }
                    }
                }
            }

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
                    if (ingredient.getCode() != null) {// && !contains(exps, ingredient)
                        ingredient.setDerivedFrom(in.getCode());
                        exps.add(ingredient);
                    }
                }
            }

            if ((parent instanceof CtIf || parent instanceof CtReturn || parent instanceof CtAssignment)) {
                in.setDerivedFrom(in.getCode().getParent());
                exps.add(in);//?
            } else if (!(in.getCode() instanceof CtVariableRead)) {
                if ("target".equalsIgnoreCase(String.valueOf(point.getCodeElement().getRoleInParent()))
                        && in.getCode() instanceof CtLiteral)
                    continue;
                in.setDerivedFrom(in.getCode().getParent());
                exps.add(in);
            }
        }
        return deduplicate(exps);
    }

    boolean isCompatiblePrimitive(CtTypedElement element1, CtTypedElement element2) {
        boolean isCompatiblePrimitive = element1.getType().isPrimitive() && element2.getType().isPrimitive() &&
                ((element1.getType().getSimpleName().equalsIgnoreCase("boolean")
                        && element2.getType().getSimpleName().equalsIgnoreCase("boolean")) ||
                        (!element1.getType().getSimpleName().equalsIgnoreCase("boolean") &&
                                !element2.getType().getSimpleName().equalsIgnoreCase("boolean")));
        return isCompatiblePrimitive;
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

    List<Ingredient> shrinkBaseElements(ModificationPoint modificationPoint, AstorOperator operationType) {
        String key = getKey(modificationPoint, operationType);
        List<Ingredient> baseElements = baseIngredientsCache.get(key);
        if (baseElements != null)
            return baseElements;
        try {
            if (ConfigurationProperties.getPropertyBool("duplicateingredientsinspace"))
                baseElements = deduplicate(geIngredientsFromSpace(modificationPoint, operationType));
            else
                baseElements = geIngredientsFromSpace(modificationPoint, operationType);

            if (operationType instanceof InsertBeforeOp || operationType instanceof InsertAfterOp)
                baseElements = getstmts(baseElements, operationType);

            if (operationType instanceof ReplaceTypeOp) //extract typereference
                baseElements = getTypeChangeIngredients(baseElements, modificationPoint);

//            if (operationType instanceof ReplaceExpressionOp)//same return type
//                baseElements = getExpIngredientsSameType(baseElements, modificationPoint);
            CtElement parent = modificationPoint.getCodeElement().getParent();
            if (operationType instanceof ReplaceExpressionOp/* &&
                    (modificationPoint.getCodeElement() instanceof CtInvocation
                            || modificationPoint.getCodeElement() instanceof CtVariableRead
                            || modificationPoint.getCodeElement() instanceof CtBinaryOperator
                            || modificationPoint.getCodeElement() instanceof CtUnaryOperator
                            || modificationPoint.getCodeElement() instanceof CtConditional
                            || modificationPoint.getCodeElement() instanceof CtConstructorCall
                            || parent instanceof CtIf || parent instanceof CtAssignment
                            || parent instanceof CtReturn)*/)
                baseElements = limitIngredientsByElementType(baseElements, modificationPoint);

            baseIngredientsCache.put(key, new ArrayList<>(baseElements));
        } catch (Exception e) {
            log.error("error in create ingredient bases : " + e.getMessage());
            e.printStackTrace();
        }
        return baseElements;
    }

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

        List<Ingredient> baseElements = shrinkBaseElements(modificationPoint, operationType);

        if (baseElements == null || baseElements.isEmpty()) {
            log.debug("Any element available for mp " + modificationPoint);
            return null;
        }

        Logger detailLog = Logger.getLogger("DetailLog");
        String eleKey = getKey(modificationPoint, operationType);
        if (!this.cache.containsKey(eleKey)) {
            this.cache.put(eleKey, new ArrayList<>());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("BaseElements for ").append(eleKey).append(" : ").append(modificationPoint).append(": \n");
            stringBuilder.append("size: ").append(baseElements.size()).append("\n")
                    .append(" [");
            for (Ingredient in :baseElements) {
                stringBuilder.append("\"").append(in.getChacheCodeString()).append("\",");
            }
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(),"]\n");
            stringBuilder.append("BaseElements End.");
            detailLog.debug(stringBuilder);
        }

        if (!this.cache.get(eleKey).isEmpty()) {
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
//            detailLog.debug("choose ingredient: " + baseIngredient);

//            boolean flag = false;
//            for (ModificationPoint mp :modificationPoint.getProgramVariant().getModificationPoints()) {
//                if (mp == modificationPoint) {
//                    String orig = modificationPoint.getCodeElement().toString().replaceAll("\\s+", " ");
//                    String modi = baseIngredient.getChacheCodeString();
//                    flag = flag || orig.equals(modi);
//                }
//                flag = flag || mp.getCodeElement() == baseIngredient.getCode();
//            }
//            if (flag)
//                continue;

//            String newingredientkey = getKey(modificationPoint, operationType);

            if (baseIngredient != null && baseIngredient.getCode() != null) {

//                this.cache.get(eleKey).add(baseIngredient.getChacheCodeString());
//                detailLog.debug("add used element: " + baseIngredient);
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
                + modPoint.getCodeElement().toString().replaceAll("\\s+", " ") + "-"
//                + modPoint.getCodeElement().getParent() + "-"
                + operator.toString();
        else
            lockey = modPoint.getCodeElement().getPosition().toString() + "-"
                + modPoint.getCodeElement().toString().replaceAll("\\s+", " ") + "-"
//                + modPoint.getCodeElement().getParent() + "-"
                + operator.toString();
        return lockey;
    }

    void removeUsed(List<Ingredient> bases, String eleKey) {
        if (bases.size() == this.cache.get(eleKey).size()) {
            bases.clear();
            return;
        }
        for (String used: this.cache.get(eleKey)) {
            for (Ingredient in :bases) {
                if (used.replaceAll("\\s+", " ").equals(in.getChacheCodeString().replaceAll("\\s+", " "))) {
                    bases.remove(in);
                    break;
                }
            }
        }
    }
}
