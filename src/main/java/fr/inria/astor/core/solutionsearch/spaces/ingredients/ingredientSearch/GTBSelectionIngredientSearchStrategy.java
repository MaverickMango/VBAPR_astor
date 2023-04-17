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
import fr.inria.astor.core.manipulation.sourcecode.VariableResolver;
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
import java.util.stream.Collectors;

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

    List<Ingredient> getstmts(ModificationPoint modificationPoint, List<Ingredient> base, AstorOperator operationType) {
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
            if (in.getCode().getParent() instanceof CtBlockImpl && !in.getCode().equals(modificationPoint.getCodeElement())) {
                in.setDerivedFrom(in.getCode());
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
                if (a == null || b == null)
                    continue;
                if (VariableResolver.areTypesCompatible(b, a) || isCompatiblePrimitive(a, b)) {
                    in.setDerivedFrom(in.getCode());
                    exps.add(in);
                }
                continue;
            }
            if (in.getCode().getClass().equals(point.getCodeElement().getClass())) {
                in.setDerivedFrom(in.getCode());
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
            exps.addAll(getstmts(point, base, null));
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
        if (point.getCodeElement() instanceof CtTypedElement && !(point.getCodeElement().getParent() instanceof CtBlock)) {
            CtTypedElement mpvar = (CtTypedElement) point.getCodeElement();
            List<CtVariable> contexts = point.getContextOfModificationPoint();
            for (CtVariable context : contexts) {//target
                if (context.getSimpleName().equals(point.getCodeElement().toString()))
                    continue;
                if (context.getType() == null || mpvar.getType() == null)
                    continue;
                if (!VariableResolver.areTypesCompatible(mpvar.getType(), context.getType()) && !isCompatiblePrimitive(mpvar.getType(), context.getType()))//[poi]check special type
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
            List<CtExpression<?>> args = ((CtInvocation<?>) point.getCodeElement()).getArguments();
            Collection<CtExecutableReference<?>> methods = point.getCodeElement().getParent(new TypeFilter<>(CtClass.class)).getAllExecutables();
            for (CtExecutableReference exe :methods) {
                List<CtTypeReference<?>> paras = exe.getParameters();
                if (exe.getSimpleName().equals(pexe.getSimpleName())) {
                    if (args.size() >= paras.size()) {
                        CtExpression[] newArgs = new CtExpression[paras.size()];
                        for (CtExpression arg1 :args) {
                            int pos = getArgPosition(arg1, paras);
                            if (pos == -1)
                                continue;
                            newArgs[pos] = arg1;
                        }
                        Ingredient ingredient = new Ingredient(
                                CodeAddFactory.createInvocationWithVars((CtInvocation) point.getCodeElement()
                                        , exe, Arrays.asList(newArgs)));
                        if (ingredient.getCode() != null) {
                            ingredient.setDerivedFrom(point.getCodeElement());
                            exps.add(ingredient);
                        }
                    }
                } else if (isParasSame(paras, args) &&
                        ((CtInvocation<?>) point.getCodeElement()).getExecutable().getType().equals(exe.getType())) {
                    Ingredient ingredient = new Ingredient(CodeAddFactory.createInvocationSameArgs((CtInvocation) point.getCodeElement(), exe));
                    if (ingredient.getCode() != null) {// && !contains(exps, ingredient)
                        ingredient.setDerivedFrom(exe);
                        exps.add(ingredient);
                    }
                }
            }
        }

        for (Ingredient in :base) {
            if (in.getCode().equals(point.getCodeElement()) || in.getChacheCodeString().equals(point.getCodeElement().toString().replaceAll("\\s+"," ")))
                continue;
            if (in.getCode() instanceof CtExpression && point.getCodeElement() instanceof CtExpression) {
                CtTypeReference a = ((CtExpression) in.getCode()).getType();
                CtTypeReference b = ((CtExpression) point.getCodeElement()).getType();
                if (a == null || b == null)
                    continue;
                if (!VariableResolver.areTypesCompatible(b, a) && !isCompatiblePrimitive(a, b)) {
                    continue;
                }
            } else if (!in.getCode().getClass().equals(point.getCodeElement().getClass())) {
                continue;
            }
            if (parent instanceof CtBlock) {
                if (in.getCode() instanceof CtStatement && in.getCode().getParent() instanceof CtBlock) {//discard super&this mthcall
                    in.setDerivedFrom(in.getCode());
                    exps.add(in);
                } else if (!(in.getCode().getParent() instanceof CtBlock)) {
                    continue;
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

            if (!(in.getCode() instanceof CtVariableRead)) {
                if ("target".equalsIgnoreCase(String.valueOf(point.getCodeElement().getRoleInParent()))
                        && in.getCode() instanceof CtLiteral)
                    continue;
                in.setDerivedFrom(in.getCode());
                exps.add(in);
            }
        }
        return exps;//deduplicate(exps);
    }

    boolean isCompatiblePrimitive(CtTypeReference element1, CtTypeReference element2) {
        if (element1 == null || element2 == null)
            return false;
        boolean isCompatiblePrimitive = element1.isPrimitive() && element2.isPrimitive() &&
                ((element1.getSimpleName().equalsIgnoreCase("boolean")
                        && element2.getSimpleName().equalsIgnoreCase("boolean")) ||
                        (!element1.getSimpleName().equalsIgnoreCase("boolean") &&
                                !element2.getSimpleName().equalsIgnoreCase("boolean")));
        return isCompatiblePrimitive;
    }

    int getArgPosition(CtExpression exp1, List<CtTypeReference<?>> para2) {
        CtTypeReference type1 = exp1.getType();
        if (type1 == null)
            return -1;
        return para2.indexOf(type1);
    }

    boolean isParasSame(List<CtTypeReference<?>> p1, List<CtExpression<?>> a2) {
        boolean flag = (p1.isEmpty() && a2.isEmpty()) || (p1.size() == a2.stream().filter(o -> o.getType() != null).count());
        if (!flag)
            return false;
        for (CtExpression arg :a2) {
            if (arg.getType() != null && !p1.contains(arg.getType()))
                return false;
        }
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
                baseElements = getstmts(modificationPoint, baseElements, operationType);

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
            Ingredient baseIngredient = baseElements.remove(idx);

            if (baseIngredient != null && baseIngredient.getCode() != null) {
                return baseIngredient;
            }

        } // End while

        log.debug("--- no mutation left to apply in element "
                + StringUtil.trunc(modificationPoint.getCodeElement().getShortRepresentation())
                + ", search space size: " + elementsFromFixSpace);
        return null;
    }


    public String getKey(ModificationPoint modPoint, AstorOperator operator) {
        String lockey = null;
        if (modPoint instanceof SuspiciousModificationPoint)//
            lockey = ((SuspiciousModificationPoint)modPoint).getSuspicious().getLineNumber() + "-"
                + modPoint.getCodeElement().toString().replaceAll("\\s+", " ") + "-"
//                + modPoint.getCodeElement().getParent() + "-"
                + operator.toString();
        else//
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
