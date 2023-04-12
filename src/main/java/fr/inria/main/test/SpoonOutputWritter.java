package fr.inria.main.test;

import spoon.compiler.Environment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;

import java.io.File;

public class SpoonOutputWritter {

    private JavaOutputProcessor javaPrinter;

    private Factory factory;

    public static final String CLASS_EXT = ".class";

    public SpoonOutputWritter(Factory factory) {
        super();
        this.factory = factory;
    }

    public void updateOutput(String output) {
        getEnvironment().setSourceOutputDirectory(new File(output));
        JavaOutputProcessor fileOutput = new JavaOutputProcessor(new DefaultJavaPrettyPrinter(getEnvironment()));
        fileOutput.setFactory(getFactory());

        this.javaPrinter = fileOutput;
    }

    public void saveSourceCode(CtClass element) {
        this.getEnvironment().setCommentEnabled(true);
        this.getEnvironment().setPreserveLineNumbers(false);
        if (javaPrinter == null) {
            throw new IllegalArgumentException("Java printer is null");
        }
        if (!element.isTopLevel()) {
            return;
        }
        // Create Java code and create ICompilationUnit
        try {
            javaPrinter.getCreatedFiles().clear();
            javaPrinter.process(element);
        } catch (Exception e) {
            System.err.println("Error saving ctclass " + element.getQualifiedName());
        }

    }

    public Environment getEnvironment() {
        return this.getFactory().getEnvironment();
    }

    /**
     * Gets the associated factory.
     */

    public Factory getFactory() {
        return this.factory;
    }

    public JavaOutputProcessor getJavaPrinter() {
        return javaPrinter;
    }

    public void setJavaPrinter(JavaOutputProcessor javaPrinter) {
        this.javaPrinter = javaPrinter;
    }
}