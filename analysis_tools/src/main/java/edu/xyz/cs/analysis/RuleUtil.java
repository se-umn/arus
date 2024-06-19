package edu.xyz.cs.analysis;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class RuleUtil {

    public static void addTestNameRules(String FilePath, boolean containsTest) throws IOException {
        CompilationUnit cu = StaticJavaParser.parse(new File(FilePath));
        LexicalPreservingPrinter.setup(cu);
        boolean  isStatic = false;
        cu.addImport("org.junit.rules.TestName");
        if(!containsTest){
            cu.addImport("org.junit.Rule");
        }
        ClassOrInterfaceDeclaration classDeclarationContainingTest = null;
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        for(MethodDeclaration methodDeclaration:methodDeclarations){
//      if(methodDeclaration.getName().getIdentifier().equals(methodName)){
//        for(Modifier m: methodDeclaration.getModifiers()) {
//          if(m.getKeyword().equals(Modifier.Keyword.STATIC)){
//            isStatic = true;
//          }
//        }
//      }
            for(AnnotationExpr annotationExpr:methodDeclaration.getAnnotations()){
                if(annotationExpr.getName().asString().equals("Test") || !containsTest){
                    Optional<Node> optionalNode = methodDeclaration.getParentNode();
                    while(optionalNode.isPresent()) {
                        Node node = optionalNode.get();
                        if(node instanceof ClassOrInterfaceDeclaration){
                            classDeclarationContainingTest = (ClassOrInterfaceDeclaration) node;
                            break;
                        }
                        else{
                            optionalNode = node.getParentNode();;
                        }
                    }
                    if(classDeclarationContainingTest!=null){
                        break;
                    }
                }
            }
        }

        if(classDeclarationContainingTest!=null ){

//      //check if class declaration has already a mockito rule
            boolean hasMockitoRuleAnnotation = false;
            List<FieldDeclaration> fieldDeclarations = classDeclarationContainingTest.findAll(FieldDeclaration.class);
            for(FieldDeclaration fieldDeclaration:fieldDeclarations) {
//        NodeList<AnnotationExpr> fieldAnnotations = fieldDeclaration.getAnnotations();
                NodeList<VariableDeclarator> fieldDeclarationVariables = fieldDeclaration.getVariables();
//        boolean hasRuleAnnotation = false;
                for (int i = 0; i < fieldDeclarationVariables.size(); i++) {
//          System.out.println(fieldDeclarationVariables.get(i).getBegin().get().line);
                    if (fieldDeclarationVariables.get(i).getNameAsString().equals("xyzTestName")) {
//            hasRuleAnnotation = true;
                        hasMockitoRuleAnnotation = true;
                        break;
                    }
                }
            }
            if(!hasMockitoRuleAnnotation) {
                final boolean isStaticFinal = isStatic;

                String split[] = FilePath.split("/");
                String fileName = split[split.length-1];
                String className = fileName.substring(0, fileName.length()-5);
                //add annotation and attributes
                cu.getClassByName(className).ifPresent(coid -> {
                    VariableDeclarator variables = new VariableDeclarator();
                    variables.setName("xyzTestName");
                    variables.setType("TestName");
                    variables.setInitializer(new NameExpr("new TestName()"));
                    FieldDeclaration fieldDeclaration = new FieldDeclaration().addVariable(variables);
                    if(isStaticFinal){
                        fieldDeclaration.addModifier(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
                    }
                    else{
                        fieldDeclaration.addModifier(Modifier.Keyword.PUBLIC);
                    }
                    fieldDeclaration.addAnnotation(new MarkerAnnotationExpr("Rule"));
                    coid.getMembers().add(0, fieldDeclaration);
                });
            }
        }
        String  result = LexicalPreservingPrinter.print(cu);
        Files.write(new File(FilePath).toPath(), Collections.singleton(result), StandardCharsets.UTF_8);
    }

}
