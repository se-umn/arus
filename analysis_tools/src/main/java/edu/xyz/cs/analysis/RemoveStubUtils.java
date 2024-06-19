package edu.xyz.cs.analysis;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.printer.YamlPrinter;
import edu.xyz.cs.analysis.model.FixUnusedStubsState;
import java.io.*;
import com.github.javaparser.ast.type.Type;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class RemoveStubUtils {
    /////check if any of the methods is used in the file
    public static boolean isUseOutside(String fileName, List<String> methodnames) throws FileNotFoundException {
        boolean result = false;
        CompilationUnit cu = StaticJavaParser.parse(new File(fileName));
        List<MethodCallExpr> l = cu.findAll(MethodCallExpr.class);
            for(MethodCallExpr mce: l){
                if(methodnames.contains(mce.getName().getIdentifier())){
                    result = true;
                    break;
                }
            }
        return result;
    }

    /////////check if the any of the methods is static
    public static boolean isStatic(CompilationUnit cu, List<String> methodnames){
        boolean result = false;
        List<MethodDeclaration> l2 = cu.findAll(MethodDeclaration.class);
            for(MethodDeclaration md: l2){
                if(methodnames.contains(md.getName().getIdentifier())){
                    for(Modifier m: md.getModifiers()){
                        if(m.getKeyword().asString().equals("static")){
                            result = true;
                            }
                        }
                    }
        }
        return result;
    }

    /////////create test name util package
    public static void createPackage(String filePath){
        File file = new File(filePath);
        file.mkdir();

        String program = "package xyzutils;" +
                "public class TestNameUtil { " +
                    "public static String TEST_NAME = \"\";"+
                "}";
        try {
            // Creates a Writer using FileWriter
            FileWriter output = new FileWriter(filePath + "/TestNameUtil.java");

            // Writes the program to file
            output.write(program);
//            System.out.println("Data is written to the file.");

            // Closes the writer
            output.close();
        }
        catch (Exception e) {
            e.getStackTrace();
        }

    }
    ///////////change the function name in the statement at line number to the new name
    public static CompilationUnit changeName(CompilationUnit cu, int lineNum, String methodName, String newMethodName, boolean changeImport) throws IOException {
        List<ClassOrInterfaceDeclaration> ciList = cu.findAll(ClassOrInterfaceDeclaration.class);
        if(changeImport){
            NodeList<ImportDeclaration> importDeclarations= cu.getImports();
            String newImportName = "";
            for(ImportDeclaration id: importDeclarations){
                if(id.getName().asString().contains(methodName)){
                    newImportName = id.getName().toString().replace(methodName,newMethodName);
                    id.setName(newImportName);
                }
            }
        }

        for(ClassOrInterfaceDeclaration ci: ciList){
            if(ci.getExtendedTypes().size() != 0){
                for(ClassOrInterfaceType cit: ci.getExtendedTypes()){
                    if (cit.getName().getIdentifier().equals(methodName)){

                        cit.setName(newMethodName);
                    }
                }
            }
        }
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        //when we duplicate we also duplicate the line nums which may cause this method to change unnecessary statements
        for (MethodDeclaration m : methodDeclarations) {
            //check if the return type is the testclass (for duplicate test class)
            if(m.getType().toString().contains(methodName)){
                //it must be a class or interface because only in this way it can be return
                ClassOrInterfaceType type = (ClassOrInterfaceType) m.getType();
                type.setName(newMethodName);
            }
            for(Statement s: m.getBody().get().getStatements()){
                if(s.isForStmt()){
                    Statement body = ((ForStmt)s).getBody();
                    for(Statement statement: ((BlockStmt)body).getStatements()){
                        changeNameInStmt(statement,lineNum,methodName,newMethodName);
                    }
                }
                else{
                    changeNameInStmt(s,lineNum,methodName,newMethodName);
                }

            }
        }
        return cu;
    }

    /////change the function name at give line/statement to a new name
    public static void changeNameInStmt(Statement s, Integer lineNum, String methodName, String newMethodName){
        if (s.getBegin().isPresent() && isLineNumMatched(lineNum,s) || (lineNum == -1 && s.toString().contains(methodName)))  {
            Expression expression = null;
            if(s.isExpressionStmt()){
                expression = ((ExpressionStmt) s).getExpression();
            }
            else if(s.isReturnStmt()) {
                expression = ((ReturnStmt) s).getExpression().get();
            }

            if(expression!=null){
                if (expression.isMethodCallExpr()) {
//                    System.out.println("FIND AND CHANGE NAME");
                    findandChangeMethodName((MethodCallExpr) expression, methodName, newMethodName);

                } else if (expression.isVariableDeclarationExpr()) {
                    VariableDeclarator variable = ((VariableDeclarationExpr) expression).getVariable(0);
                    if (variable.getInitializer().get().isMethodCallExpr()) {
                        MethodCallExpr ex = (MethodCallExpr) variable.getInitializer().get();
                        findandChangeMethodName(ex, methodName, newMethodName);
                    } else {
                        System.out.println("No such VariableDeclarationExpr");
                    }

                }
                else if(expression.isAssignExpr()){
                    if(((AssignExpr)expression).getValue().isMethodCallExpr()){
                        MethodCallExpr ex = (MethodCallExpr) ((AssignExpr)expression).getValue();
                        findandChangeMethodName(ex, methodName, newMethodName);
                    }
                    else{
                        System.out.println("No such VariableDeclarationExpr");
                    }
                }
                else if (expression.isCastExpr()){
                    Type type = ((CastExpr) expression).getType();
                    if(type.isClassOrInterfaceType()){
                        if(((ClassOrInterfaceType)type).getName().getIdentifier().equals(methodName)){
                            ((ClassOrInterfaceType)type).setName(newMethodName);
                        }
                    }
                }
                else{
                    System.out.println("New structure to be considered for changing names");
                }
            }
        }
    }

    //////recursive helper function to change the function name in statement
    public static void findandChangeMethodName(MethodCallExpr mce, String methodName, String newMethodName) {
        if (mce == null) {
            return;
        }
        if (mce.getName().getIdentifier().equals(methodName)) {
            mce.setName(newMethodName);
            return;
        }
        Expression scope = mce.getScope().orElse(null);
        if (scope instanceof NameExpr && ((NameExpr) scope).getName().getIdentifier().equals(methodName)) {
            ((NameExpr) scope).setName(newMethodName);
            return;
        }
        else if(scope instanceof MethodCallExpr){
            findandChangeMethodName((MethodCallExpr) scope, methodName,newMethodName);
        }
        mce.getArguments().stream()
                .filter(Expression::isMethodCallExpr)
                .map(Expression::asMethodCallExpr)
                .forEach(expr -> findandChangeMethodName(expr, methodName, newMethodName));

        mce.getArguments().stream()
                .filter(Expression::isLambdaExpr)
                .map(Expression::asLambdaExpr)
                .forEach(lambdaExpr -> {
                    Statement s = lambdaExpr.getBody();
                    if(s.isExpressionStmt()){
                        Expression expr = ((ExpressionStmt) s).getExpression();{
                            if(expr.isMethodCallExpr()){
                                findandChangeMethodName((MethodCallExpr) expr, methodName, newMethodName);
                            }
                        }
                    }
                });

    }

    //////check if the test class contains any @Test methods
    public static boolean ifContainsTest(String source) throws FileNotFoundException {
        boolean ifContainsTest = false;
        CompilationUnit cu = StaticJavaParser.parse(new File(source));
        List<AnnotationExpr> annotationExprs = cu.findAll(AnnotationExpr.class);
        for(AnnotationExpr ae: annotationExprs){
            if(ae.getName().getIdentifier().equals("Test")){
                ifContainsTest = true;
                break;
            }
        }
        return ifContainsTest;
    }

    private static MethodCallExpr createMethodCallExpr(String methodName, Expression scope, String arg) {
        MethodCallExpr methodCallExpr = new MethodCallExpr();

        // set the name
        methodCallExpr.setName(new SimpleName("equals"));

        // set the scope
        methodCallExpr.setScope(scope);

        // set the arguments for the condition
        NodeList<Expression> arguments = new NodeList<>();
        arguments.add(new StringLiteralExpr(arg));
        methodCallExpr.setArguments(arguments);

        return methodCallExpr;
    }

    private static BinaryExpr createBinaryExpr(Expression left, Expression right) {
        BinaryExpr inner = new BinaryExpr();
        inner.setOperator(BinaryExpr.Operator.PLUS);
        inner.setRight(right);
        inner.setLeft(left);
        return inner;
    }

    ///////////insert if statement before the stub, used for solution B
    public static CompilationUnit insertIfStatement(CompilationUnit cu, List<List> testNameLists, List<List> lineNums, List<String> methodName, boolean finalBoolean, List<List>source, String fileName, FixUnusedStubsState fixUnusedStubsState) throws IOException {
        //add all statement except for return statement to newStatements
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        boolean usingTestNameUtil = true;
        for (MethodDeclaration md : methodDeclarations) {
                if(methodName.contains(md.getName().getIdentifier())){
                    BlockStmt body = md.getBody().get();
                    NodeList<Statement> statements = body.getStatements();

                    for (int j = 0; j < lineNums.size(); j++) {
                        //create if statement
                        IfStmt stmt = new IfStmt();

                        //create if conditions
                        BinaryExpr binaryExpr = new BinaryExpr();
                        binaryExpr.setOperator(BinaryExpr.Operator.OR);

                        //get the if conditions
                        List<MethodCallExpr> conditions = new ArrayList<>();
                        List<String> testNameList = testNameLists.get(j);

                        boolean isStatic = isStatic(cu, methodName);

                        for (int i = 0; i < testNameList.size(); i++) {
                            MethodCallExpr methodCallExpr;
                            if (finalBoolean) {
                                if (isStatic) {
                                    FieldAccessExpr scope = new FieldAccessExpr();
                                    scope.setName(new SimpleName("TEST_NAME"));
                                    scope.setScope(new NameExpr("TestNameUtil"));
                                    methodCallExpr = createMethodCallExpr("equals", scope, testNameList.get(i));

                                    conditions.add(methodCallExpr);
                                    cu.addImport("xyzutils.TestNameUtil");
                                } else {
                                    MethodCallExpr left = new MethodCallExpr();
                                    left.setName(new SimpleName("getMethodName"));
                                    left.setScope(new NameExpr("xyzTestName"));

                                    BinaryExpr inner = createBinaryExpr(left, new StringLiteralExpr((String) source.get(j).get(i)));

                                    methodCallExpr = createMethodCallExpr("equals", new EnclosedExpr(inner), testNameList.get(i));

                                    conditions.add(methodCallExpr);
                                    usingTestNameUtil = false;
                                }
                            } else {
                                if (ifContainsTest(fileName) || fixUnusedStubsState.isExtend()) {
                                    MethodCallExpr left = new MethodCallExpr();
                                    left.setName(new SimpleName("getMethodName"));
                                    left.setScope(new NameExpr("xyzTestName"));

                                    BinaryExpr inner = createBinaryExpr(left, new StringLiteralExpr((String) source.get(j).get(i)));

                                    methodCallExpr = createMethodCallExpr("equals", new EnclosedExpr(inner), testNameList.get(i));

                                    conditions.add(methodCallExpr);
                                    usingTestNameUtil = false;
                                } else {
                                    FieldAccessExpr scope = new FieldAccessExpr();
                                    scope.setName(new SimpleName("TEST_NAME"));
                                    scope.setScope(new NameExpr("TestNameUtil"));

                                    methodCallExpr = createMethodCallExpr("equals", scope, testNameList.get(i));

                                    conditions.add(methodCallExpr);
                                    cu.addImport("xyzutils.TestNameUtil");
                                }
                            }
                        }

                        //set the if condition
                        if (conditions.size() > 1) {
                            //if there are more than 1 condition, get the conditions and set them
                            binaryExpr = getLeftBinaryExpr(binaryExpr, conditions.subList(0, conditions.size() - 1));
                            binaryExpr.setRight(conditions.get(conditions.size() - 1));
                            stmt.setCondition(binaryExpr);
                        } else {
                            stmt.setCondition(conditions.get(0));
                        }

                        //get the statements to be put into the if branch
                        BlockStmt ifblock = new BlockStmt();

                        NodeList<Statement> toBeRemovedNodeList = new NodeList<>();
                        for (Statement s : statements) {
                            if (s.getBegin().isPresent()) {
                                if (s.isTryStmt()) {
                                    NodeList<Statement> statementNodeList = ((TryStmt) s).getTryBlock().getStatements();
                                    NodeList<Statement> innerToBeRemovedNodeList = new NodeList<>();
                                    for (Statement statement : statementNodeList) {
                                        if (statement.getBegin().isPresent()) {
                                            if (lineNums.get(j).contains(statement.getBegin().get().line) || lineNums.get(j).contains(statement.getEnd().get().line)) {
                                                if (statement.isExpressionStmt()) {
                                                    ifblock.addStatement(statement.toString());
                                                    innerToBeRemovedNodeList.add(statement);
                                                } else {
                                                    System.out.println("BLOCK InsertBegin is not ExpressionStmt");
                                                }
                                                stmt.setThenStmt(ifblock);
                                                statementNodeList.replace(statement, stmt);
                                            }
                                        }
                                    }
                                    stmt.setThenStmt(ifblock);
                                    if(!innerToBeRemovedNodeList.isEmpty()){
                                        Statement theLastStmt = innerToBeRemovedNodeList.get(innerToBeRemovedNodeList.size() - 1);
                                        statementNodeList.replace(theLastStmt, stmt);
                                        innerToBeRemovedNodeList.remove(theLastStmt);
                                    }

                                    if (!statementNodeList.isEmpty()) {
                                        for (Statement statementTobeRemoved : innerToBeRemovedNodeList) {
                                            statementNodeList.remove(statementTobeRemoved);
                                        }
                                    }
                                } else if (s.isForStmt()) {
                                    NodeList<Statement> statementNodeList = ((ForStmt) s).getBody().asBlockStmt().getStatements();
                                    NodeList<Statement> innerToBeRemovedNodeList = new NodeList<>();
                                    for (Statement statement : statementNodeList) {
                                        if (statement.getBegin().isPresent()) {
                                            if (lineNums.get(j).contains(statement.getBegin().get().line) || lineNums.get(j).contains(statement.getEnd().get().line)) {
                                                if (statement.isExpressionStmt()) {
                                                    ifblock.addStatement(statement.toString());
                                                    innerToBeRemovedNodeList.add(statement);
                                                } else {
                                                    System.out.println("BLOCK InsertBegin is not ExpressionStmt");
                                                }
                                            }
                                        }
                                    }
                                    stmt.setThenStmt(ifblock);
                                    Statement theLastStmt = innerToBeRemovedNodeList.get(innerToBeRemovedNodeList.size() - 1);
                                    statementNodeList.replace(theLastStmt, stmt);
                                    innerToBeRemovedNodeList.remove(theLastStmt);
                                    if (!statementNodeList.isEmpty()) {
                                        for (Statement statementTobeRemoved : innerToBeRemovedNodeList) {
                                            statementNodeList.remove(statementTobeRemoved);
                                        }
                                    }

                                }
                                else{
                                    if (lineNums.get(j).contains(s.getBegin().get().line) || lineNums.get(j).contains(s.getEnd().get().line)) {
                                        if (s.isExpressionStmt()) {
                                            ifblock.addStatement(s.toString());
                                            toBeRemovedNodeList.add(s);
                                        } else {
                                            System.out.println("InsertBegin is not ExpressionStmt");
                                            YamlPrinter printer = new YamlPrinter(true);
//                                            System.out.println(printer.output(s));
                                        }
                                    }
                                }
                            }
                        }
                        /////if the list is empty, it means the stub is either in for/try stmt
                        if(!toBeRemovedNodeList.isEmpty()){
                            stmt.setThenStmt(ifblock);
                            Statement theLastStmt = toBeRemovedNodeList.get(toBeRemovedNodeList.size() - 1);
                            statements.replace(theLastStmt, stmt);
                            toBeRemovedNodeList.remove(theLastStmt);
                        }
                        ///////if there are more than one stubs
                        if (!toBeRemovedNodeList.isEmpty()) {
                            for (Statement statementTobeRemoved : toBeRemovedNodeList) {
                                statements.remove(statementTobeRemoved);
                            }
                        }
                    }
                    body.setStatements(statements);
                }

        }
        if(usingTestNameUtil){
            cu = insertTestName(cu, fileName);
            fixUnusedStubsState.setNeedAddPackage(true);
        }
        return cu;
    }
    ////insert testname util in @Before method, if the file does not have @Before method then create it and add testname util
    public static CompilationUnit insertTestName(CompilationUnit cu, String source) throws IOException {
        cu.addImport("xyzutils.TestNameUtil");

        //create the expression for the Expression statement
        Expression assignExpr = new AssignExpr();
        ((AssignExpr) assignExpr).setOperator(AssignExpr.Operator.ASSIGN);

        //set target of the statement
        FieldAccessExpr fieldAccessExpr = new FieldAccessExpr();
        fieldAccessExpr.setName("TEST_NAME");
        NameExpr nameExpr = new NameExpr("TestNameUtil");
        fieldAccessExpr.setScope(nameExpr);
        ((AssignExpr) assignExpr).setTarget(fieldAccessExpr);

        //set the value of the statement which is a binaryExpr
        BinaryExpr binaryExpr = new BinaryExpr();
        binaryExpr.setOperator(BinaryExpr.Operator.PLUS);

        //set the left side of binaryExpr
        MethodCallExpr leftBinaryExpr = new MethodCallExpr();
        leftBinaryExpr.setName("getMethodName");
        NameExpr LeftScopeNameExpr = new NameExpr("xyzTestName");
        leftBinaryExpr.setScope(LeftScopeNameExpr);
        binaryExpr.setLeft(leftBinaryExpr);

        //set the right side of binaryExpr
        StringLiteralExpr RightBinaryExpr = new StringLiteralExpr(source);
        binaryExpr.setRight(RightBinaryExpr);

        ((AssignExpr) assignExpr).setValue(binaryExpr);

        //set the expression statement
        ExpressionStmt stmt = new ExpressionStmt();
        stmt.setExpression(assignExpr);

        NodeList<Statement> newStatements = new NodeList<>();
        newStatements.add(stmt);

        boolean hasBefore = false;
        List<ClassOrInterfaceDeclaration>list = cu.findAll(ClassOrInterfaceDeclaration.class);
        for(ClassOrInterfaceDeclaration l: list){
            if(source.contains(l.getName().getIdentifier())){
                List<MethodDeclaration> methodDeclarations = l.findAll(MethodDeclaration.class);
                //if it has @before method put the statement we just created into the before method.
                outerloop:
                for (MethodDeclaration md : methodDeclarations) {
                    for(AnnotationExpr anno: md.getAnnotations()){
                        if(anno.getName().getIdentifier().equals("Before") || md.getName().getIdentifier().equalsIgnoreCase("before")){
                            BlockStmt body = md.getBody().get();
                            for(Statement s: body.getStatements()){
                                if(s.toString().contains("TestNameUtil.TEST_NAME = xyzTestName.getMethodName()")){
                                    newStatements.remove(stmt);
                                }
                                newStatements.add(s);
                            }
                            md.getBody().get().setStatements(newStatements);
                            hasBefore = true;
                            break outerloop;
                        }

                    }
                }

                //if there is @Before method insert one;
                if(!hasBefore){
                    cu.addImport("org.junit.Before");
                    for (Node childNode : cu.getChildNodes()) {
                        if (childNode instanceof ClassOrInterfaceDeclaration) {
                            ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) childNode;
                            MethodDeclaration method = classOrInterfaceDeclaration.addMethod("xyzSetUp");
                            BlockStmt blockStmt = new BlockStmt();
                            blockStmt.setStatements(newStatements);
                            method.setBody(blockStmt);
                            NodeList<AnnotationExpr> annotationExprs = new NodeList<>();
                            NormalAnnotationExpr annotationExpr = new NormalAnnotationExpr();
                            annotationExpr.setName("Before");
                            annotationExprs.add(annotationExpr);
                            method.setAnnotations(annotationExprs);
                            method.setModifier(Modifier.Keyword.PUBLIC, true);
                            methodDeclarations.add(method);
                        }
                    }
                }
            }
        }
        return cu;
    }
    ///////////insert if statement before the stub, used for solution C
    public static CompilationUnit insertIfStatementWithNegateTestCases(CompilationUnit cu, List<List> testNameLists, List<List> lineNums, List<String> methodName, boolean finalBoolean, List<List>postfixList, String fileName, FixUnusedStubsState fixUnusedStubsState) throws IOException {
        //add all statement except for return statement to newStatements
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        boolean usingTestNameUtil = true;
        for (MethodDeclaration md : methodDeclarations) {
            if(methodName.contains(md.getName().getIdentifier())){
                BlockStmt body = md.getBody().get();
                NodeList<Statement> statements = body.getStatements();

                for (int j = 0; j < lineNums.size(); j++) {

                    //create if statement
                    IfStmt stmt = new IfStmt();

                    //create if conditions
                    BinaryExpr binaryExpr = new BinaryExpr();
                    binaryExpr.setOperator(BinaryExpr.Operator.AND);

                    //get the if conditions
                    List<UnaryExpr> conditions = new ArrayList<>();
                    List<String> testNameList = testNameLists.get(j);

                    boolean isStatic = isStatic(cu,methodName);


                    for (int i = 0; i < testNameList.size(); i++) {
                        if(finalBoolean){
                            if(isStatic){
                                //add the methodCallExpr to the conditions
                                UnaryExpr methodCallExpr = createMethodCallExpr(testNameList.get(i));
                                conditions.add(methodCallExpr);
                                cu.addImport("xyzutils.TestNameUtil");
                            }
                            else {
                                UnaryExpr methodCallExpr = createMethodCallExprWithPlus(testNameList.get(i), (String)postfixList.get(j).get(i));
                                //add the methodCallExpr to the conditions
                                conditions.add(methodCallExpr);
                                usingTestNameUtil = false;
                            }
                        }
                        else {
                            if((ifContainsTest(fileName) || fileName.contains("repos_tmp/jenkinsci_repository-connector-plugin_experiment/src/test/java/org/jvnet/hudson/plugins/repositoryconnector/AbstractArtifactTest.java"))|| fixUnusedStubsState.isExtend()){
//                                System.out.println(testNameList.get(i));
//                                System.out.println(postfixList.get(j).get(i));
                                UnaryExpr methodCallExpr = createMethodCallExprWithPlus(testNameList.get(i), (String)postfixList.get(j).get(i));
                                //add the methodCallExpr to the conditions
                                conditions.add(methodCallExpr);
                                usingTestNameUtil = false;

                            }else{
                                UnaryExpr methodCallExpr = createMethodCallExpr(testNameList.get(i));

                                //add the methodCallExpr to the conditions
                                conditions.add(methodCallExpr);
                                cu.addImport("xyzutils.TestNameUtil");
                            }
                        }
                    }
                    //set the if condition
                    if (conditions.size() > 1) {
                        //if there are more than 1 condition, get the conditions and set them
                        binaryExpr = getLeftBinaryExpr2(binaryExpr, conditions.subList(0, conditions.size() - 1));
                        binaryExpr.setRight(conditions.get(conditions.size() - 1));
                        stmt.setCondition(binaryExpr);
                    } else {
                        stmt.setCondition(conditions.get(0));
                    }

                    //get the statements to be put into the if branch
                    BlockStmt ifblock = new BlockStmt();

                    NodeList<Statement> toBeRemovedNodeList = new NodeList<>();
                    for (Statement s : statements) {
                        if (s.getBegin().isPresent()) {
                            if (s.isTryStmt()) {
                                NodeList<Statement> statementNodeList = ((TryStmt) s).getTryBlock().getStatements();
                                NodeList<Statement> innerToBeRemovedNodeList = new NodeList<>();
                                for (Statement statement : statementNodeList) {
                                    if (statement.getBegin().isPresent()) {
                                        if (lineNums.get(j).contains(statement.getBegin().get().line) || lineNums.get(j).contains(statement.getEnd().get().line)) {
                                            if (statement.isExpressionStmt()) {
                                                ifblock.addStatement(statement.toString());
                                                innerToBeRemovedNodeList.add(statement);
                                            } else {
                                                System.out.println("BLOCK InsertBegin is not ExpressionStmt");
                                            }
                                            stmt.setThenStmt(ifblock);
                                            statementNodeList.replace(statement, stmt);
                                        }
                                    }
                                }
                                stmt.setThenStmt(ifblock);
                                if (innerToBeRemovedNodeList.size()!= 0){
                                    Statement theLastStmt = innerToBeRemovedNodeList.get(innerToBeRemovedNodeList.size() - 1);
                                    statementNodeList.replace(theLastStmt, stmt);
                                    innerToBeRemovedNodeList.remove(theLastStmt);
                                }

                                if (!statementNodeList.isEmpty()) {
                                    for (Statement statementTobeRemoved : innerToBeRemovedNodeList) {
                                        statementNodeList.remove(statementTobeRemoved);
                                    }
                                }
                            } else if (s.isForStmt()) {
                                NodeList<Statement> statementNodeList = ((ForStmt) s).getBody().asBlockStmt().getStatements();
                                NodeList<Statement> innerToBeRemovedNodeList = new NodeList<>();
                                for (Statement statement : statementNodeList) {
                                    if (statement.getBegin().isPresent()) {
//                                            System.out.println(statement.getBegin().get().line + " " + statement.getEnd().get().line);
                                        if (lineNums.get(j).contains(statement.getBegin().get().line) || lineNums.get(j).contains(statement.getEnd().get().line)) {
                                            if (statement.isExpressionStmt()) {
                                                ifblock.addStatement(statement.toString());
                                                innerToBeRemovedNodeList.add(statement);
                                            } else {
                                                System.out.println("BLOCK InsertBegin is not ExpressionStmt");
                                            }
                                        }
                                    }
                                }
                                stmt.setThenStmt(ifblock);
                                Statement theLastStmt = innerToBeRemovedNodeList.get(innerToBeRemovedNodeList.size() - 1);
                                statementNodeList.replace(theLastStmt, stmt);
                                innerToBeRemovedNodeList.remove(theLastStmt);
                                if (!statementNodeList.isEmpty()) {
                                    for (Statement statementTobeRemoved : innerToBeRemovedNodeList) {
                                        statementNodeList.remove(statementTobeRemoved);
                                    }
                                }

                            }
                            else{
                                if (lineNums.get(j).contains(s.getBegin().get().line) || lineNums.get(j).contains(s.getEnd().get().line)) {
                                    if (s.isExpressionStmt()) {
                                        ifblock.addStatement(s.toString());
                                        toBeRemovedNodeList.add(s);
                                    } else {
                                        System.out.println("InsertBegin is not ExpressionStmt");
                                        YamlPrinter printer = new YamlPrinter(true);
                                        System.out.println(printer.output(s));
                                    }
                                }
                            }
                        }
                    }
                    /////if the list is empty, it means the stub is either in for/try stmt
                    if(!toBeRemovedNodeList.isEmpty()){
                        stmt.setThenStmt(ifblock);
                        Statement theLastStmt = toBeRemovedNodeList.get(toBeRemovedNodeList.size() - 1);
                        statements.replace(theLastStmt, stmt);
                        toBeRemovedNodeList.remove(theLastStmt);
                    }
                    ///////if there are more than one stubs
                    if (!toBeRemovedNodeList.isEmpty()) {
                        for (Statement statementTobeRemoved : toBeRemovedNodeList) {
                            statements.remove(statementTobeRemoved);
                        }
                    }
                }
                body.setStatements(statements);
            }

        }
        if(usingTestNameUtil){
//            System.out.println("Add Test Name Util in before");
            cu = insertTestName(cu, fileName);
            fixUnusedStubsState.setNeedAddPackage(true);
        }
        return cu;
    }

    ///////////duplicate test class, used for solution B
    public static void duplicateTestClasse(CompilationUnit cu, String oldFileName, String newFileName, String newFilePath, List<Integer> lineNums, List<String> stubbedMethodName, List<String> containsUSTestName, List<String> testsTobeRemoved) throws IOException {
//        File file = new File(newFilePath);
        CompilationUnit newCu = cu.clone();

        //handle originl cu
        //remove the tests and us
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        List<MethodDeclaration> ToBeMovedMehtodDeclarations = new ArrayList<>();
        List<MethodDeclaration> NotToBeMovedMehtodDeclarations = new ArrayList<>();

        for (MethodDeclaration md : methodDeclarations) {
            for(AnnotationExpr ate: md.getAnnotations()){
                if(ate.getName().asString().toLowerCase().equals("test")){
                    boolean contains = false;
                    for(String ta: testsTobeRemoved){
                        if (ta.equals(md.getName().getIdentifier())){
                            ToBeMovedMehtodDeclarations.add(md);
                            contains = true;
                            break;
                        }
                    }
                    if(!contains){
                        NotToBeMovedMehtodDeclarations.add(md);
                    }

                }
            }

        }
        for (Node childNode : cu.getChildNodes()) {
            if (childNode instanceof ClassOrInterfaceDeclaration) {
                List<Node> methodNodes = childNode.getChildNodes();
                List<Node> nodeindexList = new ArrayList<>();
                for(Node n: methodNodes){
                    for(MethodDeclaration md: ToBeMovedMehtodDeclarations){
                        //md can contain comment which will make md different with node
                        if(md.toString().contains(n.toString()) && n.toString().contains("@Test")){
                            nodeindexList.add(n);
                        }
                    }
                }
                if(!nodeindexList.isEmpty()){
                    for(Node n: nodeindexList){
                        childNode.remove(n);
                    }
                }
            }
        }

        //////////working on the new testclass file/////////////
        for (Node childNode : newCu.getChildNodes()) {
            if (childNode instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) childNode;
                if(oldFileName.equals(classOrInterfaceDeclaration.getName().getIdentifier())){
                    classOrInterfaceDeclaration.setName(newFileName);
                    List<Node> methodNodes = childNode.getChildNodes();
                    List<Node> nodeindexList = new ArrayList<>();
                    for(MethodDeclaration md: NotToBeMovedMehtodDeclarations){
                        for(Node n: methodNodes){
                            //md can contain comment which will make md different with node
                            if(md.toString().contains(n.toString()) && n.toString().contains("@Test")){
                                nodeindexList.add(n);
                            }
                        }
                    }
                    if(!nodeindexList.isEmpty()){
                        for(Node n: nodeindexList){
                            childNode.remove(n);
                        }
                    }
                }

            }
        }
        if(newFilePath.contains("vaulttec_sonar-auth-oidc_experiment/src/test/java/org/vaulttec/sonarqube/auth/oidc/twoOidcIdentityProviderTest.java")){
            List<MethodDeclaration> methodDeclarations1 = newCu.findAll(MethodDeclaration.class);
            for (MethodDeclaration md : methodDeclarations1) {
                if (containsUSTestName.contains(md.getName().getIdentifier())) {
                    BlockStmt blockStmt = md.getBody().get();
                    NodeList<Statement> oldbody = blockStmt.getStatements();
                    NodeList<Statement> newBody = new NodeList<>();
                    for(Statement stmt: oldbody){
                        if (isLineNumMatched(lineNums.get(0),stmt) || isLineNumMatched(lineNums.get(1),stmt)){
                        }
                        else{
                            newBody.add(stmt);
                        }
                    }
                    blockStmt.setStatements(newBody);
                }
            }
        }
        else{
//            System.out.println(lineNums+" "+stubbedMethodName+" "+containsUSTestName);
            removeStub(newCu,lineNums,stubbedMethodName,containsUSTestName);
        }



        newCu = changeName(newCu,-1,oldFileName,newFileName,false);
        try {
            // Creates a Writer using FileWriter
            FileWriter output = new FileWriter(newFilePath);

            // Writes the program to file
            output.write(newCu.toString());

            // Closes the writer
            output.close();
        }
        catch (Exception e) {
            e.getStackTrace();
        }

    }

    public static void duplicateOutsideTestClass(CompilationUnit cu, List<String> testsTobeRemoved, String newFilePath, String newFileName, String oldFileName, String newChangeName, String oldChangeName) throws IOException {
//        File file = new File(newFilePath);
        CompilationUnit newCu = cu.clone();

        //handle originl cu
        //remove the tests and us
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        List<MethodDeclaration> ToBeMovedMehtodDeclarations = new ArrayList<>();
        List<MethodDeclaration> NotToBeMovedMehtodDeclarations = new ArrayList<>();

        for (MethodDeclaration md : methodDeclarations) {
            for(AnnotationExpr ate: md.getAnnotations()){
                if(ate.getName().asString().toLowerCase().equals("test")){
                    if (testsTobeRemoved.contains(md.getName().getIdentifier())){
                        ToBeMovedMehtodDeclarations.add(md);
                    }
                    else{
                        NotToBeMovedMehtodDeclarations.add(md);
                    }
                }
            }

        }
        if(NotToBeMovedMehtodDeclarations.isEmpty()){
//            System.out.println("Is empty");
            cu = changeName(cu,-1,oldChangeName,newChangeName, true);
        }
        else{
//            System.out.println(ToBeMovedMehtodDeclarations.size());
//            System.out.println(NotToBeMovedMehtodDeclarations.size());
            for (Node childNode : cu.getChildNodes()) {
                if (childNode instanceof ClassOrInterfaceDeclaration) {
                    List<Node> methodNodes = childNode.getChildNodes();
                    List<Node> nodeindexList = new ArrayList<>();
                    for(Node n: methodNodes){
                        for(MethodDeclaration md: ToBeMovedMehtodDeclarations){
                            if(n.toString().equals(md.toString())){
//                                System.out.println(md.getName().getIdentifier());
                                nodeindexList.add(n);
                            }
                        }
                    }
                    if(!nodeindexList.isEmpty()){
                        for(Node n: nodeindexList){
                            childNode.remove(n);
                        }
                    }
                }
            }

            //////////working on the new testclass file/////////////
            for (Node childNode : newCu.getChildNodes()) {
                if (childNode instanceof ClassOrInterfaceDeclaration) {
                    ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) childNode;
                    if(oldFileName.equals(classOrInterfaceDeclaration.getName().getIdentifier())){
                        classOrInterfaceDeclaration.setName(newFileName);
                        List<Node> methodNodes = childNode.getChildNodes();
                        List<Node> nodeindexList = new ArrayList<>();
                        for(Node n: methodNodes){
                            for(MethodDeclaration md: NotToBeMovedMehtodDeclarations){
                                if(n.toString().equals(md.toString())){
                                    nodeindexList.add(n);
                                }
                            }
                        }
                        if(!nodeindexList.isEmpty()){
                            for(Node n: nodeindexList){
                                childNode.remove(n);
                            }
                        }
                    }

                }
            }

            newCu = changeName(newCu,-1,oldChangeName,newChangeName, true);
            try {
                // Creates a Writer using FileWriter
                FileWriter output = new FileWriter(newFilePath);

                // Writes the program to file
                output.write(newCu.toString());

                // Closes the writer
                output.close();
            }
            catch (Exception e){
                e.getStackTrace();
            }
        }
    }
////////////add empty test if all test in the file are moved to other test files
    public static void addEmptyTest(CompilationUnit cu){
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        for (Node childNode : cu.getChildNodes()) {
            if (childNode instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) childNode;
                MethodDeclaration method = classOrInterfaceDeclaration.addMethod("xyzEmptyTest");
                NodeList<AnnotationExpr> annotationExprs = new NodeList<>();
                MarkerAnnotationExpr annotationExpr = new MarkerAnnotationExpr();
                annotationExpr.setName("Test");
                annotationExprs.add(annotationExpr);
                method.setAnnotations(annotationExprs);
                method.setModifier(Modifier.Keyword.PUBLIC, true);
                methodDeclarations.add(method);
            }
        }
        MethodDeclaration methodDeclaration = new MethodDeclaration();
        for(MethodDeclaration md: methodDeclarations){
            if(md.getName().getIdentifier().equals("init")){
                methodDeclaration = md;
            }
        }
        for (Node childNode : cu.getChildNodes()) {
            if (childNode instanceof ClassOrInterfaceDeclaration) {
                List<Node> methodNodes = childNode.getChildNodes();
                List<Node> nodeindexList = new ArrayList<>();
                for(Node n: methodNodes){
                    if(n.toString().equals(methodDeclaration.toString())){
//                        System.out.println(methodDeclaration.getName().getIdentifier());
                        nodeindexList.add(n);
                    }
                }
                if(!nodeindexList.isEmpty()){
                    for(Node n: nodeindexList){
                        childNode.remove(n);
                    }
                }
            }
        }
    }

    ///////////check if the function is used in @before method
    public static boolean isInBeforeMethod(String file, String methodName) throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser.parse(new File(file));
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        for(MethodDeclaration md: methodDeclarations){
            if(md.getName().getIdentifier().equals(methodName)){
                for(AnnotationExpr ate: md.getAnnotations()){
                    if(ate.getName().asString().toLowerCase().equals("before")){
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    //////////get structure of the statement at lineNum, for debugging
    public static void getStructure(CompilationUnit cu, String methodName, int lineNum){
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        boolean usingTestNameUtil = true;
        for (MethodDeclaration md : methodDeclarations) {
            if(methodName.contains(md.getName().getIdentifier())){
                BlockStmt body = md.getBody().get();
                NodeList<Statement> statements = body.getStatements();
                for(Statement s: statements){
                    if(s.getBegin().get().line == lineNum){
                        YamlPrinter printer = new YamlPrinter(true);
//                        System.out.println(printer.output(s));
                    }
                }
            }
        }
    }

    ////helper function to check if the statement is at lineNum
    public static boolean isLineNumMatched(int lineNum, Statement s){
        if(lineNum<=s.getEnd().get().line && lineNum>=s.getBegin().get().line){
            return true;
        }
        else{
            return false;
        }
    }

    private static BinaryExpr getLeftBinaryExpr2(BinaryExpr binaryExpr, List<UnaryExpr> list) {

        if (list.size() == 1) {
            binaryExpr.setLeft(list.get(0));
            return binaryExpr;
        }
        BinaryExpr newBE = binaryExpr.clone().setRight(list.get(list.size() - 1));
        List<UnaryExpr> newList = list.subList(0, list.size() - 1);
        return binaryExpr.setLeft(getLeftBinaryExpr2(newBE, newList));
    }

    public static UnaryExpr createMethodCallExpr (String testName){
        UnaryExpr unaryExpr = new UnaryExpr();
        unaryExpr.setOperator(UnaryExpr.Operator.LOGICAL_COMPLEMENT);
        //set the condition which is a MethodCallExpr
        MethodCallExpr methodCallExpr = new MethodCallExpr();

        //set the name for the condition
        methodCallExpr.setName(new SimpleName("equals"));

        //set the scope for the condition
        FieldAccessExpr scope = new FieldAccessExpr();
        scope.setName(new SimpleName("TEST_NAME"));
        scope.setScope(new NameExpr("TestNameUtil"));
        methodCallExpr.setScope(scope);

        //set the aruguments for the condition
        NodeList<Expression> arguments = new NodeList<>();
        arguments.add(new StringLiteralExpr(testName));
        methodCallExpr.setArguments(arguments);

        unaryExpr.setExpression(methodCallExpr);
        return unaryExpr;

    }
    public static UnaryExpr createMethodCallExprWithPlus (String testName, String perfixName){
        UnaryExpr unaryExpr = new UnaryExpr();
        unaryExpr.setOperator(UnaryExpr.Operator.LOGICAL_COMPLEMENT);
        //set condition methodCallExpr
        MethodCallExpr methodCallExpr = new MethodCallExpr();

        //set the name
        methodCallExpr.setName(new SimpleName("equals"));

        //set the aruguments for the condition
        NodeList<Expression> arguments = new NodeList<>();
        arguments.add(new StringLiteralExpr(testName));
        methodCallExpr.setArguments(arguments);

        MethodCallExpr left = new MethodCallExpr();
        left.setName(new SimpleName("getMethodName"));
        left.setScope(new NameExpr("xyzTestName"));
        BinaryExpr inner = new BinaryExpr();
        inner.setOperator(BinaryExpr.Operator.PLUS);
        StringLiteralExpr nameExpr1 = new StringLiteralExpr(perfixName);
        inner.setRight(nameExpr1);
        inner.setLeft(left);
        EnclosedExpr scope = new EnclosedExpr(inner);
        methodCallExpr.setScope(scope);

        unaryExpr.setExpression(methodCallExpr);

        return unaryExpr;
    }


    public static List<String> checkIfExtend(String fileName) throws IOException {
        List<String> result = new ArrayList<>();
        CompilationUnit cu = StaticJavaParser.parse(new File(fileName));
        List<ClassOrInterfaceDeclaration> ciList = cu.findAll(ClassOrInterfaceDeclaration.class);
        for(ClassOrInterfaceDeclaration ci: ciList){
            if(ci.getExtendedTypes().size() != 0){
                for(ClassOrInterfaceType cit: ci.getExtendedTypes()){
                    result.add(cit.getName().getIdentifier());
                }
            }
        }

        return result;
    }

    public static List<String> ifIsImported(Set<String> files, String targetFile) throws FileNotFoundException {
        List<String> filesImportTargetFile = new ArrayList<>();
        for(String filePath: files){
            if(ifIsImported(filePath, targetFile)){
                filesImportTargetFile.add(filePath);
            }

        }
        return filesImportTargetFile;
    }


    public static boolean ifIsImported(String filePath, String targetFile) throws FileNotFoundException {
        boolean isImported = false;
        if(new File(filePath).exists()){
            CompilationUnit cu = StaticJavaParser.parse(new File(filePath));
            NodeList<ImportDeclaration> importDeclarations=  cu.getImports();
            for(ImportDeclaration id: importDeclarations){
                if(id.getName().asString().contains(targetFile)){
                    return true;
                }
            }
        }
        return isImported;
    }


    public static int ifMoreThanOnce(String source, String methodName, String stubMethodName) throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser.parse(new File(source));

        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        int count = 0;
        for (MethodDeclaration md : methodDeclarations) {
            if (md.getName().getIdentifier().equals(methodName)) {
                NodeList<Statement> statements = md.getBody().get().getStatements();
                for(Statement s: statements){
                    if(s.toString().contains(stubMethodName)){
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static BinaryExpr getLeftBinaryExpr(BinaryExpr binaryExpr, List<MethodCallExpr> list) {

        if (list.size() == 1) {
            binaryExpr.setLeft(list.get(0));
            return binaryExpr;
        }
        BinaryExpr newBE = binaryExpr.clone().setRight(list.get(list.size() - 1));
        List<MethodCallExpr> newList = list.subList(0, list.size() - 1);
        return binaryExpr.setLeft(getLeftBinaryExpr(newBE, newList));
    }

    public static CompilationUnit changeNameInMethod(CompilationUnit cu, String methodName, String oldName, String newName){
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        for (MethodDeclaration md : methodDeclarations) {
            if (md.getName().getIdentifier().equals(methodName)) {
                BlockStmt statements = md.getBody().get();
                BlockStmt newStatement = new BlockStmt();
                for (Statement s : statements.getStatements()) {
                    Statement newS = s.clone();
                    changeNameInStmt(newS,-1,oldName,newName );
                    newStatement.addStatement(newS);
                }
                md.setBody(newStatement);
            }
        }
        return cu;
    }

    public static CompilationUnit duplicateMethod(CompilationUnit cu, String methodName, List<Integer> removeLineNums, String newMethodName, boolean isChanging, List<Integer> changeNamelineNums, String changeMethodName, String newName) throws IOException {
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        for (MethodDeclaration md : methodDeclarations) {
            if(md.getName().getIdentifier().equals(newMethodName)){
                return cu;
            }
        }
        for (MethodDeclaration md : methodDeclarations) {
            if(md.getName().getIdentifier().equals(newMethodName)){
                return cu;
            }
            else if (md.getName().getIdentifier().equals(methodName)) {
                int begin = md.getBegin().get().line;
                int end = md.getEnd().get().line;
                if ((removeLineNums.get(0) > begin && removeLineNums.get(0) < end) || removeLineNums.get(0) == 0) {
                    for (Node childNode : cu.getChildNodes()) {
                        if (childNode instanceof ClassOrInterfaceDeclaration) {
                            ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) childNode;
                            MethodDeclaration method = classOrInterfaceDeclaration.addMethod(newMethodName);
                            method.setModifiers(md.getModifiers());
                            //set parameters
                            method.setParameters(md.getParameters());
                            // set return type
                            method.setType(md.getType());
                            //set body
                            BlockStmt statements = md.getBody().get();
                            BlockStmt newStatement = new BlockStmt();
                            for (Statement s : statements.getStatements()) {
                                Statement newS = s.clone();
                                boolean isTheRemoveLine = false;
                                for(Integer num: removeLineNums){
                                    if (isLineNumMatched(num,s)) {
                                        isTheRemoveLine = true;
                                        break;
                                    }
                                }
                                boolean isTheChangeLine = false;
                                for(Integer num: changeNamelineNums){
                                    if (isLineNumMatched(num,s)) {
                                        isTheChangeLine = true;
                                        break;
                                    }
                                }
                                if(isTheRemoveLine){
                                    if(newS.isTryStmt()){
                                        Statement tryBlock = ((TryStmt) newS).getTryBlock();
                                        NodeList<Statement> blockStmt = new NodeList<>();
                                        for(Statement statement1: ((BlockStmt)tryBlock).getStatements()){
                                            boolean needRemove = false;
                                            for(Integer i: removeLineNums){
                                                if(isLineNumMatched(i,statement1)){
                                                    needRemove = true;
                                                    break;
                                                }
                                            }
                                            if(!needRemove){
//                                              System.out.println(statement1+" "+statement1.getBegin().get().line + " "+statement1.getEnd().get().line);
                                                blockStmt.add(statement1);
                                            }
                                        }
                                        ((BlockStmt)tryBlock).setStatements(blockStmt);
                                        ((TryStmt) newS).setTryBlock((BlockStmt)tryBlock);
                                        newStatement.addStatement(newS);
                                    }
                                    else if(newS.isForStmt()){
                                        NodeList<Statement> blockStmt = new NodeList<>();
                                        BlockStmt newBlockStatement = new BlockStmt();
                                        Statement body = ((ForStmt)newS).getBody();
                                        for(Statement statement1: ((BlockStmt)body).getStatements()){
                                            boolean needRemove = false;
                                            for(Integer i: removeLineNums){
                                                if(isLineNumMatched(i,statement1)){
                                                    needRemove = true;
                                                    break;
                                                }
                                            }
                                            if(!needRemove){
//                                              System.out.println(statement1+" "+statement1.getBegin().get().line + " "+statement1.getEnd().get().line);
                                                blockStmt.add(statement1);
                                            }
                                            else{
                                                if(statement1.isIfStmt()){
                                                    NodeList<Statement> IfblockStmt = new NodeList<>();
                                                    BlockStmt newIFBlockStatement = new BlockStmt();
                                                    Statement ifBody = ((IfStmt)statement1).getThenStmt();
                                                    for(Statement stmt: ((BlockStmt)ifBody).getStatements()){
                                                        boolean needRemoveIF = false;
                                                        for(Integer i: removeLineNums){
                                                            if(isLineNumMatched(i,stmt)){
                                                                needRemoveIF = true;
                                                                break;
                                                            }
                                                        }
                                                        if(!needRemoveIF){
//                                              System.out.println(statement1+" "+statement1.getBegin().get().line + " "+statement1.getEnd().get().line);
                                                            IfblockStmt.add(stmt);
                                                        }
                                                    }
                                                    newIFBlockStatement.setStatements(IfblockStmt);
                                                    ((IfStmt) statement1).setThenStmt(newIFBlockStatement);
                                                    blockStmt.add(statement1);

                                                }
                                            }
                                        }
                                        newBlockStatement.setStatements(blockStmt);
                                        ((ForStmt) newS).setBody(newBlockStatement);
                                        newStatement.addStatement(newS);
                                    }
                                    else if(newS.isIfStmt()){
                                        NodeList<Statement> blockStmt = new NodeList<>();
                                        BlockStmt newBlockStatement = new BlockStmt();
                                        Statement body = ((IfStmt)newS).getThenStmt();
                                        for(Statement statement1: ((BlockStmt)body).getStatements()){
                                            boolean needRemove = false;
                                            for(Integer i: removeLineNums){
                                                if(isLineNumMatched(i,statement1)){
                                                    needRemove = true;
                                                    break;
                                                }
                                            }
                                            if(!needRemove){
//                                              System.out.println(statement1+" "+statement1.getBegin().get().line + " "+statement1.getEnd().get().line);
                                                blockStmt.add(statement1);
                                            }
                                        }
                                        newBlockStatement.setStatements(blockStmt);
                                        ((IfStmt) newS).setThenStmt(newBlockStatement);
                                        newStatement.addStatement(newS);
                                    }
                                }
                                else if(isChanging){
                                    if(isTheChangeLine){
                                        if(newS.isForStmt()){
                                            Statement body = ((ForStmt)newS).getBody();
                                            for(Statement statement1: ((BlockStmt)body).getStatements()){
                                                boolean needChange = false;
                                                for(Integer i: changeNamelineNums){
                                                    if(isLineNumMatched(i,statement1)){
                                                        needChange = true;
                                                        break;
                                                    }
                                                }
                                                if(needChange){
//                                                    System.out.println(statement1+" "+statement1.getBegin().get().line + " "+statement1.getEnd().get().line);
                                                    Expression expression = ((ExpressionStmt) statement1).getExpression();
                                                    if (expression.isMethodCallExpr()) {
                                                        findandChangeMethodName((MethodCallExpr) expression, changeMethodName, newName);

                                                    } else if (expression.isVariableDeclarationExpr()) {
                                                        VariableDeclarator variable = ((VariableDeclarationExpr) expression).getVariable(0);
//                                                        System.out.println("variableName" + ((MethodCallExpr) variable.getInitializer().get()).getName().getIdentifier());
                                                        if (variable.getName().getIdentifier().equals(changeMethodName)) {
                                                            variable.setName(newName);
                                                        } else if (variable.getInitializer().get().isMethodCallExpr()) {
                                                            MethodCallExpr ex = (MethodCallExpr) variable.getInitializer().get();
                                                            findandChangeMethodName(ex, changeMethodName, newName);
                                                        } else {
                                                            System.out.println("No such VariableDeclarationExpr");
                                                        }

                                                    }
                                                    else if(expression.isAssignExpr()){
                                                        Expression mde = ((AssignExpr) expression).getValue();
                                                        if(mde.isMethodCallExpr()){
                                                            findandChangeMethodName((MethodCallExpr) mde, changeMethodName, newName);
                                                        }
                                                        else{
                                                            System.out.println("AssignExpr 's value is not MethodCallExpr");
                                                        }
                                                    }
                                                    else{
                                                        System.out.println("New expression type needs to be handled");
                                                    }
                                                }
                                            }
                                        }
                                        else if(newS.isTryStmt()){
                                            Statement body = ((TryStmt)newS).getTryBlock();
                                            for(Statement statement1: ((BlockStmt)body).getStatements()){
                                                boolean needChange = false;
                                                for(Integer i: changeNamelineNums){
                                                    if(isLineNumMatched(i,statement1)){
                                                        needChange = true;
                                                        break;
                                                    }
                                                }
                                                if(needChange){
//                                                    System.out.println(statement1+" "+statement1.getBegin().get().line + " "+statement1.getEnd().get().line);
                                                    Expression expression = ((ExpressionStmt) statement1).getExpression();
                                                    if (expression.isMethodCallExpr()) {
                                                        findandChangeMethodName((MethodCallExpr) expression, changeMethodName, newName);

                                                    } else if (expression.isVariableDeclarationExpr()) {
                                                        VariableDeclarator variable = ((VariableDeclarationExpr) expression).getVariable(0);
//                                                        System.out.println("variableName" + ((MethodCallExpr) variable.getInitializer().get()).getName().getIdentifier());
                                                        if (variable.getName().getIdentifier().equals(changeMethodName)) {
                                                            variable.setName(newName);
                                                        } else if (variable.getInitializer().get().isMethodCallExpr()) {
                                                            MethodCallExpr ex = (MethodCallExpr) variable.getInitializer().get();
                                                            findandChangeMethodName(ex, changeMethodName, newName);
                                                        } else {
                                                            System.out.println("No such VariableDeclarationExpr");
                                                        }

                                                    }
                                                    else if(expression.isAssignExpr()){
                                                        Expression mde = ((AssignExpr) expression).getValue();
                                                        if(mde.isMethodCallExpr()){
                                                            findandChangeMethodName((MethodCallExpr) mde, changeMethodName, newName);
                                                        }
                                                        else{
                                                            System.out.println("AssignExpr 's value is not MethodCallExpr");
                                                        }
                                                    }
                                                    else{
                                                        System.out.println("New expression type needs to be handled");
                                                    }
                                                }
                                            }
                                        }
                                        else{
                                            Expression expression = ((ExpressionStmt) newS).getExpression();
                                            if (expression.isMethodCallExpr()) {
                                                findandChangeMethodName((MethodCallExpr) expression, changeMethodName, newName);

                                            } else if (expression.isVariableDeclarationExpr()) {
                                                VariableDeclarator variable = ((VariableDeclarationExpr) expression).getVariable(0);
//                                                System.out.println("variableName" + ((MethodCallExpr) variable.getInitializer().get()).getName().getIdentifier());
                                                if (variable.getName().getIdentifier().equals(changeMethodName)) {
                                                    variable.setName(newName);
                                                } else if (variable.getInitializer().get().isMethodCallExpr()) {
                                                    MethodCallExpr ex = (MethodCallExpr) variable.getInitializer().get();
                                                    findandChangeMethodName(ex, changeMethodName, newName);
                                                } else {
                                                    System.out.println("No such VariableDeclarationExpr");
                                                }

                                            }
                                            else if(expression.isAssignExpr()){
                                                Expression mde = ((AssignExpr) expression).getValue();
                                                if(mde.isMethodCallExpr()){
                                                    findandChangeMethodName((MethodCallExpr) mde, changeMethodName, newName);
                                                }
                                                else{
                                                    System.out.println("AssignExpr 's value is not MethodCallExpr");
                                                }
                                            }
                                            else{
                                                System.out.println("New expression type needs to be handled");
                                            }

//                                        newStatement.addStatement(newS);
                                        }
                                        newStatement.addStatement(newS);
                                    }
                                    else{
                                        newStatement.addStatement(newS);
                                    }
                                }
                                else{
                                    newStatement.addStatement(newS);
                                }
                            }
                            method.setBody(newStatement);
                            //set ThrowException
                            if (md.getThrownExceptions().size() != 0) {
                                for (ReferenceType r : md.getThrownExceptions()) {
                                    method.addThrownException(r);
                                }

                            }
                        }
                    }
                }
            }

        }
        return cu;
    }


    public static CompilationUnit removeStub(CompilationUnit cu, List<Integer> lineNums, List<String> stubbedmethodNames, List<String> methodName){
        for (int i = 0; i < lineNums.size(); i++) {
            cu = removeStub(cu, lineNums.get(i),stubbedmethodNames.get(i), methodName.get(i));
        }
        return cu;
    }
    public static CompilationUnit removeStub(CompilationUnit cu, Integer lineNum, String stubbedMethodName, String testName){
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        for (MethodDeclaration md : methodDeclarations) {
            if (testName.contains(md.getName().getIdentifier()) || md.getName().getIdentifier().contains(testName)) {
                List<ExpressionStmt> stmts = md.findAll(ExpressionStmt.class);
                for(ExpressionStmt stmt: stmts){
//                    System.out.println(stmt.toString()+" "+ stmt.getBegin().get().line);
                    if (isLineNumMatched(lineNum,stmt)){
                        if(stmt.toString().contains(stubbedMethodName)){
                            Node parentNode = stmt.getParentNode().get();
                            List<Node> nodeList = parentNode.getChildNodes();
                            for(Node node: nodeList){
                                if(node.toString().contains(stmt.toString())){
                                    parentNode.remove(node);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        return cu;
    }

    public static boolean ifInLoop(String source, Integer lineNum, String methodName) throws FileNotFoundException {
        boolean result = false;
        CompilationUnit cu = StaticJavaParser.parse(new File(source));

        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);

        for (MethodDeclaration md : methodDeclarations) {
            if (md.getName().getIdentifier().equals(methodName)) {
                int begin = md.getBegin().get().line;
                int end = md.getEnd().get().line;
                if ((lineNum > begin && lineNum < end)) {
                    //set body
                    BlockStmt statements = md.getBody().get();
                    for (Statement s : statements.getStatements()) {
                        if (s.isForStmt()) {
                            Statement body = ((ForStmt) s).getBody();
                            for(Statement statement: ((BlockStmt)body).getStatements()){
                                if(lineNum == statement.getBegin().get().line) {
                                    result = true;
                                    break;
                                }
                                else if(statement.isIfStmt()){
                                    Statement ifBlock = ((IfStmt) statement).getThenStmt();
                                    for(Statement statement1: ((BlockStmt)ifBlock).getStatements()){
                                        if(lineNum == statement1.getBegin().get().line) {
                                            result = true;
                                            break;
                                        }
                                    }
                                }
                                else if(statement.isTryStmt()){
                                    Statement ifBlock = ((TryStmt) statement).getTryBlock();
                                    for(Statement statement1: ((BlockStmt)ifBlock).getStatements()){
                                        if(lineNum == statement1.getBegin().get().line) {
                                            result = true;
                                            break;
                                        }
                                    }
                                }

                            }
                        }
                        else if (s.isWhileStmt()) {
                            Statement body = ((WhileStmt) s).getBody();
                            for(Statement statement: ((BlockStmt)body).getStatements()){
                                if(lineNum == statement.getBegin().get().line) {
                                    result = true;
                                    break;
                                }
                                else if(statement.isIfStmt()){
                                    Statement ifBlock = ((IfStmt) statement).getThenStmt();
                                    for(Statement statement1: ((BlockStmt)ifBlock).getStatements()){
                                        if(lineNum == statement1.getBegin().get().line) {
                                            result = true;
                                            break;
                                        }
                                    }
                                }
                                else if(statement.isTryStmt()){
                                    Statement ifBlock = ((TryStmt) statement).getTryBlock();
                                    for(Statement statement1: ((BlockStmt)ifBlock).getStatements()){
                                        if(lineNum == statement1.getBegin().get().line) {
                                            result = true;
                                            break;
                                        }
                                    }
                                }

                            }
                        }
                        else if (s.isForEachStmt()) {
                            Statement body = ((ForEachStmt) s).getBody();
                            for(Statement statement: ((BlockStmt)body).getStatements()){
                                if(lineNum == statement.getBegin().get().line) {
                                    result = true;
                                    break;
                                }
                                else if(statement.isIfStmt()){
                                    Statement ifBlock = ((IfStmt) statement).getThenStmt();
                                    for(Statement statement1: ((BlockStmt)ifBlock).getStatements()){
                                        if(lineNum == statement1.getBegin().get().line) {
                                            result = true;
                                            break;
                                        }
                                    }
                                }
                                else if(statement.isTryStmt()){
                                    Statement ifBlock = ((TryStmt) statement).getTryBlock();
                                    for(Statement statement1: ((BlockStmt)ifBlock).getStatements()){
                                        if(lineNum == statement1.getBegin().get().line) {
                                            result = true;
                                            break;
                                        }
                                    }
                                }

                            }
                        }
                        else if (s.isDoStmt()) {
                            Statement body = ((DoStmt) s).getBody();
                            for(Statement statement: ((BlockStmt)body).getStatements()){
                                if(lineNum == statement.getBegin().get().line) {
                                    result = true;
                                    break;
                                }
                                else if(statement.isIfStmt()){
                                    Statement ifBlock = ((IfStmt) statement).getThenStmt();
                                    for(Statement statement1: ((BlockStmt)ifBlock).getStatements()){
                                        if(lineNum == statement1.getBegin().get().line) {
                                            result = true;
                                            break;
                                        }
                                    }
                                }
                                else if(statement.isTryStmt()){
                                    Statement ifBlock = ((TryStmt) statement).getTryBlock();
                                    for(Statement statement1: ((BlockStmt)ifBlock).getStatements()){
                                        if(lineNum == statement1.getBegin().get().line) {
                                            result = true;
                                            break;
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}

