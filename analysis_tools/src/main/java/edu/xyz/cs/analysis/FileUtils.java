package edu.xyz.cs.analysis;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.YamlPrinter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import edu.xyz.cs.analysis.model.Flag;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class FileUtils {

  public static boolean replaceFile(String repoName, String javaFileName, String sourceLocation) throws IOException {
    boolean result = false;
    if(repoName.equals("apache/sling-org-apache-sling-commons-threads") && javaFileName.endsWith("ThreadPoolExecutorCleaningThreadLocalsTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"apache_sling-org-apache-sling-commons-threads"+File.separator+"ThreadPoolExecutorCleaningThreadLocalsTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("ben-xo/cdjscrobbler") && javaFileName.endsWith("ComboConfigTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"ben-xo_cdjscrobbler"+File.separator+"ComboConfigTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("ben-xo/cdjscrobbler") && javaFileName.endsWith("SongDetailsTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"ben-xo_cdjscrobbler"+File.separator+"SongDetailsTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/chucknorris-plugin") && javaFileName.endsWith("BeardDescriptorTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_chucknorris-plugin"+File.separator+"BeardDescriptorTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/chucknorris-plugin") && javaFileName.endsWith("CordellWalkerRecorderTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_chucknorris-plugin"+File.separator+"CordellWalkerRecorderTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/chucknorris-plugin") && javaFileName.endsWith("FactGeneratorTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_chucknorris-plugin"+File.separator+"FactGeneratorTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/chucknorris-plugin") && javaFileName.endsWith("RoundhouseActionTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_chucknorris-plugin"+File.separator+"RoundhouseActionTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/chucknorris-plugin") && javaFileName.endsWith("StyleTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_chucknorris-plugin"+File.separator+"StyleTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("garbagemule/MobArena") && javaFileName.endsWith("SignReaderTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"garbagemule_MobArena"+File.separator+"SignReaderTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("garbagemule/MobArena") && javaFileName.endsWith("SignWriterTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"garbagemule_MobArena"+File.separator+"SignWriterTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/hashicorp-vault-plugin") && javaFileName.endsWith("VaultCertificateCredentialsIT.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_hashicorp-vault-plugin"+File.separator+"VaultCertificateCredentialsIT.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/hashicorp-vault-plugin") && javaFileName.endsWith("VaultConfigurationIT.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_hashicorp-vault-plugin"+File.separator+"VaultConfigurationIT.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/hashicorp-vault-plugin") && javaFileName.endsWith("VaultGCRLoginIT.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_hashicorp-vault-plugin"+File.separator+"VaultGCRLoginIT.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/hashicorp-vault-plugin") && javaFileName.endsWith("VaultSSHUserPrivateKeyIT.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_hashicorp-vault-plugin"+File.separator+"VaultSSHUserPrivateKeyIT.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/hashicorp-vault-plugin") && javaFileName.endsWith("VaultTokenCredentialBindingIT.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_hashicorp-vault-plugin"+File.separator+"VaultTokenCredentialBindingIT.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/hashicorp-vault-plugin") && javaFileName.endsWith("VaultUsernamePasswordCredentialIT.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_hashicorp-vault-plugin"+File.separator+"VaultUsernamePasswordCredentialIT.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/hashicorp-vault-plugin") && javaFileName.endsWith("VaultBindingStepWithMockAccessor.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_hashicorp-vault-plugin"+File.separator+"VaultBindingStepWithMockAccessor.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/android-emulator-plugin") && javaFileName.endsWith("MonkeyRecorderTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_android-emulator-plugin"+File.separator+"MonkeyRecorderTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("octavian-h/time-series-math") && javaFileName.endsWith("DiscreteCosineTransformTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"octavian-h_time-series-math"+File.separator+"DiscreteCosineTransformTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("octavian-h/time-series-math") && javaFileName.endsWith("ZNormalizerTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"octavian-h_time-series-math"+File.separator+"ZNormalizerTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("octavian-h/time-series-math") && javaFileName.endsWith("DiscreteFourierTransformTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"octavian-h_time-series-math"+File.separator+"DiscreteFourierTransformTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("octavian-h/time-series-math") && javaFileName.endsWith("DiscreteChebyshevTransformTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"octavian-h_time-series-math"+File.separator+"DiscreteChebyshevTransformTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/dashboard-view-plugin") && javaFileName.endsWith("TestTrendChartTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_dashboard-view-plugin"+File.separator+"TestTrendChartTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("dmulloy2/SwornAPI") && javaFileName.endsWith("VersioningTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"dmulloy2_SwornAPI"+File.separator+"VersioningTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("dmulloy2/SwornAPI") && javaFileName.endsWith("ConfigTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"dmulloy2_SwornAPI"+File.separator+"ConfigTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("dmulloy2/SwornAPI") && javaFileName.endsWith("BukkitTesting.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"dmulloy2_SwornAPI"+File.separator+"BukkitTesting.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("jenkinsci/google-kubernetes-engine-plugin") && javaFileName.endsWith("CredentialsUtilTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"jenkinsci_google-kubernetes-engine-plugin"+File.separator+"CredentialsUtilTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("vaulttec/sonar-auth-oidc") && javaFileName.endsWith("OidcIdentityProviderTest.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"vaulttec_sonar-auth-oidc"+File.separator+"OidcIdentityProviderTest.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    else if(repoName.equals("vy/reactor-pubsub") && javaFileName.endsWith("PubsubAccessTokenCacheFixture.java")){
      //copy file
      File fixedTestFile = new File(sourceLocation+File.separator+"vy_reactor-pubsub"+File.separator+"PubsubAccessTokenCacheFixture.java");
      File originalTestFile = new File(javaFileName);
      org.apache.commons.io.FileUtils.copyFile(fixedTestFile, originalTestFile);
      result = true;
      return result;
    }
    return result;
  }

  //go over the files in that folder and put the name of the file into the result set
  public static Set<String> findFilesWithExtension(String folderName, String extension) {
    Set<String> result = new HashSet<String>();
    List<String> workList = new ArrayList<String>();
    workList.add(folderName);
    while (!workList.isEmpty()) {
      String currFileName = workList.remove(0);
      File currFile = new File(currFileName);
      if (currFile.isDirectory()) {
        String fileNameArray[] = currFile.list();
        for (String fileName : fileNameArray) {
          String fullFileName = currFileName + File.separator + fileName;
          workList.add(fullFileName);
        }
      } else {
        if (currFileName.endsWith(extension)) {
          result.add(currFileName);
        }
      }
    }
    return result;
  }

  public static boolean checkIfClassesWithTestsUsePowerMockRunner(String fileName) throws IOException {
    boolean usesPowerMockRunner = false;
    CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
    List<ClassOrInterfaceDeclaration> classesContainingTests = getClassesContainingTests(compilationUnit);
    for(ClassOrInterfaceDeclaration classContainingTests:classesContainingTests){
      for (AnnotationExpr annotationExpr:classContainingTests.getAnnotations()) {
        if (annotationExpr.getName().asString().equals("RunWith")) {
          if (annotationExpr.toString().contains("PowerMockRunner.class")) {
            usesPowerMockRunner = true;
            return usesPowerMockRunner;
          }
        }
      }
    }
    return usesPowerMockRunner;
  }


  public static List<ClassOrInterfaceDeclaration> getClassesContainingTests(CompilationUnit compilationUnit){
    List<ClassOrInterfaceDeclaration> classesContainingTests = new ArrayList<ClassOrInterfaceDeclaration>();
    List<MethodDeclaration> methodDeclarations = compilationUnit.findAll(MethodDeclaration.class);
    for(MethodDeclaration methodDeclaration:methodDeclarations){
      for(AnnotationExpr annotationExpr:methodDeclaration.getAnnotations()){
        if(annotationExpr.getName().asString().equals("Test")){
          Optional<Node> optionalNode = methodDeclaration.getParentNode();
          while(optionalNode.isPresent()) {
            Node node = optionalNode.get();
            if(node instanceof ClassOrInterfaceDeclaration){
              ClassOrInterfaceDeclaration currclassDeclarationContainingTests = (ClassOrInterfaceDeclaration) node;
              classesContainingTests.add(currclassDeclarationContainingTests);
              break;
            }
            else{
              optionalNode = node.getParentNode();;
            }
          }
        }
      }
    }
    return classesContainingTests;
  }

  public static Set<String> getClassNamesContainingTests(String fileName)  throws IOException {
    Set<String> classesNamesContainingTests = new HashSet<String>();
    CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
    List<MethodDeclaration> methodDeclarations = compilationUnit.findAll(MethodDeclaration.class);
    for(MethodDeclaration methodDeclaration:methodDeclarations){
      for(AnnotationExpr annotationExpr:methodDeclaration.getAnnotations()){
        if(annotationExpr.getName().asString().equals("Test")){
          Optional<Node> optionalNode = methodDeclaration.getParentNode();
          while(optionalNode.isPresent()) {
            Node node = optionalNode.get();
            if(node instanceof ClassOrInterfaceDeclaration){
              ClassOrInterfaceDeclaration currclassDeclarationContainingTests = (ClassOrInterfaceDeclaration) node;
              classesNamesContainingTests.add(currclassDeclarationContainingTests.getName().asString());
              break;
            }
            else{
              optionalNode = node.getParentNode();;
            }
          }
        }
      }
    }
    return classesNamesContainingTests;
  }

  private static ClassOrInterfaceDeclaration getDeclaringClass(Node node){
    ClassOrInterfaceDeclaration classDeclarationContainingNode = null;
    List<Node> worklist = new ArrayList<Node>();
    worklist.add(node);
    while(!worklist.isEmpty()){
      Node currNode = worklist.remove(0);
      Optional<Node> optionalParentNode = currNode.getParentNode();
      if(optionalParentNode.isPresent()){
        Node parentNode = optionalParentNode.get();
        if(parentNode instanceof ClassOrInterfaceDeclaration){
          classDeclarationContainingNode = (ClassOrInterfaceDeclaration) parentNode;
          return classDeclarationContainingNode;
        }
      }
    }
    return classDeclarationContainingNode;
  }


  public static boolean checkIfClassHasTests(String fileName, String className) throws IOException {
    boolean result = false;
    CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
    List<ClassOrInterfaceDeclaration> classesContainingTests = getClassesContainingTests(compilationUnit);
    for(ClassOrInterfaceDeclaration classContainingTests:classesContainingTests) {
      if(classContainingTests.getName().asString().equals(className)) {
        result = true;
        return result;
      }
    }
    return result;
  }

  public static boolean checkIfHasTestsAndConstructor(String fileName, String className) throws IOException {
    boolean result = false;
    CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
    List<ClassOrInterfaceDeclaration> classesContainingTests = getClassesContainingTests(compilationUnit);
    ClassOrInterfaceDeclaration classDeclarationContainingTests = null;
    for(ClassOrInterfaceDeclaration classContainingTests:classesContainingTests){
      if(classContainingTests.getName().asString().equals(className)){
        classDeclarationContainingTests = classContainingTests;
        break;
      }
    }
    if (classDeclarationContainingTests != null) {
      List<ConstructorDeclaration> constructorDeclarations = classDeclarationContainingTests.findAll(ConstructorDeclaration.class);
      for(ConstructorDeclaration constructorDeclaration:constructorDeclarations) {
        ClassOrInterfaceDeclaration declaringClassForConstructor = getDeclaringClass(constructorDeclaration);
        if (declaringClassForConstructor != null && declaringClassForConstructor.getName().asString().equals(classDeclarationContainingTests.getName().asString())) {
          result = true;
          return result;
        }
      }
    }
    return result;
  }

  public static CompilationUnit transformThenReturnToDoReturn(CompilationUnit cu, int lineNum, String methodName) throws IOException {
    List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
    for (MethodDeclaration methodDeclaration : methodDeclarations) {
      BlockStmt body = methodDeclaration.getBody().get();
      for (Statement s : body.getStatements()) {
        if (s.getBegin().get().line == lineNum) {
          Expression exp = ((ExpressionStmt) s).getExpression();
          if(s instanceof ExpressionStmt){
//            System.out.println("statement: " + s.toString());
//            YamlPrinter printer = new YamlPrinter(true);
//            System.out.println(printer.output(s));
            Expression expression = ((ExpressionStmt) s).getExpression();
            NodeList<Expression> returnArg = ((MethodCallExpr)expression).getArguments();
            if(((MethodCallExpr) expression).getName().getIdentifier().equals("thenReturn")|| ((MethodCallExpr) expression).getName().getIdentifier().equals("willReturn")) {
              //get when
              Expression scope =  ((MethodCallExpr)expression).getScope().get();
              //with scope (eg.Mockito.when)
              if ( ((MethodCallExpr) scope).getName().getIdentifier().equals("when") || ((MethodCallExpr) scope).getName().getIdentifier().equals("given") ){
                //get mock object and mock arguments
                Optional<Expression> mockScope = ((MethodCallExpr) scope).getScope();
                Expression beforeMock = null;
                if (!mockScope.equals(Optional.empty())){
                  beforeMock = mockScope.get();
                }
                NodeList<Expression> mockExpresions = ((MethodCallExpr)scope).getArguments();
                for(Expression mockExpression: mockExpresions){
                  if ( ((MethodCallExpr) mockExpression).getName().getIdentifier().equals(methodName)){
                    NodeList<Expression> mockArgs = ((MethodCallExpr)mockExpression).getArguments();
                    Expression mockObj = ((MethodCallExpr)mockExpression).getScope().get();
                    //create new doReturn() expression
                    MethodCallExpr expre1 = new MethodCallExpr(beforeMock,"doReturn", returnArg);
                    NodeList<Expression> l = new NodeList<>();
                    l.add(mockObj);
                    MethodCallExpr expre2 = new MethodCallExpr(expre1,"when", l);
                    MethodCallExpr expre3 = new MethodCallExpr(expre2, methodName, mockArgs);
                    //set new expression
                    ((ExpressionStmt) s).setExpression(expre3);
                  }
                  else{
                    break;
                  }
                }
              }
            }
          }
        }
      }
    }
    return cu;
  }

  private static MethodDeclaration createBeforeMethodThatNullifiesFields(List<String> fields){
    MethodDeclaration nullifyMethod = StaticJavaParser.parseMethodDeclaration("public void nullifyFields(){}");
    String block = "{" + System.lineSeparator();
    for(String field:fields){
      block = block + field + "=null;" + System.lineSeparator();
    }
    block = block + "}";
    BlockStmt loggingMethodBody = StaticJavaParser.parseBlock(block);
    nullifyMethod.setBody(loggingMethodBody);
    nullifyMethod.addAnnotation("Before");
    return nullifyMethod;
  }

  public static void undoInitializationOfFieldsAnnotatedWithAtMock(String repoName, String fileName, String className) throws IOException {
    if(repoName.equals("jenkinsci/repository-connector-plugin") && fileName.endsWith("RemoteRepositoryFactoryTest.java") && className.equals("RemoteRepositoryFactoryTest")){
      CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
      List<ClassOrInterfaceDeclaration> classesContainingTests = getClassesContainingTests(compilationUnit);
      ClassOrInterfaceDeclaration classDeclarationContainingTests = null;
      for(ClassOrInterfaceDeclaration classContainingTests:classesContainingTests){
        if(classContainingTests.getName().asString().equals(className)){
          classDeclarationContainingTests = classContainingTests;
          break;
        }
      }
      if(classDeclarationContainingTests!=null) {
        List<String> fields = new ArrayList<String>();
        fields.add("mockCredentials");
        classDeclarationContainingTests.getMembers().add(FileUtils.createBeforeMethodThatNullifiesFields(fields));
        compilationUnit.addImport("org.junit.Before");
        File modifiedTestFile = new File(fileName);
        FileWriter fooWriter = new FileWriter(modifiedTestFile, false);
        fooWriter.write(compilationUnit.toString());
        fooWriter.close();
      }
    }
  }



  public static boolean checkIfHasRunWithOtherThanMockitoJunitRunner(String fileName, String className) throws IOException {
    boolean result = false;
    CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
    List<ClassOrInterfaceDeclaration> classesContainingTests = getClassesContainingTests(compilationUnit);
    ClassOrInterfaceDeclaration classDeclarationContainingTests = null;
    for(ClassOrInterfaceDeclaration classContainingTests:classesContainingTests){
      if(classContainingTests.getName().asString().equals(className)){
        classDeclarationContainingTests = classContainingTests;
        break;
      }
    }
    if (classDeclarationContainingTests != null) {
      NodeList<AnnotationExpr> annotationExprs = classDeclarationContainingTests.getAnnotations();
      for(AnnotationExpr annotationExpr:annotationExprs){
        if(annotationExpr.getName().asString().equals("RunWith")){
          if(!annotationExpr.toString().contains("MockitoJUnitRunner")){
            result = true;
            return result;
          }
        }
      }
    }
    return result;
  }

  //replace @RunWith(MockitoJUnitRunner...) with @RunWith(MockitoJUnitRunner.MockitoJUnitRunner.Silent.class
  public static boolean replaceRunWithMockitoJUnitRunnerIfNecessary(String fileName, String className) throws IOException {
    boolean result = false;
    CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
    List<ClassOrInterfaceDeclaration> classesContainingTests = getClassesContainingTests(compilationUnit);
    ClassOrInterfaceDeclaration classDeclarationContainingTests = null;
    for(ClassOrInterfaceDeclaration classContainingTests:classesContainingTests){
      if(classContainingTests.getName().asString().equals(className)){
        classDeclarationContainingTests = classContainingTests;
        break;
      }
    }
    boolean needToReplaceRunWith = false;
    boolean needToAddImport = false;
    if (classDeclarationContainingTests != null) {
      NodeList<AnnotationExpr> annotationExprs = classDeclarationContainingTests.getAnnotations();
      int indexToRemove = -1;
      for(int i=0; i< annotationExprs.size();++i){
        if(annotationExprs.get(i).getName().asString().equals("RunWith")){
          if(annotationExprs.get(i).toString().contains("MockitoJUnitRunner")){
            indexToRemove = i;
            if(!annotationExprs.get(i).toString().startsWith("MockitoJUnitRunner")){
              needToAddImport = true;
            }
          }
        }
      }
      if(indexToRemove!=-1){
        if(!annotationExprs.get(indexToRemove).remove()){
          result = false;
          return result;
        }
        else{
          needToReplaceRunWith = true;
        }
      }
    }
    if(needToReplaceRunWith) {
      SingleMemberAnnotationExpr runWithAnnotation = new SingleMemberAnnotationExpr(new Name("RunWith"), new NameExpr("MockitoJUnitRunner.Silent.class"));
      classDeclarationContainingTests.addAnnotation(runWithAnnotation);
      if(needToAddImport){
        compilationUnit.addImport("org.mockito.junit.MockitoJUnitRunner");
      }
      File modifiedTestFile = new File(fileName);
      FileWriter fooWriter = new FileWriter(modifiedTestFile, false);
      fooWriter.write(compilationUnit.toString());
      fooWriter.close();
    }
    result = true;
    return result;
  }

  public static boolean addMockitoRule(String fileName, String className) throws IOException {
    boolean success = false;
    CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
    List<ClassOrInterfaceDeclaration> classesContainingTests = getClassesContainingTests(compilationUnit);
    ClassOrInterfaceDeclaration classDeclarationContainingTests = null;
    for(ClassOrInterfaceDeclaration classContainingTests:classesContainingTests){
      if(classContainingTests.getName().asString().equals(className)){
        classDeclarationContainingTests = classContainingTests;
        break;
      }
    }
    if(classDeclarationContainingTests!=null){
      //check if class declaration has already a mockito rule and in that case remove it
      List<FieldDeclaration> fieldDeclarations = classDeclarationContainingTests.findAll(FieldDeclaration.class);
      for(FieldDeclaration fieldDeclaration:fieldDeclarations){
        ClassOrInterfaceDeclaration declaringClassForField= getDeclaringClass(fieldDeclaration);
        if(declaringClassForField==null || !declaringClassForField.getName().asString().equals(classDeclarationContainingTests.getName().asString())) {
          continue;
        }
        NodeList<AnnotationExpr> fieldAnnotations = fieldDeclaration.getAnnotations();
        boolean hasRuleAnnotation = false;
        for(AnnotationExpr fieldAnnotation:fieldAnnotations){
          if(fieldAnnotation.getNameAsString().equals("Rule")){
            hasRuleAnnotation = true;
            break;
          }
        }
        if(hasRuleAnnotation){
          Flag ruleFlag = new Flag();
          fieldDeclaration.accept(new VoidVisitorAdapter<Flag>() {
            @Override
            public void visit(MethodCallExpr methodCallExpr, Flag methodCallExprFlag){
              if(methodCallExpr.getNameAsString().equals("rule")){
                if(methodCallExpr.getScope().isPresent()){
                  methodCallExpr.getScope().get().accept(new VoidVisitorAdapter<Flag>() {
                    @Override
                    public void visit(SimpleName simpleName, Flag simpleNameFlag){
                      if(simpleName.getIdentifier().equals("MockitoJUnit")){
                        simpleNameFlag.setValue(true);
                      }
                    }
                  }, methodCallExprFlag);
                }
              }
              if(methodCallExpr.getNameAsString().equals("rule")){
                List<ImportDeclaration> importDeclarations = compilationUnit.findAll(ImportDeclaration.class);
                for(ImportDeclaration importDeclaration:importDeclarations){
                  if(importDeclaration.getName().asString().equals("org.mockito.junit.MockitoJUnit.rule")){
                    methodCallExprFlag.setValue(true);
                  }
                }
              }
            }
          }, ruleFlag);
          if(ruleFlag.getValue()){
            if(!fieldDeclaration.remove()){
              success = false;
              return success;
            }
            break;
          }
        }
      }
      compilationUnit.addImport("org.junit.Rule");
      compilationUnit.addImport("org.mockito.junit.MockitoJUnit");
      compilationUnit.addImport("org.mockito.junit.MockitoRule");
      compilationUnit.addImport("org.mockito.quality.Strictness");
      VariableDeclarator variables = new VariableDeclarator();
      variables.setName("experimentRule");
      variables.setType("MockitoRule");
      variables.setInitializer(new NameExpr("MockitoJUnit.rule().strictness(Strictness.LENIENT)"));
      FieldDeclaration fieldDeclaration = new FieldDeclaration().addVariable(variables);
      fieldDeclaration.addModifier(Modifier.Keyword.PUBLIC);
      fieldDeclaration.addAnnotation(new MarkerAnnotationExpr("Rule"));
      classDeclarationContainingTests.getMembers().add(0, fieldDeclaration);
      File modifiedTestFile = new File(fileName);
      FileWriter fooWriter = new FileWriter(modifiedTestFile, false);
      fooWriter.write(compilationUnit.toString());
      fooWriter.close();
    }
    success = true;
    return success;
  }

  public static boolean setMockitoRule(String fileName, String className) throws IOException {
    boolean success = false;
    CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
    List<ClassOrInterfaceDeclaration> classesContainingTests = getClassesContainingTests(compilationUnit);
    ClassOrInterfaceDeclaration classDeclarationContainingTests = null;
    for(ClassOrInterfaceDeclaration classContainingTests:classesContainingTests){
      if(classContainingTests.getName().asString().equals(className)){
        classDeclarationContainingTests = classContainingTests;
        break;
      }
    }
    if(classDeclarationContainingTests!=null){
      //check if class declaration has already a mockito rule and in that case remove it
      List<FieldDeclaration> fieldDeclarations = classDeclarationContainingTests.findAll(FieldDeclaration.class);
      for(FieldDeclaration fieldDeclaration:fieldDeclarations){
        ClassOrInterfaceDeclaration declaringClassForField= getDeclaringClass(fieldDeclaration);
        if(declaringClassForField==null || !declaringClassForField.getName().asString().equals(classDeclarationContainingTests.getName().asString())) {
          continue;
        }
        NodeList<VariableDeclarator> vars = fieldDeclaration.getVariables();
        for(VariableDeclarator var: vars){
          if(var.getName().getIdentifier().equals("experimentRule")){
            var.setInitializer(new NameExpr("MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS)"));
          }
        }
      }
      for(AnnotationExpr antexpr: classDeclarationContainingTests.getAnnotations()){
        if(antexpr.isSingleMemberAnnotationExpr()){
          Expression expr = ((SingleMemberAnnotationExpr)antexpr).getMemberValue();
          if(expr.isNameExpr()){
            if(((NameExpr) expr).getName().getIdentifier().contains("MockitoJUnitRunner")){
              ((SingleMemberAnnotationExpr)antexpr).setMemberValue(new NameExpr("MockitoJUnitRunner.StrictStubs.class"));
            }
          }
        }
      }

      File modifiedTestFile = new File(fileName);
      FileWriter fooWriter = new FileWriter(modifiedTestFile, false);
      fooWriter.write(compilationUnit.toString());
      fooWriter.close();
    }

    success = true;
    return success;
  }


  private static ClassOrInterfaceDeclaration findDeclaringClassOrInterface(Node node){
    ClassOrInterfaceDeclaration classOrInterfaceDeclaration = null;
    Optional<Node> optionalNode = node.getParentNode();
    while(optionalNode.isPresent()) {
      Node currNode = optionalNode.get();
      if(currNode instanceof ClassOrInterfaceDeclaration){
        classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) currNode;
        break;
      }
      else{
        optionalNode = currNode.getParentNode();;
      }
    }
    return classOrInterfaceDeclaration;
  }


  private static MethodDeclaration createLoggingMethod(){
    MethodDeclaration loggingMethod = StaticJavaParser.parseMethodDeclaration("private void experimentsLogger(String info){}");
    BlockStmt loggingMethodBody = StaticJavaParser.parseBlock("{" + System.lineSeparator() +
            "try {" + System.lineSeparator() +
            "\tFileWriter fw = new FileWriter(\"/tmp/mel.txt\", true);" + System.lineSeparator() +
            "\tBufferedWriter bw = new BufferedWriter(fw);" + System.lineSeparator() +
            "\tbw.write(info);" + System.lineSeparator() +
            "\tbw.flush();" + System.lineSeparator() +
            "\tbw.close();" + System.lineSeparator() +
            "}" + System.lineSeparator() +
            "catch(Exception e){" + System.lineSeparator() +
            "" + System.lineSeparator() +
            "}" + System.lineSeparator() +
            "}");
            loggingMethod.setBody(loggingMethodBody);
    return loggingMethod;
  }

  private static Statement createStatement(String statement){
    Statement result = StaticJavaParser.parseStatement(statement);
    return result;
  }

  public static void addConstructorLogging(String fileName, String className) throws IOException {
    boolean changed = false;
    CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
    String packageName = "";
    Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
    if(packageDeclaration.isPresent()){
      packageName = packageDeclaration.get().getName().asString();
    }
    List<ClassOrInterfaceDeclaration> classesContainingTests = getClassesContainingTests(compilationUnit);
    ClassOrInterfaceDeclaration classDeclarationContainingTests = null;
    for(ClassOrInterfaceDeclaration classContainingTests:classesContainingTests){
      if(classContainingTests.getName().asString().equals(className)){
        classDeclarationContainingTests = classContainingTests;
        break;
      }
    }
    if(classDeclarationContainingTests!=null) {
      List<ConstructorDeclaration> constructorDeclarations = classDeclarationContainingTests.findAll(ConstructorDeclaration.class);
      boolean addedLoggingMethod = false;
      for (ConstructorDeclaration constructorDeclaration : constructorDeclarations) {
        ClassOrInterfaceDeclaration declaringClassForConstructor = getDeclaringClass(constructorDeclaration);
        if(declaringClassForConstructor!=null && declaringClassForConstructor.getName().asString().equals(classDeclarationContainingTests.getName().asString())) {
          if(!addedLoggingMethod) {
            addedLoggingMethod = true;
            classDeclarationContainingTests.getMembers().add(FileUtils.createLoggingMethod());
            compilationUnit.addImport("java.io.BufferedWriter");
            compilationUnit.addImport("java.io.File");
            compilationUnit.addImport("java.io.FileWriter");
          }
          changed = true;
          int startIndexForLoggingInBody = 0;
          for (int i = 0; i < constructorDeclaration.getBody().getStatements().size(); ++i) {
            Statement stmt = constructorDeclaration.getBody().getStatements().get(i);
            if (stmt instanceof ExpressionStmt) {
              ExpressionStmt expressionStmt = (ExpressionStmt) stmt;
              if (expressionStmt.getExpression() instanceof SuperExpr) {
                startIndexForLoggingInBody = i + 1;
              }
            }
            else if (stmt instanceof ExplicitConstructorInvocationStmt) {
              ExplicitConstructorInvocationStmt explicitConstructorInvocationStmt = (ExplicitConstructorInvocationStmt) stmt;
              if(!explicitConstructorInvocationStmt.isThis()) {
                startIndexForLoggingInBody = i + 1;
              }
            }
          }
          if (startIndexForLoggingInBody == constructorDeclaration.getBody().getStatements().size()) {
            constructorDeclaration.getBody().addStatement(createStatement("experimentsLogger(\"###test_constructor_execution_start\"+System.lineSeparator());"));
            constructorDeclaration.getBody().addStatement(createStatement("experimentsLogger(\"###test_constructor_class:" + packageName + "." + classDeclarationContainingTests.getName().asString() + "\"+System.lineSeparator());"));
            constructorDeclaration.getBody().addStatement(createStatement("experimentsLogger(\"###test_constructor_name:" + constructorDeclaration.getName().asString() + "\"+System.lineSeparator());"));
            constructorDeclaration.getBody().addStatement(createStatement("experimentsLogger(\"###test_constructor_execution_end\"+System.lineSeparator());"));
          } else {
            constructorDeclaration.getBody().addStatement(startIndexForLoggingInBody, createStatement("experimentsLogger(\"###test_constructor_execution_start\"+System.lineSeparator());"));
            constructorDeclaration.getBody().addStatement(startIndexForLoggingInBody + 1, createStatement("experimentsLogger(\"###test_constructor_class:" + packageName + "." + classDeclarationContainingTests.getName().asString() + "\"+System.lineSeparator());"));
            constructorDeclaration.getBody().addStatement(startIndexForLoggingInBody + 2, createStatement("experimentsLogger(\"###test_constructor_name:" + constructorDeclaration.getName().asString() + "\"+System.lineSeparator());"));
            constructorDeclaration.getBody().addStatement(createStatement("experimentsLogger(\"###test_constructor_execution_end\"+System.lineSeparator());"));
          }
        }
      }
      if (changed) {
        File modifiedTestFile = new File(fileName);
        FileWriter fooWriter = new FileWriter(modifiedTestFile, false);
        fooWriter.write(compilationUnit.toString());
        fooWriter.close();
      }
    }
  }

  public static void addRunWithAnnotation(String fileName, String className) throws IOException {
    CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
    List<ClassOrInterfaceDeclaration> classesContainingTests = getClassesContainingTests(compilationUnit);
    ClassOrInterfaceDeclaration classDeclarationContainingTests = null;
    for(ClassOrInterfaceDeclaration classContainingTests:classesContainingTests){
      if(classContainingTests.getName().asString().equals(className)){
        classDeclarationContainingTests = classContainingTests;
        break;
      }
    }
    compilationUnit.addImport("org.junit.runner.RunWith");
    compilationUnit.addImport("org.mockito.junit.MockitoJUnitRunner");
    SingleMemberAnnotationExpr runWithAnnotation = new SingleMemberAnnotationExpr(new Name("RunWith"), new NameExpr("MockitoJUnitRunner.Silent.class"));
    classDeclarationContainingTests.addAnnotation(runWithAnnotation);
    File modifiedTestFile = new File(fileName);
    FileWriter fooWriter = new FileWriter(modifiedTestFile, false);
    fooWriter.write(compilationUnit.toString());
    fooWriter.close();
  }

  public static void addMockitoAnnotationsIfNecessary(String fileName, String className) throws IOException {
    CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
    List<ClassOrInterfaceDeclaration> classesContainingTests = getClassesContainingTests(compilationUnit);
    ClassOrInterfaceDeclaration classDeclarationContainingTests = null;
    for(ClassOrInterfaceDeclaration classContainingTests:classesContainingTests){
      if(classContainingTests.getName().asString().equals(className)){
        classDeclarationContainingTests = classContainingTests;
        break;
      }
    }
    if(classDeclarationContainingTests!=null){
      compilationUnit.addImport("org.junit.jupiter.api.extension.ExtendWith");
      compilationUnit.addImport("org.mockito.junit.jupiter.MockitoExtension");
      compilationUnit.addImport("org.mockito.junit.jupiter.MockitoSettings");
      compilationUnit.addImport("org.mockito.quality.Strictness");
      //handle MockitoSettings
      int existingMockitoSettingsIndex = -1;
      for(int i=0; i<classDeclarationContainingTests.getAnnotations().size(); ++i){
        AnnotationExpr annotationExpr = classDeclarationContainingTests.getAnnotations().get(i);
        if(annotationExpr.getNameAsString().equals("MockitoSettings")){
          existingMockitoSettingsIndex=i;
          break;
        }
      }
      if(existingMockitoSettingsIndex!=-1){
        classDeclarationContainingTests.getAnnotations().remove(existingMockitoSettingsIndex);
      }
      //add annotations
      SingleMemberAnnotationExpr extendWithAnnotation = new SingleMemberAnnotationExpr(new Name("ExtendWith"), new NameExpr("MockitoExtension.class"));
      classDeclarationContainingTests.addAnnotation(extendWithAnnotation);
      NormalAnnotationExpr mockitoSettingsAnnotation = new NormalAnnotationExpr();
      mockitoSettingsAnnotation.setName("MockitoSettings");
      mockitoSettingsAnnotation.addPair("strictness", "Strictness.WARN");
      classDeclarationContainingTests.addAnnotation(mockitoSettingsAnnotation);
      File modifiedTestFile = new File(fileName);
      FileWriter fooWriter = new FileWriter(modifiedTestFile, false);
      fooWriter.write(compilationUnit.toString());
      fooWriter.close();
    }
  }

  public static int countTestsInFile(String filename) {
    int count = 0;
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim(); // remove leading and trailing whitespace
        if (line.startsWith("@Test")) {
          count++;
        }
      }
    } catch (IOException e) {
      System.out.println("An error occurred while reading the file.");
      e.printStackTrace();
    }
    return count;
  }

}

