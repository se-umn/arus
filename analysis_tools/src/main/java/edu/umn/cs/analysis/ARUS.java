package edu.umn.cs.analysis;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.umn.cs.analysis.model.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.TransportException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import com.opencsv.CSVWriter;


public class ARUS{
  private static String REPO_FULL_PATH = "";
  private static String REPO_NAME="";
  private static String RESULTS_FILE_NAME = "";
  private static String MAVEN_HOME = "";
  private static String CUSTOMIZED_MOCKITO_LIBS_FOLDER_NAME = "";
  private static String ADDITIONAL_FILES_FOLDER_NAME = "";
  private static int TEST_EXECUTION_REPETITIONS = 3;
  private static int SOLUTION_NUMBER = 1;
  private static boolean REMOVE_UUS = true;

  private Set<String> relevantRepos = new HashSet<>();
  private boolean cloneRepo = true;
  private boolean completeAnalysis = true;

  public static void main(String args[]) {

    // Path is always required, so if it's not there, exit.
    if (args.length < 2) {
      System.out.println("usage: ./gradlew clean -PmainClass=edu.umn.cs.analysis.ARUS run --args=\"config_file_name repo_full_path WITHOUT_SETUP_boolean\"");
      System.exit(1);
    }

    if (args.length == 2) {
      ARUS.REPO_FULL_PATH = args[1];
      if(!new File(ARUS.REPO_FULL_PATH).exists()){
        System.out.println("Wrong project path, please enter the absolute path to the project.");
        System.exit(1);
      }
    } else if (args.length == 3) {
      ARUS.REPO_FULL_PATH = args[1];
      if(!new File(ARUS.REPO_FULL_PATH).exists()){
        System.out.println("Wrong project path, please enter the absolute path to the project.");
        System.exit(1);
      }
      if (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false")) {
        // The user has provided the boolean flag only
        REMOVE_UUS = Boolean.parseBoolean(args[2]);
      }
      else{
        System.out.println("Invalid argument. The third argument must be a boolean.");
      }
    }

    try {
      String rootDir = Paths.get("").toAbsolutePath().toString();


      ARUS.RESULTS_FILE_NAME = rootDir+File.separator+"data/run_with_customized_mockito_results.json";
      ARUS.MAVEN_HOME = args[0];
      ARUS.CUSTOMIZED_MOCKITO_LIBS_FOLDER_NAME = rootDir+"/libs";
      ARUS.ADDITIONAL_FILES_FOLDER_NAME = rootDir+"/files";
      if (ARUS.RESULTS_FILE_NAME.equals("") || ARUS.MAVEN_HOME.equals("") || ARUS.CUSTOMIZED_MOCKITO_LIBS_FOLDER_NAME.equals("") || ARUS.ADDITIONAL_FILES_FOLDER_NAME.equals("")) {
        System.out.println("ERROR: Need suitable configuration information");
        System.exit(1);
      }

      ARUS rwcm = new ARUS();
      ExperimentRepoInfo result = new ExperimentRepoInfo();
      rwcm.analyzeRepo(result);

    } catch (Exception e) {
      e.printStackTrace(System.out);
    }
  }

  public static void deleteDirectory(File dir) throws IOException {
    Path directoryPath = dir.toPath(); // Convert File to Path
    Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file); // Delete each file
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc != null) throw exc; // Re-throw any exception encountered during the directory visit
        Files.delete(dir); // Delete directory after its contents are deleted
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private ExperimentRepoInfo analyzeRepo(ExperimentRepoInfo result) {
    String workingDirectoryInRepo = "";
    String[] splitedPath = ARUS.REPO_FULL_PATH.split("/");
    ARUS.REPO_NAME = splitedPath[splitedPath.length-1];

    ////////////////////////////////start analyzing repo////////////////////////////////
    System.out.println("Analyzing: " + ARUS.REPO_NAME);
    result.setAnalysisStarted(true);
    AnalysisStatistics analysisStatistics = new AnalysisStatistics();
    long totalAnalysisTime = 0;
    long repoCloneTime = 0;
    long originalTestsTime = 0;
    long customizedMockitoTestsTime = 0;
    long modificationExecutionTime = 0;
    long totalAnalysisTimeStart = System.currentTimeMillis();
    long repoCloneTimeStart = System.currentTimeMillis();
   
    try {

      File originalFolderName = new File(ARUS.REPO_FULL_PATH);
      File experimentFolderName = new File(ARUS.REPO_FULL_PATH+"_experiment");
      if(experimentFolderName.exists()){
        System.out.println(ARUS.REPO_FULL_PATH+"_experiment exists, deleting...");
        deleteDirectory(experimentFolderName);
      }
      System.out.println("copying file from "+originalFolderName+" to "+experimentFolderName);
      org.apache.commons.io.FileUtils.copyDirectory(originalFolderName,experimentFolderName);

      result.setOriginalDirectory(ARUS.REPO_FULL_PATH);
      result.setModifiedDirectory(ARUS.REPO_FULL_PATH+"_experiment");

      //change pom file to use our customize version of mockito
      String originalPomFile = "";
      String experimentPomFile = "";
      String copyExperimentPomFile = "";
      String originalSurfireDirectory = "";
      String experimentSurfireDirectory = "";
      String customizedMockitoResultFileNameInExperimentProject = "";
      String originalTestDirectoryName = "";
      String experimentTestDirectoryName = "";
      if (workingDirectoryInRepo.equals("")) {
        originalPomFile = ARUS.REPO_FULL_PATH + File.separator + "pom.xml";
        experimentPomFile = experimentFolderName + File.separator + "pom.xml";
        copyExperimentPomFile = experimentFolderName  + File.separator + "temp_pom.xml";
        originalSurfireDirectory = ARUS.REPO_FULL_PATH + File.separator + "target" + File.separator + "surefire-reports";
        experimentSurfireDirectory = experimentFolderName + File.separator + "target" + File.separator + "surefire-reports";
        customizedMockitoResultFileNameInExperimentProject = experimentFolderName + File.separator + "mel.txt";
        if (ARUS.REPO_NAME.equals("heros")) {
          originalTestDirectoryName = originalFolderName + File.separator + "test";
          experimentTestDirectoryName = experimentFolderName + File.separator + "test";
        } else {
          originalTestDirectoryName = originalFolderName + File.separator + "src" + File.separator + "test";
          experimentTestDirectoryName = experimentFolderName + File.separator + "src" + File.separator + "test";
        }
      } else {
        originalPomFile = ARUS.REPO_FULL_PATH + File.separator + workingDirectoryInRepo + File.separator + "pom.xml";
        experimentPomFile = experimentFolderName + File.separator + workingDirectoryInRepo + File.separator + "pom.xml";
        copyExperimentPomFile = experimentFolderName + File.separator + workingDirectoryInRepo  + File.separator + "temp_pom.xml";
        originalSurfireDirectory = ARUS.REPO_FULL_PATH + File.separator + workingDirectoryInRepo + File.separator + "target" + File.separator + "surefire-reports";
        experimentSurfireDirectory = experimentFolderName + File.separator + workingDirectoryInRepo + File.separator + "target" + File.separator + "surefire-reports";
        customizedMockitoResultFileNameInExperimentProject = experimentFolderName + File.separator + workingDirectoryInRepo + File.separator + "mel.txt";
        originalTestDirectoryName = originalFolderName + File.separator + workingDirectoryInRepo + File.separator + "src" + File.separator + "test";
        experimentTestDirectoryName = experimentFolderName + File.separator + workingDirectoryInRepo + File.separator + "src" + File.separator + "test";
      }
      String dependencyTreeFileName = "";
      int mockitoMajorVersion = 0;
      int junitMajorVersion = -1;
      int junitMinorVersion = -1;
      boolean gotDependencyTree = MavenUtils.runMavenDependencyTree(originalPomFile, ARUS.MAVEN_HOME, "umn_dependency_tree.xml");
      if (!gotDependencyTree) {
        String errorMessage = "ERROR: Could not get dependency tree";
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueWithDependencyTree(true);
        return result;
      }
      if (workingDirectoryInRepo.equals("")) {
        dependencyTreeFileName = ARUS.REPO_FULL_PATH + File.separator + "umn_dependency_tree.xml";
      } else {
        dependencyTreeFileName = ARUS.REPO_FULL_PATH + File.separator + workingDirectoryInRepo + File.separator + "umn_dependency_tree.xml";
      }
      //junit
      boolean junit = MavenUtils.checkForDependency(dependencyTreeFileName, MavenUtils.SCOPES, "junit");
      boolean junitJupiter = MavenUtils.checkForDependency(dependencyTreeFileName, MavenUtils.SCOPES, "org.junit.jupiter");
      boolean usesJunit = junit || junitJupiter;
      if (!usesJunit) {
        String errorMessage = "ERROR: Does not use junit";
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueJunitVersion(true);
        return result;
      }
      Set<String> junitDependencyLabels = new HashSet<String>();
      if (junitJupiter) {
        junitDependencyLabels.addAll(MavenUtils.getLabelsForDependency(dependencyTreeFileName, MavenUtils.SCOPES, "org.junit.jupiter"));
      } else if (junit) {
        junitDependencyLabels.addAll(MavenUtils.getLabelsForDependency(dependencyTreeFileName, MavenUtils.SCOPES, "junit"));
      }
      junitMajorVersion = AnalysisUtils.getJunitMajorVersion(junitDependencyLabels);
      junitMinorVersion = AnalysisUtils.getJunitMinorVersion(junitDependencyLabels);
      //mockito
      Set<String> mockitoDependencyLabels = MavenUtils.getLabelsForDependency(dependencyTreeFileName, MavenUtils.SCOPES, "org.mockito");
      mockitoMajorVersion = AnalysisUtils.getMajorMockitoVersion(AnalysisUtils.getMockitoDep(mockitoDependencyLabels));

      ///////////////////////////////////junit versions checks////////////////////////////////////
      System.out.println("Junit:" + junitMajorVersion + "." + junitMinorVersion);
      if (junitMajorVersion != 4 && junitMajorVersion != 5) {
        String errorMessage = "STOP: Junit vçersion not supported#" + junitMajorVersion;
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueJunitVersion(true);
        return result;
      }

      ///////////////////////////////////mockito versions checks////////////////////////////////////
      System.out.println("Mockito:" + mockitoMajorVersion);
      if (mockitoMajorVersion != 3) {
        String errorMessage = "STOP: Mockito version not supported#" + mockitoMajorVersion;
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueMockitoVersion(true);
        return result;
      }

      /////////////////////////////////test file directory check////////////////////////////////////////////
      File experimentTestDirectoryFile = new File(experimentTestDirectoryName);
      if (!experimentTestDirectoryFile.exists()) {
        String errorMessage = "ERROR: Did not find test directory";
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueWithTestDirectory(true);
        return result;
      }

      /////////////////////////////////powermock check////////////////////////////////////////////
      //check if uses powermockrunner in any of its class because we do not support it
      boolean usesPowerMockRunner = false;
      Set<String> originalJavaFileNamesInTestsFolder = FileUtils.findFilesWithExtension(originalTestDirectoryName, ".java");
      Set<String> javaFileNamesInTestsFolder = FileUtils.findFilesWithExtension(experimentTestDirectoryName, ".java");
      for (String javaFileName : javaFileNamesInTestsFolder) {
        boolean fileUsesPowerMockRunner = FileUtils.checkIfClassesWithTestsUsePowerMockRunner(javaFileName);
        if (fileUsesPowerMockRunner) {
          usesPowerMockRunner = true;
        }
      }
      if (usesPowerMockRunner) {
        String errorMessage = "STOP: Cannot handle because the project uses PowerMockRunner";
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueWithTestDirectory(true);
        return result;
      }

      ////////////////////////////check if additional mvn test parameters are needed////////////////
      List<String> mavenOptions = new ArrayList<String>();
      boolean usesEnforcerPlugin = checkPluginUse(experimentPomFile, "org.apache.maven.plugins", "maven-enforcer-plugin");
      if (usesEnforcerPlugin) {
        mavenOptions.add("-Denforcer.skip=true");
      }
      if (ARUS.REPO_NAME.equals("native-protocol")) {
        mavenOptions.add("-Dlicense.skip=true");
        mavenOptions.add("-Dfmt.skip=true");
      }
      if (ARUS.REPO_NAME.equals("spring-boot-graceful-shutdown")) {
        mavenOptions.add("-Dcheckstyle.skip");
        mavenOptions.add("-Denforcer.skip=true");
      }
      if (ARUS.REPO_NAME.equals("gozer")) {
        mavenOptions.add("-Dcheckstyle.skip");
      }

      /////////////////////////////////run original tests////////////////////////////////////////////
      //run original project tests
      analysisStatistics.setTestExecutionRepetitions(ARUS.TEST_EXECUTION_REPETITIONS);
      long originalTestsTimeStart = System.currentTimeMillis();
      List<TestExecutionResult> originalTestExecutionResults = new ArrayList<TestExecutionResult>();
      TestExecutionResult testExecutionResult = new TestExecutionResult();
      originalTestExecutionResults = MavenUtils.runTestsWithMaven(ARUS.MAVEN_HOME, originalPomFile,
              originalSurfireDirectory, ARUS.TEST_EXECUTION_REPETITIONS, mavenOptions);
      if (originalTestExecutionResults.size() == 0) {
        String errorMessage = "TERMINATE: No test results for original project";
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueRunningOriginalTests(true);
        return result;
      }
      testExecutionResult = originalTestExecutionResults.get(0);
      if (!testExecutionResult.isMavenSuccess() || testExecutionResult.getFailed() > 0 || testExecutionResult.getErrors() > 0) {
        String errorMessage = "TERMINATE: Problems with tests in the original project#" + testExecutionResult.getExecutions();
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueRunningOriginalTests(true);
        return result;
      }
      int originalTestsPassedCount = AnalysisUtils.checkConsistencyOfExecutionResults(originalTestExecutionResults);
      if(originalTestsPassedCount==-1){
        String errorMessage = "TERMINATE: Problems with consistency of the tests in the original project";
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueRunningOriginalTests(true);
        return result;
      }
      long originalTestsTimeEnd = System.currentTimeMillis();
      originalTestsTime = originalTestsTimeEnd - originalTestsTimeStart;
      analysisStatistics.setOriginalTestsTime(originalTestsTime);
      analysisStatistics.setOriginalTestsPassedCount(originalTestsPassedCount);
      analysisStatistics.setTotalTestCount(originalTestExecutionResults.get(0).getExecutions());


      /////////////////////////////////make junit version change////////////////////////////////////////////
      //upgrade to junit 4.13 if needed
      if (junitMajorVersion < 5) {
        boolean needToChangeJunitVersion = false;
        if (junitMajorVersion == 4 && junitMinorVersion < 13) {
          needToChangeJunitVersion = true;
        }
        if (junitMajorVersion < 4) {
          needToChangeJunitVersion = true;
        }
        if (needToChangeJunitVersion) {
          boolean replacedJunitInPomFile = replaceOrAddJunitInPomFile(experimentPomFile);
          if (!replacedJunitInPomFile) {
            String errorMessage = "ERROR: Could not replace junit";
            System.out.println(errorMessage);
            result.setIssueMessage(errorMessage);
            result.setIssueChangingJunit(true);
            return result;
          }
        }
      }

      //fix junit transitive dependencies
      if (ARUS.REPO_NAME.equals("amazon-ecs-plugin")) {
        excludeLibraryFromLibraryIfNeeded(experimentPomFile, "junit", "junit", "org.hamcrest", "hamcrest-core");
      }

      /////////////////////////////////do not run tests in parallel to avoid logging issues////////////////////////////////////////////
      //check whether tests are run in parallel if yes, do not consider project
      boolean runsTestsInParallel = checkTestsInParallel(experimentPomFile);
      if (runsTestsInParallel) {
        boolean removedTestsInParallel = removeTestsInParallel(experimentPomFile);
        if (!removedTestsInParallel) {
          String errorMessage = "ERROR: Could not remove attributes to avoid running tests in parallel";
          System.out.println(errorMessage);
          result.setIssueMessage(errorMessage);
          result.setIssueHandlingParallelTests(true);
          return result;
        }
      }

      setThatTestsAreRunSequentially(experimentPomFile, ARUS.REPO_NAME);
      setFailOnWarningToFalse(experimentPomFile);
      removeCompilerArgInPlugin(experimentPomFile, "org.apache.maven.plugins", "maven-compiler-plugin", "-Werror");


      //////////////////////replace original mockito with customized mockito//////////////////////////////////////

      /////before replacing save a copy of experimentPomFile
      File copyExpPomFile = new File (copyExperimentPomFile);
      if(copyExpPomFile.exists()){
        copyExpPomFile.delete();
      }
      Files.copy(new File(experimentPomFile).toPath(), copyExpPomFile.toPath());

      boolean replacedMockitoWithCustomizedMockitoInPomFile = replaceOrAddMockitoWithCustomizedMockitoInPomFile(experimentPomFile, mockitoMajorVersion, junitMajorVersion, ARUS.REPO_NAME);
      if (!replacedMockitoWithCustomizedMockitoInPomFile) {
        String errorMessage = "ERROR: Could not replace mockito with customized mockito";
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueWithCustomizedMockito(true);
        return result;
      }


      //////////////////////make changes to test code for mockito logging///////////////////////////////////////
      //get list of test files
      for (String javaFileName : javaFileNamesInTestsFolder) {
        Set<String> classesNamesContainingTests = FileUtils.getClassNamesContainingTests(javaFileName);
        if (FileUtils.replaceFile(ARUS.REPO_NAME, javaFileName, ADDITIONAL_FILES_FOLDER_NAME)) {
          continue;
        }
        for (String className : classesNamesContainingTests) {
          if (FileUtils.checkIfClassHasTests(javaFileName, className)) {
            if (ARUS.REPO_NAME.equals("allure-bamboo") && javaFileName.endsWith("ExceptionUtilTest.java")) {
              continue;
            }
            boolean replacedRunWith = FileUtils.replaceRunWithMockitoJUnitRunnerIfNecessary(javaFileName, className);
            if (!replacedRunWith) {
              System.out.println("ERROR: Issue adding mockito rule");
              result.setIssueAddingMockitoCode(true);
            }
            if (junitMajorVersion == 4) {
              boolean addedRule = FileUtils.addMockitoRule(javaFileName, className);
              if (!addedRule) {
                System.out.println("ERROR: Issue adding mockito rule");
                result.setIssueAddingMockitoCode(true);
              }
              if (FileUtils.checkIfHasTestsAndConstructor(javaFileName, className)) {
                if (FileUtils.checkIfHasRunWithOtherThanMockitoJunitRunner(javaFileName, className)) {
                  FileUtils.addConstructorLogging(javaFileName, className);
                } else {
                  FileUtils.addRunWithAnnotation(javaFileName, className);
                }
              }
              FileUtils.undoInitializationOfFieldsAnnotatedWithAtMock(ARUS.REPO_NAME, javaFileName, className);
            } else if (junitMajorVersion == 5) {
              FileUtils.addMockitoAnnotationsIfNecessary(javaFileName, className);
            }
          }
        }
      }


      //////////////////////remove previous experiment results///////////////////////////////////////
      //remove customized mockito result file from previous experiments
      String customizedMockitoResultFileName = File.separator + "tmp" + File.separator + "mel.txt";
      File customizedMockitoResultFile = new File(customizedMockitoResultFileName);
      if (customizedMockitoResultFile.exists()) {
        if (!customizedMockitoResultFile.delete()) {
          String errorMessage = "ERROR: Failed to delete customized mockito result file";
          System.out.println(errorMessage);
          result.setIssueMessage(errorMessage);
          result.setIssueWithCustomizedMockito(true);
          return result;
        }
      }
      //create empty /mel.txt file
      File tmpMelFile = new File(customizedMockitoResultFileName);
      tmpMelFile.createNewFile();

      //////////////////////run tests with customized mockito///////////////////////////////////////
      //run tests after adding customized mockito
      long customizedMockitoTestsTimeStart = System.currentTimeMillis();
      List<TestExecutionResult> customizedMockitoExecutionResults = MavenUtils.runTestsWithMaven(ARUS.MAVEN_HOME, experimentPomFile,
              experimentSurfireDirectory, 1, mavenOptions);
      if (customizedMockitoExecutionResults.size() == 0) {
        String errorMessage = "ERROR: No test results for experiment project running with customized mockito";
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueWithCustomizedMockito(true);
        return result;
      }
      TestExecutionResult customizedMockitoExecutionResult = customizedMockitoExecutionResults.get(0);
      if (!customizedMockitoExecutionResult.isMavenSuccess() || customizedMockitoExecutionResult.getFailed() > 0 || customizedMockitoExecutionResult.getErrors() > 0) {
        String errorMessage = "ERROR: Problems with tests in experiment project running customized mockito";
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueWithCustomizedMockito(true);
        return result;
      }

      //read content of the mockito results file
      int customizedMockitoTestsPassed = customizedMockitoExecutionResult.getExecutions()-customizedMockitoExecutionResult.getFailed()-customizedMockitoExecutionResult.getErrors()-customizedMockitoExecutionResult.getSkipped();
      //mf: we cannot compare tests in the trace because tests through inheritance and tests using @TestFactory are not easy to intercept, because of those cases, the traces might be malformed.
      if (originalTestsPassedCount != customizedMockitoTestsPassed) {
        System.out.println("ERROR: different number of tests passes when adding customized mockito");
        result.setIssueWithCustomizedMockito(true);
      }

      //copy result file in experiment project directory
      customizedMockitoResultFile = new File(customizedMockitoResultFileName);
      File customizedMockitoResultFileInExperimentProject = new File(customizedMockitoResultFileNameInExperimentProject);
      org.apache.commons.io.FileUtils.copyFile(customizedMockitoResultFile, customizedMockitoResultFileInExperimentProject);

      //run tests again to compute average time
      List<TestExecutionResult> customizedMockitoExecutionResultsForAverageTime = MavenUtils.runTestsWithMaven(ARUS.MAVEN_HOME, experimentPomFile,
              experimentSurfireDirectory, ARUS.TEST_EXECUTION_REPETITIONS-1, mavenOptions);
      if (customizedMockitoExecutionResultsForAverageTime.size() == 0) {
        String errorMessage = "ERROR: No test results for experiment project running with customized mockito for average time";
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueWithCustomizedMockito(true);
        return result;
      }
      long customizedMockitoTestsTimeEnd = System.currentTimeMillis();
      if (customizedMockitoResultFile.exists()) {
        if (!customizedMockitoResultFile.delete()) {
          String errorMessage = "ERROR: Failed to delete customized mockito result file after running tests with customized mockito";
          System.out.println(errorMessage);
          result.setIssueMessage(errorMessage);
          result.setIssueWithCustomizedMockito(true);
          return result;
        }
      }
      //compute test execution time
      customizedMockitoTestsTime = customizedMockitoTestsTimeEnd - customizedMockitoTestsTimeStart;
      analysisStatistics.setCustomizedMockitoTestsTime(customizedMockitoTestsTime);
      customizedMockitoTestsPassed = AnalysisUtils.checkConsistencyOfExecutionResults(customizedMockitoExecutionResultsForAverageTime);
      if(customizedMockitoTestsPassed==-1){
        String errorMessage = "ERROR: problems of consistency check when running tests with customized mockito";
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueWithCustomizedMockito(true);
        return result;
      }
      analysisStatistics.setCustomizedMockitoTestsPassedCount(customizedMockitoTestsPassed);
      customizedMockitoExecutionResultsForAverageTime.addAll(customizedMockitoExecutionResults);

      //////////////////////parse mockito log and do sanity checks///////////////////////////////////////
      //read customized mockito trace
      List<TestAnalysis> testAnalyses = new ArrayList<TestAnalysis>();
      try {
        testAnalyses = CustomizedMockitoUtils.parseCustomizedMockitoTrace(customizedMockitoResultFileNameInExperimentProject);
      } catch (Exception e) {
        System.out.println("ERROR:");
        e.printStackTrace(System.out);
        String errorMessage = "ERROR: Could not get dependency tree";
        System.out.println(errorMessage);
        result.setIssueMessage(errorMessage);
        result.setIssueInTrace(true);
        return result;
      }

      //sanity checks on test analyses
      for (TestAnalysis testAnalysis : testAnalyses) {
        if (!testAnalysis.performSanityChecks()) {
          String errorMessage = "ERROR: issue performing sanity checks on trace content";
          System.out.println(errorMessage);
          result.setIssueMessage(errorMessage);
          result.setIssueInTrace(true);
          return result;
        }
      }

      //////////////////////print unused stubs///////////////////////////////////////
      //printing unused stubs

      int unusedStubsCount = 0;
      Map<String, Integer> usmap = new HashMap<>();
      for (TestAnalysis testAnalysis : testAnalyses) {
        for (UnusedStub unusedStub : testAnalysis.getUnusedStubs()) {
          if(usmap.keySet().contains(unusedStub.getInfo())){
            int count = usmap.get(unusedStub.getInfo());
            count++;
            usmap.put(unusedStub.getInfo(), count);
          }
          else{
            usmap.put(unusedStub.getInfo(), 1);
          }
          //System.out.println("UNUSED:" + unusedStub.getStubbingLocation().getFileName() + "#" + unusedStub.getStubbingLocation().getLineNum());
          unusedStubsCount++;
        }
      }
//      System.out.println(usmap);
      List<String> sortedKeys = new ArrayList<>(usmap.keySet());
      Collections.sort(sortedKeys);
      // Create the directories if they don't exist
      File dir = new File(ARUS.REPO_FULL_PATH + File.separator +"results");
      if (!dir.exists()) {
        dir.mkdirs();
      }
      File outputFile = new File(dir,ARUS.REPO_NAME.replace("/", "-")+".txt");
//      System.out.println(outputFile.getAbsolutePath());
      try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
        for (String key : sortedKeys) {
//          System.out.println(key + " -> " + usmap.get(key));
//          writer.println(key + " -> " + usmap.get(key));
        }
      } catch (IOException e) {
        System.out.println("An error occurred while writing to the file: " + e.getMessage());
      }
      analysisStatistics.setUSOB(unusedStubsCount);
      /////create result directory
      File resultDir = new File("/tmp/umnResults");
      if (!resultDir.exists()){
        resultDir.mkdirs();
      }

      /////////////////unused stub analysis///////////////////////////////////////////////
      if (completeAnalysis) {
        /////////////////find stubs that should be changed from thenReturn to doReturn////////////////////////
        List<StubCreation> stubCreationsThatShouldBeChanged = new ArrayList<StubCreation>();
        for (TestAnalysis testAnalysis : testAnalyses) {
          stubCreationsThatShouldBeChanged.addAll(testAnalysis.findStubsThatShouldBeChanged());
        }

        for (String testFileName : javaFileNamesInTestsFolder) {
          CompilationUnit cu = StaticJavaParser.parse(new File(testFileName));
          boolean needAddImports = false;
          //preserve the format of the original code
          LexicalPreservingPrinter.setup(cu);
          for (StubCreation stubCreationThatShouldBeChanged : stubCreationsThatShouldBeChanged) {
            if (testFileName.contains(stubCreationThatShouldBeChanged.getStubbingLocation().getFileName())) {
//                System.out.println("CHANGE:"+stubCreationThatShouldBeChanged.getStubbingLocation().getFileName()+"#"+stubCreationThatShouldBeChanged.getStubbingLocation().getLineNum()+"#"+stubCreationThatShouldBeChanged.getStubbedMethodClassName()+"#"+stubCreationThatShouldBeChanged.getStubbedMethodName());
              //transform the file
              cu = FileUtils.transformThenReturnToDoReturn(cu, stubCreationThatShouldBeChanged.getStubbingLocation().getLineNum(), stubCreationThatShouldBeChanged.getStubbedMethodName());
              needAddImports = true;
            }
          }
          if(needAddImports){
            cu.addImport("org.mockito.Mockito.doReturn",true,false);
            cu.addImport("org.mockito.Mockito.when",true,false);
          }
          Files.write(new File(testFileName).toPath(), Collections.singleton(LexicalPreservingPrinter.print(cu)), StandardCharsets.UTF_8);
        }

        ///////////////////////recompute unused stub information/////////////////////////////////
        long testsTimeAfterThenReturnToDoReturnChangesStart = System.currentTimeMillis();
        if (stubCreationsThatShouldBeChanged.size() > 0) {
          //remove customized mockito result file from tmp
          if (customizedMockitoResultFile.exists()) {
            if (!customizedMockitoResultFile.delete()) {
              String errorMessage = "ERROR: Failed to delete customized mockito result file after changing thenReturn to doReturn";
              System.out.println(errorMessage);
              result.setIssueMessage(errorMessage);
              result.setIssueWithCustomizedMockito(true);
              return result;
            }
          }

          //check if all test still pass
          List<TestExecutionResult> transformUtilExecutionResults = MavenUtils.runTestsWithMaven(ARUS.MAVEN_HOME, experimentPomFile,
                  experimentSurfireDirectory, 1, mavenOptions);
          if (transformUtilExecutionResults.size() == 0) {
            String errorMessage = "ERROR: No test results for experiment project after changing thenReturn to doReturn";
            System.out.println(errorMessage);
            result.setIssueMessage(errorMessage);
            result.setIssueWithCustomizedMockito(true);
            return result;
          }
          //check test results
          TestExecutionResult transformUtilExecutionResult = transformUtilExecutionResults.get(0);
          if (!transformUtilExecutionResult.isMavenSuccess() || transformUtilExecutionResult.getFailed() > 0 || transformUtilExecutionResult.getErrors() > 0) {
            String errorMessage = "ERROR: Problems with tests in experiment project  after changing thenReturn to doReturn";
            System.out.println(errorMessage);
            result.setIssueMessage(errorMessage);
            result.setIssueWithCustomizedMockito(true);
            return result;
          }
          //remove customized mockito result file from previous experiments
          if (customizedMockitoResultFileInExperimentProject.exists()) {
            if (!customizedMockitoResultFileInExperimentProject.delete()) {
              String errorMessage = "ERROR: Failed to delete mel.txt in experiment project after changing thenReturn to doReturn";
              System.out.println(errorMessage);
              result.setIssueMessage(errorMessage);
              result.setIssueWithCustomizedMockito(true);
              return result;
            }
          }
          //copy mel.txt from tmp to experiment
          customizedMockitoResultFile = new File(customizedMockitoResultFileName);
          File customizedMockitoResultFileAfterTransforming = new File(customizedMockitoResultFileNameInExperimentProject);
          org.apache.commons.io.FileUtils.copyFile(customizedMockitoResultFile, customizedMockitoResultFileAfterTransforming);
          //parse trace
          try {
            testAnalyses = CustomizedMockitoUtils.parseCustomizedMockitoTrace(customizedMockitoResultFileNameInExperimentProject);

          } catch (Exception e) {
            String errorMessage = "ERROR: After changing thenReturn to doReturn I get" + System.lineSeparator() + e.getMessage();
            System.out.println(errorMessage);
            result.setIssueMessage(errorMessage);
            result.setIssueInTrace(true);
            return result;
          }
          //sanity checks on test analyses
          for (TestAnalysis transformTestAnalysis : testAnalyses) {
            if (!transformTestAnalysis.performSanityChecks()) {
              String errorMessage = "ERROR: issue performing sanity checks on trace content after changing thenReturn to doReturn";
              System.out.println(errorMessage);
              result.setIssueMessage(errorMessage);
              result.setIssueInTrace(true);
              return result;
            }
          }
        }
        long testsTimeAfterThenReturnToDoReturnChangesEnd = System.currentTimeMillis();
        long testsTimeAfterThenReturnToDoReturnChanges = testsTimeAfterThenReturnToDoReturnChangesEnd - testsTimeAfterThenReturnToDoReturnChangesStart;
        int thenReturnToDoReturnChanges = stubCreationsThatShouldBeChanged.size();
        analysisStatistics.setTestsTimeAfterThenReturnToDoReturnChanges(testsTimeAfterThenReturnToDoReturnChanges);
        analysisStatistics.setThenReturnToDoReturnChangesCount(thenReturnToDoReturnChanges);

        System.out.println("Start to identify and remove US");

        long modificationExecutionTimeStart = System.currentTimeMillis();
        //fix unnecessary stubs and get a list of changed files' names
        switch (SOLUTION_NUMBER) {
          case 1:
            /////////////////////collect case numbers////////////////////
            SolutionB.identifyUnusedStubs(testAnalyses, javaFileNamesInTestsFolder, new FixUnusedStubsState(), analysisStatistics);
            SolutionA.fixUnusedStubs(testAnalyses, javaFileNamesInTestsFolder, analysisStatistics, REMOVE_UUS);
            break;
          case 2:
            SolutionB.fixUnusedStubs(testAnalyses, javaFileNamesInTestsFolder, analysisStatistics,REMOVE_UUS);
            break;
          case 3:
            SolutionC.fixUnusedStubs(testAnalyses, javaFileNamesInTestsFolder, analysisStatistics,REMOVE_UUS);
            break;
        }

        long modificationExecutionTimeEnd = System.currentTimeMillis();
        modificationExecutionTime = modificationExecutionTimeEnd - modificationExecutionTimeStart;
        analysisStatistics.setModificationExecutionTime(modificationExecutionTime);

        //////////////////run the tests again to do a sanity check on whether we removed all unused stubs////////////////////////
        //remove customized mockito result file from tmp
        if (customizedMockitoResultFile.exists()) {
          if (!customizedMockitoResultFile.delete()) {
            String errorMessage = "ERROR: Failed to delete customized mockito result file";
            System.out.println(errorMessage);
            result.setIssueMessage(errorMessage);
            result.setIssueWithCustomizedMockito(true);
            return result;
          }
        }
        //check if all test still pass
        long customizedMockitoSanityCheckTestsTimeStart = System.currentTimeMillis();
        List<TestExecutionResult> removeStubUtilExecutionResults = MavenUtils.runTestsWithMaven(ARUS.MAVEN_HOME, experimentPomFile,
                experimentSurfireDirectory, 1, mavenOptions);
        if (removeStubUtilExecutionResults.size() == 0) {
          String errorMessage = "ERROR: No test results for experiment project after removing";
          System.out.println(errorMessage);
          result.setIssueMessage(errorMessage);
          result.setIssueWithCustomizedMockito(true);
          return result;
        }
        long customizedMockitoSanityCheckTestsTimeEnd = System.currentTimeMillis();
        TestExecutionResult removeStubUtilExecutionResult = removeStubUtilExecutionResults.get(0);
        if (!removeStubUtilExecutionResult.isMavenSuccess() || removeStubUtilExecutionResult.getFailed() > 0 || removeStubUtilExecutionResult.getErrors() > 0) {
          String errorMessage = "ERROR: Problems with tests in experiment project after removing";
          System.out.println(errorMessage);
          result.setIssueMessage(errorMessage);
          result.setIssueWithCustomizedMockito(true);
          return result;
        }
        //remove customized mockito result file from previous experiments
        if (customizedMockitoResultFileInExperimentProject.exists()) {
          if (!customizedMockitoResultFileInExperimentProject.delete()) {
            String errorMessage = "ERROR: Failed to delete mel.txt in experiment project";
            System.out.println(errorMessage);
            result.setIssueMessage(errorMessage);
            result.setIssueWithCustomizedMockito(true);
            return result;
          }
        }
        //copy mel.txt from tmp to experiment
        customizedMockitoResultFile = new File(customizedMockitoResultFileName);
        File customizedMockitoResultFileAfterRemoving = new File(customizedMockitoResultFileNameInExperimentProject);
        org.apache.commons.io.FileUtils.copyFile(customizedMockitoResultFile, customizedMockitoResultFileAfterRemoving);

        long customizedMockitoSanityCheckTestsTime = customizedMockitoSanityCheckTestsTimeEnd - customizedMockitoSanityCheckTestsTimeStart;
        analysisStatistics.setCustomizedMockitoSanityCheckTestsTime(customizedMockitoSanityCheckTestsTime);

        //read customized mockito trace after transforming
        List<TestAnalysis> removeTestAnalyses = new ArrayList<TestAnalysis>();
        try {
          removeTestAnalyses = CustomizedMockitoUtils.parseCustomizedMockitoTrace(customizedMockitoResultFileNameInExperimentProject);
//        System.out.println(transformTestAnalyses);
        } catch (Exception e) {
          String errorMessage = "ERROR: " + e.getMessage();
          System.out.println(errorMessage);
          result.setIssueMessage(errorMessage);
          result.setIssueInTrace(true);
          return result;
        }
        //sanity checks on test analyses
        for (TestAnalysis removeTestAnalysis : removeTestAnalyses) {
          if (!removeTestAnalysis.performSanityChecks()) {
            String errorMessage = "ERROR: issue performing sanity checks on trace content";
            System.out.println(errorMessage);
            result.setIssueMessage(errorMessage);
            result.setIssueInTrace(true);
            return result;
          }
        }
        //we should check no UNUSED stubs anymore
        int unusedStubsRemainingOccurCount = 0;
        Set<String> unusedStubsRemainingDefinCount = new HashSet<>();
        for (TestAnalysis ta : removeTestAnalyses) {
          for(UnusedStub us: ta.getUnusedStubs()){
            unusedStubsRemainingDefinCount.add(us.getInfo());
            unusedStubsRemainingOccurCount++;
            if(!us.getStubbingLocation().getMethodName().equals("historyOf")){
              for(StackComponent sct: us.getStubbingLocationStack()){
                System.out.println(sct.getClassName()+" "+sct.getMethodName()+" "+sct.getLineInvokedInMethod());
              }
            }
          }
        }

        if (unusedStubsRemainingOccurCount!=0) {
          analysisStatistics.setUSDA(unusedStubsRemainingDefinCount.size());
          analysisStatistics.setUSOA(unusedStubsRemainingOccurCount);
          System.out.println("ERROR: "+ unusedStubsRemainingDefinCount.size()+ " (DEF) " +unusedStubsRemainingOccurCount+ " (OCC) " +" US are not removed");
        } else {
          System.out.println("SUCCEED");
        }
        /////calculate the total analysis time
        long totalAnalysisTimeEnd = System.currentTimeMillis();
        totalAnalysisTime = totalAnalysisTimeEnd - totalAnalysisTimeStart;
        analysisStatistics.setTotalAnalysisTime(totalAnalysisTime);
        result.setAnalysisFullyCompleted(true);


        File expPomFile = new File(experimentPomFile);
        if(expPomFile.exists()){
          expPomFile.delete();
        }
        //run tests after removing stubs
        long testWithOriginalMockitoTimeStart = System.currentTimeMillis();

        Files.copy(copyExpPomFile.toPath(), expPomFile.toPath());
        List<TestExecutionResult> originalMockitoExecutionResultsAfterRemoving = MavenUtils.runTestsWithMaven(ARUS.MAVEN_HOME, experimentPomFile,
                experimentSurfireDirectory, 1, mavenOptions);
        if (originalMockitoExecutionResultsAfterRemoving.size() == 0) {
          String errorMessage = "ERROR: No test results for experiment project running with original mockito after removing";
          System.out.println(errorMessage);
          result.setIssueMessage(errorMessage);
          result.setIssueWithOriginalMockito(true);
          return result;
        }
        TestExecutionResult originalMockitoExecutionResult = originalMockitoExecutionResultsAfterRemoving.get(0);
        if (!originalMockitoExecutionResult.isMavenSuccess() || originalMockitoExecutionResult.getFailed() > 0 || originalMockitoExecutionResult.getErrors() > 0) {
          String errorMessage = "ERROR: Problems with tests in experiment project running original mockito after removing";
          System.out.println(errorMessage);
          result.setIssueMessage(errorMessage);
          result.setIssueWithOriginalMockito(true);
          return result;
        }

        //read content of the mockito results file after removing the test
        int originalMockitoTestsPassedAfterRemovingCount = originalMockitoExecutionResult.getExecutions()-originalMockitoExecutionResult.getFailed()-originalMockitoExecutionResult.getErrors()-originalMockitoExecutionResult.getSkipped();
        if (originalTestsPassedCount != originalMockitoTestsPassedAfterRemovingCount-analysisStatistics.getAddedTestCount()) {
          System.out.println("ERROR: different number of tests passes after changing back to original mockito"+originalTestsPassedCount+" "+ originalMockitoTestsPassedAfterRemovingCount+ " "+ analysisStatistics.getAddedTestCount());
          result.setIssueWithOriginalMockito(true);
        }

        //run tests again to compute average time
        List<TestExecutionResult> originalMockitoExecutionResultsForAverageTime = MavenUtils.runTestsWithMaven(ARUS.MAVEN_HOME, experimentPomFile,
                experimentSurfireDirectory, ARUS.TEST_EXECUTION_REPETITIONS-1, mavenOptions);
        if (originalMockitoExecutionResultsForAverageTime.size() == 0) {
          String errorMessage = "ERROR: No test results for experiment project running with original mockito for average time after removing us";
          System.out.println(errorMessage);
          result.setIssueMessage(errorMessage);
          result.setIssueWithOriginalMockito(true);
          return result;
        }
        long testWithOriginalMockitoTimeEnd = System.currentTimeMillis();
        long originalMockitoTestsTime = testWithOriginalMockitoTimeEnd - testWithOriginalMockitoTimeStart;
        analysisStatistics.setOriginalMockitoTestsTime(originalMockitoTestsTime);
        int originalMockitoTestsPassed = AnalysisUtils.checkConsistencyOfExecutionResults(originalMockitoExecutionResultsForAverageTime);
        if(originalMockitoTestsPassed==-1){
          String errorMessage = "ERROR: problems of consistency check when running tests with original mockito after experiment";
          System.out.println(errorMessage);
          result.setIssueMessage(errorMessage);
          result.setIssueWithOriginalMockito(true);
          return result;
        }
        analysisStatistics.setOriginalMockitoTestsPassedCount(originalTestsPassedCount);
        originalMockitoExecutionResultsAfterRemoving.addAll(originalMockitoExecutionResultsAfterRemoving);
      }

      result.setAnalysisStatistics(analysisStatistics);
      System.out.println("Unnecessary Stubbing Definitions before runnning ARUS: "+analysisStatistics.getUSDB());
      System.out.println("Unnecessary Stubbing Occurrences before runnning ARUS: "+analysisStatistics.getUSOB() );
      System.out.println("Unnecessary Stubbing Definitions after runnning ARUS: "+analysisStatistics.getUSDA());
      System.out.println("Unnecessary Stubbing Occurrences after runnning ARUS: "+ analysisStatistics.getUSOA());

      //get list of test files
      for (String javaFileName : javaFileNamesInTestsFolder) {
        Set<String> classesNamesContainingTests = FileUtils.getClassNamesContainingTests(javaFileName);
        for (String className : classesNamesContainingTests) {
          FileUtils.setMockitoRule(javaFileName, className);
        }
      }

      System.out.println("Analyzed:" + ARUS.REPO_NAME);
    } catch (Exception e) {
      StringWriter errorWriter = new StringWriter();
      PrintWriter errorPrintWriter = new PrintWriter(errorWriter);
      e.printStackTrace(errorPrintWriter);
      String errorMessage = "ERROR: Exception while processing project" + System.lineSeparator() + errorWriter.toString();
      System.out.println(errorMessage);
      result.setIssueMessage(errorMessage);
      result.setOtherException(true);
      return result;
    }
    return result;
  }

  private boolean checkMockitoVersion(String mockitoDep) {
    boolean result = false;
    if (mockitoDep.equals("")) {
      return result;
    }
    String mockitoDepArray[] = mockitoDep.split(":");
    String mockitoVersionItems[] = mockitoDepArray[3].split("\\.");
    int mainVersionNumber = Integer.parseInt(mockitoVersionItems[0]);
    if (mainVersionNumber == 2) {
      int subVersionNumber = Integer.parseInt(mockitoVersionItems[1]);
      if (subVersionNumber >= 3) {
        result = true;
        return result;
      }
    } else if (mainVersionNumber == 3) {
      result = true;
      return result;
    } else if (mainVersionNumber == 4) {
      result = true;
      return result;
    }
    return result;
  }


  private boolean checkTestsInParallel(String pomFileName) {
    boolean parallel = false;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      String xmlContent = new String(Files.readAllBytes(Paths.get(pomFileName)));
      Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
      Element rootElement = document.getDocumentElement();
      NodeList buildNodeList = rootElement.getElementsByTagName("build");
      for (int i = 0; i < buildNodeList.getLength(); ++i) {
        Node buildNode = buildNodeList.item(i);
        if (!(buildNode instanceof Element)) {
          continue;
        }
        Element buildElement = (Element) buildNode;
        NodeList pluginsNodeList = buildElement.getElementsByTagName("plugins");
        for (int j = 0; j < pluginsNodeList.getLength(); ++j) {
          Node pluginsNode = pluginsNodeList.item(j);
          if (!(pluginsNode instanceof Element)) {
            continue;
          }
          Element pluginsElement = (Element) pluginsNode;
          NodeList pluginNodeList = pluginsElement.getElementsByTagName("plugin");
          for (int k = 0; k < pluginNodeList.getLength(); ++k) {
            Node pluginNode = pluginNodeList.item(k);
            if (!(pluginNode instanceof Element)) {
              continue;
            }
            Element pluginElement = (Element) pluginNode;
            boolean isOrgApacheMavenPluginsGroupId = false;
            boolean isMavenSurfirePluginArtifactId = false;
            NodeList groupIdNodeList = pluginElement.getElementsByTagName("groupId");
            for (int l = 0; l < groupIdNodeList.getLength(); ++l) {
              Node groupIdNode = groupIdNodeList.item(l);
              if (!(groupIdNode instanceof Element)) {
                continue;
              }
              Element groupIdElement = (Element) groupIdNode;
              if (groupIdElement.getTextContent().trim().equals("org.apache.maven.plugins")) {
                isOrgApacheMavenPluginsGroupId = true;
              }
            }
            NodeList artifactIdNodeList = pluginElement.getElementsByTagName("artifactId");
            for (int l = 0; l < artifactIdNodeList.getLength(); ++l) {
              Node artifactIdNode = artifactIdNodeList.item(l);
              if (!(artifactIdNode instanceof Element)) {
                continue;
              }
              Element artifactIdElement = (Element) artifactIdNode;
              if (artifactIdElement.getTextContent().trim().equals("maven-surefire-plugin")) {
                isMavenSurfirePluginArtifactId = true;
              }
            }
            if (isOrgApacheMavenPluginsGroupId && isMavenSurfirePluginArtifactId) {
              NodeList configurationNodeList = pluginElement.getElementsByTagName("configuration");
              List<String> nodeNamesToConsider = new ArrayList<String>();
              nodeNamesToConsider.add("parallel");
              nodeNamesToConsider.add("parallelOptimized");
              nodeNamesToConsider.add("parallelTestsTimeoutForcedInSeconds");
              nodeNamesToConsider.add("parallelTestsTimeoutInSeconds");
              nodeNamesToConsider.add("perCoreThreadCount");
              nodeNamesToConsider.add("threadCount");
              nodeNamesToConsider.add("threadCountClasses");
              nodeNamesToConsider.add("threadCountMethods");
              nodeNamesToConsider.add("threadCountSuites");
              nodeNamesToConsider.add("useUnlimitedThreads");
              for (int l = 0; l < configurationNodeList.getLength(); ++l) {
                Node configurationNode = configurationNodeList.item(l);
                if (!(configurationNode instanceof Element)) {
                  continue;
                }
                Element configurationElement = (Element) configurationNode;
                for (String nodeName : nodeNamesToConsider) {
                  NodeList nodeList = configurationElement.getElementsByTagName(nodeName);
                  if (nodeList.getLength() > 0) {
                    parallel = true;
                    return parallel;
                  }
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      System.out.println("ERROR: Error while checking if project runs tests in parallel, assuming it does");
      parallel = true;
    }
    return parallel;
  }

  private void setFailOnWarningToFalse(String pomFileName) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      String xmlContent = new String(Files.readAllBytes(Paths.get(pomFileName)));
      Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
      Element rootElement = document.getDocumentElement();
      NodeList failOnWarningNodeList = rootElement.getElementsByTagName("failOnWarning");
      for (int i = 0; i < failOnWarningNodeList.getLength(); ++i) {
        Node failOnWarningNode = failOnWarningNodeList.item(i);
        if (!(failOnWarningNode instanceof Element)) {
          continue;
        }
        //check plugin is inside plugins
        Element failOnWarningElement = (Element) failOnWarningNode;
        failOnWarningElement.setTextContent("false");
      }
      String newXMlDocString = getXMLString(document);
      if (newXMlDocString.equals("")) {
        System.out.println("WARNING: XML document is empty");
        throw new RuntimeException();
      } else {
        File modifiedPomFile = new File(pomFileName);
        FileWriter modifiedPomFileWriter = new FileWriter(modifiedPomFile, false);
        modifiedPomFileWriter.write(newXMlDocString);
        modifiedPomFileWriter.close();
      }
    } catch (Exception e) {
      System.out.println("WARNING: Could not set set failOnWarning to false");
    }
  }

  private void setThatTestsAreRunSequentially(String pomFileName, String repoName) {
    if (repoName.equals("mweirauch/micrometer-jvm-extras") || repoName.equals("blockchain-jd-com/bftsmart")) {
      Node buildNodeGlobal = null;
      Node pluginsNodeGlobal = null;
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        String xmlContent = new String(Files.readAllBytes(Paths.get(pomFileName)));
        Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
        Element rootElement = document.getDocumentElement();
        NodeList pluginNodeList = rootElement.getElementsByTagName("plugin");
        for (int i = 0; i < pluginNodeList.getLength(); ++i) {
          Node pluginNode = pluginNodeList.item(i);
          if (!(pluginNode instanceof Element)) {
            continue;
          }
          //check plugin is inside plugins
          Element pluginElement = (Element) pluginNode;
          Node pluginParentNode = pluginElement.getParentNode();
          if (pluginParentNode == null || !(pluginParentNode instanceof Element)) {
            continue;
          }
          Element pluginParentElement = (Element) pluginParentNode;
          if (!pluginParentElement.getTagName().equals("plugins")) {
            continue;
          }
          Node pluginsNode = pluginParentElement;
          if (pluginsNodeGlobal == null && pluginsNode != null) {
            pluginsNodeGlobal = pluginsNode;
          }
          //check plugins is inside build
          Node pluginsParentNode = pluginsNode.getParentNode();
          if (pluginsParentNode == null || !(pluginsParentNode instanceof Element)) {
            continue;
          }
          Element pluginsParentElement = (Element) pluginsParentNode;
          if (!pluginsParentElement.getTagName().equals("build")) {
            continue;
          }
          Node buildNode = pluginsParentElement;
          if (buildNodeGlobal == null && buildNode != null) {
            buildNodeGlobal = buildNode;
          }
          //check for groupId
          NodeList childNodesOfPlugin = pluginElement.getChildNodes();
          List<Node> groupIdNodeList = new ArrayList<Node>();
          for (int j = 0; j < childNodesOfPlugin.getLength(); ++j) {
            Node childNode = childNodesOfPlugin.item(j);
            if (childNode instanceof Element) {
              Element childElement = (Element) childNode;
              if (childElement.getTagName().equals("groupId")) {
                groupIdNodeList.add(childNode);
              }
            }
          }
          if (groupIdNodeList.size() != 1) {
            continue;
          }
          Node groupIdNode = groupIdNodeList.get(0);
          Element groupIdElement = (Element) groupIdNode;
          if (!groupIdElement.getTextContent().equals("org.apache.maven.plugins")) {
            continue;
          }
          //check for artifactId
          List<Node> artifactIdNodeList = new ArrayList<Node>();
          for (int j = 0; j < childNodesOfPlugin.getLength(); ++j) {
            Node childNode = childNodesOfPlugin.item(j);
            if (childNode instanceof Element) {
              Element childElement = (Element) childNode;
              if (childElement.getTagName().equals("artifactId")) {
                artifactIdNodeList.add(childNode);
              }
            }
          }
          if (artifactIdNodeList.size() != 1) {
            continue;
          }
          Node artifactIdNode = artifactIdNodeList.get(0);
          Element artifactIdElement = (Element) artifactIdNode;
          if (!artifactIdElement.getTextContent().equals("maven-surefire-plugin")) {
            continue;
          }
          //check for configuration
          List<Node> configurationNodeList = new ArrayList<Node>();
          for (int j = 0; j < childNodesOfPlugin.getLength(); ++j) {
            Node childNode = childNodesOfPlugin.item(j);
            if (childNode instanceof Element) {
              Element childElement = (Element) childNode;
              if (childElement.getTagName().equals("configuration")) {
                configurationNodeList.add(childNode);
              }
            }
          }
          if (configurationNodeList.size() == 1) {
            //fix sequential run tags
            Node configurationNode = configurationNodeList.get(0);
            Element configurationElement = (Element) artifactIdNode;
            List<String> tags = new ArrayList<String>();
            tags.add("forkCount");
            tags.add("reuseForks");
            removeTags(configurationElement, tags);
            addTag(document, configurationElement, "forkCount", "1");
            addTag(document, configurationElement, "reuseForks", "false");
            //get string and rewrite file
            String newXMlDocString = getXMLString(document);
            if (newXMlDocString.equals("")) {
              System.out.println("WARNING: XML document is empty");
            } else {
              File modifiedPomFile = new File(pomFileName);
              FileWriter modifiedPomFileWriter = new FileWriter(modifiedPomFile, false);
              modifiedPomFileWriter.write(newXMlDocString);
              modifiedPomFileWriter.close();
            }
            return;
          } else if (configurationNodeList.size() == 0) {
            addSequentialTestsConfiguration(document, pluginElement);
            return;
          } else {
            System.out.println("WARNING: More than one configuration node in surefire plugin");
            return;
          }
        }
        //getting here only if ni
        addMavenSurefirePluginForSequentialTestsConfiguration(pomFileName, document, rootElement, buildNodeGlobal, pluginsNodeGlobal);
      } catch (Exception e) {
        System.out.println("WARNING: Could not set sequential test run");
      }
    }
  }

  private void removeCompilerArgInPlugin(String pomFileName, String pluginGroupId, String pluginArtifactId, String argValue) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      String xmlContent = new String(Files.readAllBytes(Paths.get(pomFileName)));
      Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
      Element rootElement = document.getDocumentElement();
      NodeList pluginNodeList = rootElement.getElementsByTagName("plugin");
      for (int i = 0; i < pluginNodeList.getLength(); ++i) {
        Node pluginNode = pluginNodeList.item(i);
        if (!(pluginNode instanceof Element)) {
          continue;
        }
        //check plugin is inside plugins
        Element pluginElement = (Element) pluginNode;
        Node pluginParentNode = pluginElement.getParentNode();
        if (pluginParentNode == null || !(pluginParentNode instanceof Element)) {
          continue;
        }
        Element pluginParentElement = (Element) pluginParentNode;
        if (!pluginParentElement.getTagName().equals("plugins")) {
          continue;
        }
        Node pluginsNode = pluginParentElement;
        //check plugins is inside build
        Node pluginsParentNode = pluginsNode.getParentNode();
        if (pluginsParentNode == null || !(pluginsParentNode instanceof Element)) {
          continue;
        }
        Element pluginsParentElement = (Element) pluginsParentNode;
        if (!pluginsParentElement.getTagName().equals("build")) {
          continue;
        }
        //check for groupId
        NodeList childNodesOfPlugin = pluginElement.getChildNodes();
        List<Node> groupIdNodeList = new ArrayList<Node>();
        for (int j = 0; j < childNodesOfPlugin.getLength(); ++j) {
          Node childNode = childNodesOfPlugin.item(j);
          if (childNode instanceof Element) {
            Element childElement = (Element) childNode;
            if (childElement.getTagName().equals("groupId")) {
              groupIdNodeList.add(childNode);
            }
          }
        }
        if (groupIdNodeList.size() != 1) {
          continue;
        }
        Node groupIdNode = groupIdNodeList.get(0);
        Element groupIdElement = (Element) groupIdNode;
        if (!groupIdElement.getTextContent().equals(pluginGroupId)) {
          continue;
        }
        //check for artifactId
        List<Node> artifactIdNodeList = new ArrayList<Node>();
        for (int j = 0; j < childNodesOfPlugin.getLength(); ++j) {
          Node childNode = childNodesOfPlugin.item(j);
          if (childNode instanceof Element) {
            Element childElement = (Element) childNode;
            if (childElement.getTagName().equals("artifactId")) {
              artifactIdNodeList.add(childNode);
            }
          }
        }
        if (artifactIdNodeList.size() != 1) {
          continue;
        }
        Node artifactIdNode = artifactIdNodeList.get(0);
        Element artifactIdElement = (Element) artifactIdNode;
        if (!artifactIdElement.getTextContent().equals(pluginArtifactId)) {
          continue;
        }
        //check for configuration
        List<Element> argsToRemove = new ArrayList<Element>();
        NodeList compilerArgList = pluginElement.getElementsByTagName("compilerArg");
        for (int j = 0; j < compilerArgList.getLength(); ++j) {
          Node compilerArgNode = compilerArgList.item(j);
          if (!(compilerArgNode instanceof Element)) {
            continue;
          }
          Element compilerArgElement = (Element) compilerArgNode;
          if (compilerArgElement.getTextContent().trim().equals(argValue)) {
            argsToRemove.add(compilerArgElement);
          }
        }
        if (argsToRemove.size() > 0) {
          for (Element argToRemove : argsToRemove) {
            argToRemove.getParentNode().removeChild(argToRemove);
          }
          //get string and rewrite file
          String newXMlDocString = getXMLString(document);
          if (newXMlDocString.equals("")) {
            System.out.println("WARNING: XML document is empty");
          } else {
            File modifiedPomFile = new File(pomFileName);
            FileWriter modifiedPomFileWriter = new FileWriter(modifiedPomFile, false);
            modifiedPomFileWriter.write(newXMlDocString);
            modifiedPomFileWriter.close();
          }
        }
      }
    } catch (Exception e) {
      System.out.println("WARNING: Could not remove arg");
    }
  }

  private void addMavenSurefirePluginForSequentialTestsConfiguration(String pomFileName, Document document, Element rootElement, Node buildNode, Node pluginsNode) throws IOException {
    //create plugin node
    Element pluginElement = document.createElement("plugin");
    addTag(document, pluginElement, "groupId", "org.apache.maven.plugins");
    addTag(document, pluginElement, "artifactId", "maven-surefire-plugin");
    addTag(document, pluginElement, "version", "2.22.2");
    Element configurationElement = document.createElement("configuration");
    addTag(document, configurationElement, "forkCount", "1");
    addTag(document, configurationElement, "reuseForks", "false");
    pluginElement.appendChild(configurationElement);
    //add plugin node
    if (buildNode != null && pluginsNode != null) {
      pluginsNode.appendChild(pluginElement);
    } else if (buildNode != null) {
      //create plugins node
      Element pluginsElement = document.createElement("plugins");
      pluginsElement.appendChild(pluginElement);
      buildNode.appendChild(pluginsElement);
    } else {
      //create build and plugins nodes
      if (!rootElement.getTagName().equals("project")) {
        System.out.println("WARNING: Root tag is not the project tag");
      }
      Node projectNode = rootElement;
      Element buildElement = document.createElement("build");
      Element pluginsElement = document.createElement("plugins");
      pluginsElement.appendChild(pluginElement);
      buildElement.appendChild(pluginsElement);
      projectNode.appendChild(buildElement);
    }
    //save new XML content
    String newXMlDocString = getXMLString(document);
    if (newXMlDocString.equals("")) {
      System.out.println("WARNING: XML document is empty");
    } else {
      File modifiedPomFile = new File(pomFileName);
      FileWriter modifiedPomFileWriter = new FileWriter(modifiedPomFile, false);
      modifiedPomFileWriter.write(newXMlDocString);
      modifiedPomFileWriter.close();
    }
  }

  private void removeTags(Element element, List<String> tags) {
    List<Node> nodesToRemove = new ArrayList<Node>();
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); ++i) {
      Node child = children.item(i);
      if (child instanceof Element) {
        Element childElement = (Element) child;
        if (tags.contains(childElement.getTagName())) {
          nodesToRemove.add(childElement);
        }
      }
    }
    for (Node nodeToRemove : nodesToRemove) {
      nodeToRemove.getParentNode().removeChild(nodeToRemove);
    }
  }

  private void addSequentialTestsConfiguration(Document document, Element element) {
    Element configurationElement = document.createElement("configuration");
    addTag(document, configurationElement, "forkCount", "1");
    addTag(document, configurationElement, "reuseForks", "false");
    element.appendChild(configurationElement);
  }

  private void addTag(Document document, Element element, String tag, String value) {
    Element newElement = document.createElement(tag);
    newElement.setTextContent(value);
    element.appendChild(newElement);
  }

  private boolean removeTestsInParallel(String pomFileName) {
    boolean success = false;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      String xmlContent = new String(Files.readAllBytes(Paths.get(pomFileName)));
      Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
      Element rootElement = document.getDocumentElement();
      NodeList buildNodeList = rootElement.getElementsByTagName("build");
      for (int i = 0; i < buildNodeList.getLength(); ++i) {
        Node buildNode = buildNodeList.item(i);
        if (!(buildNode instanceof Element)) {
          continue;
        }
        Element buildElement = (Element) buildNode;
        NodeList pluginsNodeList = buildElement.getElementsByTagName("plugins");
        for (int j = 0; j < pluginsNodeList.getLength(); ++j) {
          Node pluginsNode = pluginsNodeList.item(j);
          if (!(pluginsNode instanceof Element)) {
            continue;
          }
          Element pluginsElement = (Element) pluginsNode;
          NodeList pluginNodeList = pluginsElement.getElementsByTagName("plugin");
          for (int k = 0; k < pluginNodeList.getLength(); ++k) {
            Node pluginNode = pluginNodeList.item(k);
            if (!(pluginNode instanceof Element)) {
              continue;
            }
            Element pluginElement = (Element) pluginNode;
            boolean isOrgApacheMavenPluginsGroupId = false;
            boolean isMavenSurfirePluginArtifactId = false;
            NodeList groupIdNodeList = pluginElement.getElementsByTagName("groupId");
            for (int l = 0; l < groupIdNodeList.getLength(); ++l) {
              Node groupIdNode = groupIdNodeList.item(l);
              if (!(groupIdNode instanceof Element)) {
                continue;
              }
              Element groupIdElement = (Element) groupIdNode;
              if (groupIdElement.getTextContent().trim().equals("org.apache.maven.plugins")) {
                isOrgApacheMavenPluginsGroupId = true;
              }
            }
            NodeList artifactIdNodeList = pluginElement.getElementsByTagName("artifactId");
            for (int l = 0; l < artifactIdNodeList.getLength(); ++l) {
              Node artifactIdNode = artifactIdNodeList.item(l);
              if (!(artifactIdNode instanceof Element)) {
                continue;
              }
              Element artifactIdElement = (Element) artifactIdNode;
              if (artifactIdElement.getTextContent().trim().equals("maven-surefire-plugin")) {
                isMavenSurfirePluginArtifactId = true;
              }
            }
            if (isOrgApacheMavenPluginsGroupId && isMavenSurfirePluginArtifactId) {
              NodeList configurationNodeList = pluginElement.getElementsByTagName("configuration");
              List<Node> nodesToRemoveList = new ArrayList<Node>();
              List<String> nodeNamesToConsider = new ArrayList<String>();
              nodeNamesToConsider.add("parallel");
              nodeNamesToConsider.add("parallelOptimized");
              nodeNamesToConsider.add("parallelTestsTimeoutForcedInSeconds");
              nodeNamesToConsider.add("parallelTestsTimeoutInSeconds");
              nodeNamesToConsider.add("perCoreThreadCount");
              nodeNamesToConsider.add("threadCount");
              nodeNamesToConsider.add("threadCountClasses");
              nodeNamesToConsider.add("threadCountMethods");
              nodeNamesToConsider.add("threadCountSuites");
              nodeNamesToConsider.add("useUnlimitedThreads");
              for (int l = 0; l < configurationNodeList.getLength(); ++l) {
                Node configurationNode = configurationNodeList.item(l);
                if (!(configurationNode instanceof Element)) {
                  continue;
                }
                Element configurationElement = (Element) configurationNode;
                for (String nodeName : nodeNamesToConsider) {
                  NodeList nodeList = configurationElement.getElementsByTagName(nodeName);
                  if (nodeList.getLength() > 0) {
                    if (nodeList.getLength() != 1) {
                      System.out.println("ERROR: Multiple nodes for:" + nodeName);
                      success = false;
                      return success;
                    }
                    nodesToRemoveList.add(nodeList.item(0));
                  }
                }
              }
              if (nodesToRemoveList.size() == 0) {
                //we must have some nodes to remove
                System.out.println("ERROR: We must have some nodes to remove");
                success = false;
                return success;
              }
              for (Node nodeToRemove : nodesToRemoveList) {
                nodeToRemove.getParentNode().removeChild(nodeToRemove);
              }
              //get string and rewrite file
              String newXMlDocString = getXMLString(document);
              if (newXMlDocString.equals("")) {
                success = false;
                break;
              } else {
                File modifiedPomFile = new File(pomFileName);
                FileWriter modifiedPomFileWriter = new FileWriter(modifiedPomFile, false);
                modifiedPomFileWriter.write(newXMlDocString);
                modifiedPomFileWriter.close();
              }
              success = true;
              return success;
            }
          }
        }
      }
    } catch (Exception e) {
      System.out.println("ERROR: Error while trying to remove nodes for parallel test runs");
      success = false;
    }
    return success;
  }

  private void excludeLibraryFromLibraryIfNeeded(String modifiedPomFileName, String libraryToModifyGrouptId, String libraryToModifyArtifactId, String libraryToExcludeGroupId, String libraryToExcludeArtifactId) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      String xmlContent = new String(Files.readAllBytes(Paths.get(modifiedPomFileName)));
      Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
      Element rootElement = document.getDocumentElement();
      NodeList dependencyNodeList = rootElement.getElementsByTagName("dependency");
      for (int i = 0; i < dependencyNodeList.getLength(); ++i) {
        Node dependencyNode = dependencyNodeList.item(i);
        if (!(dependencyNode instanceof Element)) {
          continue;
        }
        Element dependencyElement = (Element) dependencyNode;
        //check parent of dependency is dependencies
        Node dependencyParentNode = dependencyElement.getParentNode();
        if (dependencyParentNode == null || !(dependencyParentNode instanceof Element)) {
          continue;
        }
        Element dependencyParentElement = (Element) dependencyParentNode;
        if (!dependencyParentElement.getTagName().equals("dependencies")) {
          continue;
        }
        //check parent of dependencies is project
        Node dependenciesNode = dependencyParentElement;
        Node dependenciesParentNode = dependenciesNode.getParentNode();
        if (dependenciesParentNode == null || !(dependenciesParentNode instanceof Element)) {
          continue;
        }
        Element dependenciesParentElement = (Element) dependenciesParentNode;
        if (!dependenciesParentElement.getTagName().equals("project")) {
          continue;
        }
        //check for groupId
        NodeList groupIdNodeList = dependencyElement.getElementsByTagName("groupId");
        if (groupIdNodeList.getLength() != 1) {
          continue;
        }
        Node groupIdNode = groupIdNodeList.item(0);
        if (!(groupIdNode instanceof Element)) {
          continue;
        }
        Element groupIdElement = (Element) groupIdNode;
        if (!groupIdElement.getTextContent().equals(libraryToModifyGrouptId)) {
          continue;
        }
        //check for artifactId
        NodeList artifactIdNodeList = dependencyElement.getElementsByTagName("artifactId");
        if (artifactIdNodeList.getLength() != 1) {
          continue;
        }
        Node artifactIdNode = artifactIdNodeList.item(0);
        if (!(artifactIdNode instanceof Element)) {
          continue;
        }
        Element artifactIdElement = (Element) artifactIdNode;
        if (!artifactIdElement.getTextContent().equals(libraryToModifyArtifactId)) {
          continue;
        }
        //change pom file to have new dependencies
        Element exclusionsElement = document.createElement("exclusions");
        Element exclusionElement = document.createElement("exclusion");
        Element exclusionGroupIdElement = document.createElement("groupId");
        exclusionGroupIdElement.setTextContent(libraryToExcludeGroupId);
        exclusionElement.appendChild(exclusionGroupIdElement);
        Element exclusionArtifactIdElement = document.createElement("artifactId");
        exclusionArtifactIdElement.setTextContent(libraryToExcludeArtifactId);
        exclusionElement.appendChild(exclusionArtifactIdElement);
        exclusionsElement.appendChild(exclusionElement);
        dependencyElement.appendChild(exclusionsElement);
        //get string and rewrite file
        String newXMlDocString = getXMLString(document);
        if (newXMlDocString.equals("")) {
          throw new RuntimeException();
        } else {
          File modifiedPomFile = new File(modifiedPomFileName);
          FileWriter modifiedPomFileWriter = new FileWriter(modifiedPomFile, false);
          modifiedPomFileWriter.write(newXMlDocString);
          modifiedPomFileWriter.close();
        }
      }
    } catch (Exception e) {
      System.out.println("WARNING: Could not exclude library " + modifiedPomFileName + "#" + libraryToExcludeGroupId + "#" + libraryToExcludeArtifactId);
    }
  }

  private boolean replaceOrAddJunitInPomFile(String modifiedPomFileName) {
    boolean success = false;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      String xmlContent = new String(Files.readAllBytes(Paths.get(modifiedPomFileName)));
      Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
      Element rootElement = document.getDocumentElement();
      NodeList dependencyNodeList = rootElement.getElementsByTagName("dependency");
      for (int i = 0; i < dependencyNodeList.getLength(); ++i) {
        Node dependencyNode = dependencyNodeList.item(i);
        if (!(dependencyNode instanceof Element)) {
          continue;
        }
        Element dependencyElement = (Element) dependencyNode;
        //check parent of dependency is dependencies
        Node dependencyParentNode = dependencyElement.getParentNode();
        if (dependencyParentNode == null || !(dependencyParentNode instanceof Element)) {
          continue;
        }
        Element dependencyParentElement = (Element) dependencyParentNode;
        if (!dependencyParentElement.getTagName().equals("dependencies")) {
          continue;
        }
        //check parent of dependencies is project
        Node dependenciesNode = dependencyParentElement;
        Node dependenciesParentNode = dependenciesNode.getParentNode();
        if (dependenciesParentNode == null || !(dependenciesParentNode instanceof Element)) {
          continue;
        }
        Element dependenciesParentElement = (Element) dependenciesParentNode;
        if (!dependenciesParentElement.getTagName().equals("project")) {
          continue;
        }
        //check for groupId
        NodeList groupIdNodeList = dependencyElement.getElementsByTagName("groupId");
        if (groupIdNodeList.getLength() != 1) {
          continue;
        }
        Node groupIdNode = groupIdNodeList.item(0);
        if (!(groupIdNode instanceof Element)) {
          continue;
        }
        Element groupIdElement = (Element) groupIdNode;
        if (!groupIdElement.getTextContent().equals("junit")) {
          continue;
        }
        //check for artifactId
        NodeList artifactIdNodeList = dependencyElement.getElementsByTagName("artifactId");
        if (artifactIdNodeList.getLength() != 1) {
          continue;
        }
        Node artifactIdNode = artifactIdNodeList.item(0);
        if (!(artifactIdNode instanceof Element)) {
          continue;
        }
        Element artifactIdElement = (Element) artifactIdNode;
        if (!artifactIdElement.getTextContent().equals("junit")) {
          continue;
        }
        //change pom file to have new dependencies
        dependencyParentNode.removeChild(dependencyNode);
        addDependencyNode(document, dependencyParentNode, "junit", "junit", "4.13");
        //get string and rewrite file
        String newXMlDocString = getXMLString(document);
        if (newXMlDocString.equals("")) {
          success = false;
          break;
        } else {
          File modifiedPomFile = new File(modifiedPomFileName);
          FileWriter modifiedPomFileWriter = new FileWriter(modifiedPomFile, false);
          modifiedPomFileWriter.write(newXMlDocString);
          modifiedPomFileWriter.close();
        }
        success = true;
        //breaking because we already modified the pom file
        return success;
      }
      //
      if (!success) {
        NodeList dependenciesNodeList = rootElement.getElementsByTagName("dependencies");
        for (int i = 0; i < dependenciesNodeList.getLength(); ++i) {
          Node dependenciesNode = dependenciesNodeList.item(i);
          if (!(dependenciesNode instanceof Element)) {
            continue;
          }
          Element dependenciesElement = (Element) dependenciesNode;
          Node dependenciesParentNode = dependenciesElement.getParentNode();
          if (dependenciesParentNode == null || !(dependenciesParentNode instanceof Element)) {
            continue;
          }
          Element dependencyParentElement = (Element) dependenciesParentNode;
          if (!dependencyParentElement.getTagName().equals("project")) {
            continue;
          }
          //add dependencies
          addDependencyNode(document, dependenciesNode, "junit", "junit", "4.13");
          //get string and rewrite file
          String newXMlDocString = getXMLString(document);
          if (newXMlDocString.equals("")) {
            success = false;
            break;
          } else {
            File modifiedPomFile = new File(modifiedPomFileName);
            FileWriter modifiedPomFileWriter = new FileWriter(modifiedPomFile, false);
            modifiedPomFileWriter.write(newXMlDocString);
            modifiedPomFileWriter.close();
          }
          success = true;
          return success;
        }
      }

    } catch (Exception e) {
      System.out.println("ERROR: Could not replace junit");
      success = false;
    }
    return success;
  }

  private void addDependencyNode(Document document, Node dependenciesNode, String groupId, String artifactId, String version) {
    Element dependencyElement = document.createElement("dependency");
    Element groupIdElement = document.createElement("groupId");
    groupIdElement.setTextContent(groupId);
    dependencyElement.appendChild(groupIdElement);
    Element artifactIdElement = document.createElement("artifactId");
    artifactIdElement.setTextContent(artifactId);
    dependencyElement.appendChild(artifactIdElement);
    Element versionElement = document.createElement("version");
    versionElement.setTextContent(version);
    dependencyElement.appendChild(versionElement);
    Element scopeElement = document.createElement("scope");
    scopeElement.setTextContent("test");
    dependencyElement.appendChild(scopeElement);
    dependenciesNode.appendChild(dependencyElement);
  }

  private boolean replaceOrAddMockitoWithCustomizedMockitoInPomFile(String modifiedPomFileName, int mockitoMajorVersion, int junitVersion, String repoName) {
    boolean success = false;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      String xmlContent = new String(Files.readAllBytes(Paths.get(modifiedPomFileName)));
      Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
      Element rootElement = document.getDocumentElement();
      NodeList dependencyNodeList = rootElement.getElementsByTagName("dependency");
      for (int i = 0; i < dependencyNodeList.getLength(); ++i) {
        Node dependencyNode = dependencyNodeList.item(i);
        if (!(dependencyNode instanceof Element)) {
          continue;
        }
        Element dependencyElement = (Element) dependencyNode;
        //check parent of dependency is dependencies
        Node dependencyParentNode = dependencyElement.getParentNode();
        if (dependencyParentNode == null || !(dependencyParentNode instanceof Element)) {
          continue;
        }
        Element dependencyParentElement = (Element) dependencyParentNode;
        if (!dependencyParentElement.getTagName().equals("dependencies")) {
          continue;
        }
        //check parent of dependencies is project
        Node dependenciesNode = dependencyParentElement;
        Node dependenciesParentNode = dependenciesNode.getParentNode();
        if (dependenciesParentNode == null || !(dependenciesParentNode instanceof Element)) {
          continue;
        }
        Element dependenciesParentElement = (Element) dependenciesParentNode;
        if (!dependenciesParentElement.getTagName().equals("project")) {
          continue;
        }
        //check for groupId
        NodeList childNodesOfDependency = dependencyElement.getChildNodes();
        List<Node> groupIdNodeList = new ArrayList<Node>();
        for (int j = 0; j < childNodesOfDependency.getLength(); ++j) {
          Node childNode = childNodesOfDependency.item(j);
          if (childNode instanceof Element) {
            Element childElement = (Element) childNode;
            if (childElement.getTagName().equals("groupId")) {
              groupIdNodeList.add(childNode);
            }
          }
        }
        if (groupIdNodeList.size() != 1) {
          continue;
        }
        Node groupIdNode = groupIdNodeList.get(0);
        Element groupIdElement = (Element) groupIdNode;
        if (!groupIdElement.getTextContent().equals("org.mockito")) {
          continue;
        }
        //check for artifactId
        List<Node> artifactIdNodeList = new ArrayList<Node>();
        for (int j = 0; j < childNodesOfDependency.getLength(); ++j) {
          Node childNode = childNodesOfDependency.item(j);
          if (childNode instanceof Element) {
            Element childElement = (Element) childNode;
            if (childElement.getTagName().equals("artifactId")) {
              artifactIdNodeList.add(childNode);
            }
          }
        }
        if (artifactIdNodeList.size() != 1) {
          continue;
        }
        Node artifactIdNode = artifactIdNodeList.get(0);
        Element artifactIdElement = (Element) artifactIdNode;
        if (!artifactIdElement.getTextContent().equals("mockito-core")) {
          continue;
        }
        //change pom file to have new dependencies
        dependencyParentNode.removeChild(dependencyNode);
        addDependencyNode(document, dependencyParentNode, "org.mockito", "mockito-core", getCustomizedMockitoVersion(mockitoMajorVersion), getCustomizedMockitoJarFileName(mockitoMajorVersion));
        addDependencyNode(document, dependencyParentNode, "net.bytebuddy", "byte-buddy", getByteBuddyVersion(mockitoMajorVersion), getByteBuddyJarFileName(mockitoMajorVersion));
        //repo specific solution to avoid jar hell
        if (!repoName.equals("komoot/photon")) {
          addDependencyNode(document, dependencyParentNode, "net.bytebuddy", "byte-buddy-agent", getByteBuddyAgentVersion(mockitoMajorVersion), getByteBuddyAgentJarFileName(mockitoMajorVersion));
        }
        addDependencyNode(document, dependencyParentNode, "org.objenesis", "objenesis", getObjenesisVersion(mockitoMajorVersion), getObjenesisJarFileName(mockitoMajorVersion));
        if (junitVersion == 5) {
          addDependencyNode(document, dependencyParentNode, "org.mockito", "mockito-junit-jupiter", getCustomizedMockitoExtensionVersion(mockitoMajorVersion), getCustomizedMockitoExtensionJarFileName(mockitoMajorVersion));
        }
        //get string and rewrite file
        String newXMlDocString = getXMLString(document);
        if (newXMlDocString.equals("")) {
          success = false;
          break;
        } else {
          File modifiedPomFile = new File(modifiedPomFileName);
          FileWriter modifiedPomFileWriter = new FileWriter(modifiedPomFile, false);
          modifiedPomFileWriter.write(newXMlDocString);
          modifiedPomFileWriter.close();
        }
        success = true;
        //breaking because we already modified the pom file
        return success;
      }
      //
      if (!success) {
        NodeList dependenciesNodeList = rootElement.getElementsByTagName("dependencies");
        for (int i = 0; i < dependenciesNodeList.getLength(); ++i) {
          Node dependenciesNode = dependenciesNodeList.item(i);
          if (!(dependenciesNode instanceof Element)) {
            continue;
          }
          Element dependenciesElement = (Element) dependenciesNode;
          Node dependenciesParentNode = dependenciesElement.getParentNode();
          if (dependenciesParentNode == null || !(dependenciesParentNode instanceof Element)) {
            continue;
          }
          Element dependencyParentElement = (Element) dependenciesParentNode;
          if (!dependencyParentElement.getTagName().equals("project")) {
            continue;
          }
          //add dependencies
          addDependencyNode(document, dependenciesNode, "org.mockito", "mockito-core", getCustomizedMockitoVersion(mockitoMajorVersion), getCustomizedMockitoJarFileName(mockitoMajorVersion));
          addDependencyNode(document, dependenciesNode, "net.bytebuddy", "byte-buddy", getByteBuddyVersion(mockitoMajorVersion), getByteBuddyJarFileName(mockitoMajorVersion));
          //repo specific solution to avoid jar hell
          if (!repoName.equals("komoot/photon")) {
            addDependencyNode(document, dependenciesNode, "net.bytebuddy", "byte-buddy-agent", getByteBuddyAgentVersion(mockitoMajorVersion), getByteBuddyAgentJarFileName(mockitoMajorVersion));
          }
          addDependencyNode(document, dependenciesNode, "org.objenesis", "objenesis", getObjenesisVersion(mockitoMajorVersion), getObjenesisJarFileName(mockitoMajorVersion));
          if (junitVersion == 5) {
            addDependencyNode(document, dependenciesNode, "org.mockito", "mockito-junit-jupiter", getCustomizedMockitoExtensionVersion(mockitoMajorVersion), getCustomizedMockitoExtensionJarFileName(mockitoMajorVersion));
          }
          //get string and rewrite file
          String newXMlDocString = getXMLString(document);
          if (newXMlDocString.equals("")) {
            success = false;
            break;
          } else {
            File modifiedPomFile = new File(modifiedPomFileName);
            FileWriter modifiedPomFileWriter = new FileWriter(modifiedPomFile, false);
            modifiedPomFileWriter.write(newXMlDocString);
            modifiedPomFileWriter.close();
          }
          success = true;
          return success;
        }
      }

    } catch (Exception e) {
      System.out.println("ERROR: Could not replace mockito with customized mockito in pom file");
      success = false;
    }
    return success;
  }

  private boolean checkPluginUse(String modifiedPomFileName, String pluginGroupId, String pluginArtifactId) {
    boolean found = false;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      String xmlContent = new String(Files.readAllBytes(Paths.get(modifiedPomFileName)));
      Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
      Element rootElement = document.getDocumentElement();
      NodeList pluginNodeList = rootElement.getElementsByTagName("plugin");
      for (int i = 0; i < pluginNodeList.getLength(); ++i) {
        Node pluginNode = pluginNodeList.item(i);
        if (!(pluginNode instanceof Element)) {
          continue;
        }
        //check plugin is inside plugins
        Element pluginElement = (Element) pluginNode;
        Node pluginParentNode = pluginElement.getParentNode();
        if (pluginParentNode == null || !(pluginParentNode instanceof Element)) {
          continue;
        }
        Element pluginParentElement = (Element) pluginParentNode;
        if (!pluginParentElement.getTagName().equals("plugins")) {
          continue;
        }
        Node pluginsNode = pluginParentElement;
        //check plugins is inside build
        Node pluginsParentNode = pluginsNode.getParentNode();
        if (pluginsParentNode == null || !(pluginsParentNode instanceof Element)) {
          continue;
        }
        Element pluginsParentElement = (Element) pluginsParentNode;
        if (!pluginsParentElement.getTagName().equals("build")) {
          continue;
        }
        //check for groupId
        NodeList childNodesOfPlugin = pluginElement.getChildNodes();
        List<Node> groupIdNodeList = new ArrayList<Node>();
        for (int j = 0; j < childNodesOfPlugin.getLength(); ++j) {
          Node childNode = childNodesOfPlugin.item(j);
          if (childNode instanceof Element) {
            Element childElement = (Element) childNode;
            if (childElement.getTagName().equals("groupId")) {
              groupIdNodeList.add(childNode);
            }
          }
        }
        if (groupIdNodeList.size() != 1) {
          continue;
        }
        Node groupIdNode = groupIdNodeList.get(0);
        Element groupIdElement = (Element) groupIdNode;
        if (!groupIdElement.getTextContent().equals(pluginGroupId)) {
          continue;
        }
        //check for artifactId
        List<Node> artifactIdNodeList = new ArrayList<Node>();
        for (int j = 0; j < childNodesOfPlugin.getLength(); ++j) {
          Node childNode = childNodesOfPlugin.item(j);
          if (childNode instanceof Element) {
            Element childElement = (Element) childNode;
            if (childElement.getTagName().equals("artifactId")) {
              artifactIdNodeList.add(childNode);
            }
          }
        }
        if (artifactIdNodeList.size() != 1) {
          continue;
        }
        Node artifactIdNode = artifactIdNodeList.get(0);
        Element artifactIdElement = (Element) artifactIdNode;
        if (!artifactIdElement.getTextContent().equals(pluginArtifactId)) {
          continue;
        }
        found = true;
        break;
      }
    } catch (Exception e) {
      System.out.println("WARNING: Could not check the use of the plugin");
    }
    return found;
  }

  private void addDependencyNode(Document document, Node dependenciesNode, String groupId, String artifactId, String version, String jarFileName) {
    Element dependencyElement = document.createElement("dependency");
    Element groupIdElement = document.createElement("groupId");
    groupIdElement.setTextContent(groupId);
    dependencyElement.appendChild(groupIdElement);
    Element artifactIdElement = document.createElement("artifactId");
    artifactIdElement.setTextContent(artifactId);
    dependencyElement.appendChild(artifactIdElement);
    Element versionElement = document.createElement("version");
    versionElement.setTextContent(version);
    dependencyElement.appendChild(versionElement);
    Element scopeElement = document.createElement("scope");
    scopeElement.setTextContent("system");
    dependencyElement.appendChild(scopeElement);
    Element systemPathElement = document.createElement("systemPath");
    systemPathElement.setTextContent(jarFileName);
    dependencyElement.appendChild(systemPathElement);
    dependenciesNode.appendChild(dependencyElement);
  }

  private String getCustomizedMockitoVersion(int majorMockitoVersion) {
    String result = "";
    if (majorMockitoVersion == 3) {
      result = "3.12.5";
      return result;
    }
    return result;
  }

  private String getCustomizedMockitoJarFileName(int majorMockitoVersion) {
    String result = "";
    if (majorMockitoVersion == 3) {
      result = ARUS.CUSTOMIZED_MOCKITO_LIBS_FOLDER_NAME + File.separator + "org-mockito_mockito-core_3-12-5.jar";
      return result;
    }
    return result;
  }

  private String getCustomizedMockitoExtensionVersion(int majorMockitoVersion) {
    String result = "";
    if (majorMockitoVersion == 3) {
      result = "3.12.5";
      return result;
    }
    return result;
  }

  private String getCustomizedMockitoExtensionJarFileName(int majorMockitoVersion) {
    String result = "";
    if (majorMockitoVersion == 3) {
      result = ARUS.CUSTOMIZED_MOCKITO_LIBS_FOLDER_NAME + File.separator + "org-mockito_mockito-junit-jupiter_3-12-5.jar";
      return result;
    }
    return result;
  }

  private String getByteBuddyVersion(int majorMockitoVersion) {
    String result = "";
    if (majorMockitoVersion == 3) {
      result = "1.11.13";
      return result;
    }
    return result;
  }

  private String getByteBuddyJarFileName(int majorMockitoVersion) {
    String result = "";
    if (majorMockitoVersion == 3) {
      result = ARUS.CUSTOMIZED_MOCKITO_LIBS_FOLDER_NAME + File.separator + "net-bytebuddy_byte-buddy_1-11-13.jar";
      return result;
    }
    return result;
  }

  private String getByteBuddyAgentVersion(int majorMockitoVersion) {
    String result = "";
    if (majorMockitoVersion == 3) {
      result = "1.11.13";
      return result;
    }
    return result;
  }

  private String getByteBuddyAgentJarFileName(int majorMockitoVersion) {
    String result = "";
    if (majorMockitoVersion == 3) {
      result = ARUS.CUSTOMIZED_MOCKITO_LIBS_FOLDER_NAME + File.separator + "net-bytebuddy_byte-buddy-agent_1-11-13.jar";
      return result;
    }
    return result;
  }

  private String getObjenesisVersion(int majorMockitoVersion) {
    String result = "";
    if (majorMockitoVersion == 3) {
      result = "3.2";
      return result;
    }
    return result;
  }

  private String getObjenesisJarFileName(int majorMockitoVersion) {
    String result = "";
    if (majorMockitoVersion == 3) {
      result = ARUS.CUSTOMIZED_MOCKITO_LIBS_FOLDER_NAME + File.separator + "org-objenesis_objenesis_3-2.jar";
      return result;
    }
    return result;
  }

  private String getXMLString(Document doc) {
    String docString = "";
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(doc);
      transformer.transform(source, result);
      docString = result.getWriter().toString();
    } catch (Exception e) {
      System.out.println("ERROR: Could not get XML document string");
    }
    return docString;
  }

  private void saveResults(JsonArray resultsArray) {
    try {
      JsonObject resultsJson = new JsonObject();
      resultsJson.add("results", resultsArray);
      FileWriter resultsFileWriter = new FileWriter(ARUS.RESULTS_FILE_NAME);
      resultsFileWriter.write(resultsJson.toString());
      resultsFileWriter.close();
    } catch (Exception e) {
      System.out.println("ERROR: Could not save results to file");
      System.exit(1);
    }
  }
}