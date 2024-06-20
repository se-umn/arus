package edu.umn.cs.analysis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.umn.cs.analysis.model.Commit;
import edu.umn.cs.analysis.model.ExperimentRepoInfo;
import org.eclipse.jgit.api.Git;

import java.io.*;
import java.util.*;

public class AnalyzeDepsInRelevantRepos {

  private static String REPOS_ANALYSIS_FILE_NAME = "";
  private static String REPOS_FOLDER = "";
  private static String MAVEN_HOME = "";
  public static void main(String args[]){
    if(args.length!=1){
      System.out.println("usage: ./gradlew clean -PmainClass=edu.umn.cs.analysis.RunExperiment run --args=\"config_file_name\"");
      System.exit(1);
    }
    try {
      Properties prop = new Properties();
      prop.load(new FileInputStream(args[0]));
      AnalyzeDepsInRelevantRepos.REPOS_ANALYSIS_FILE_NAME = prop.getProperty("repos_analysis_file_name");
      AnalyzeDepsInRelevantRepos.REPOS_FOLDER = prop.getProperty("repos_folder");
      AnalyzeDepsInRelevantRepos.MAVEN_HOME = prop.getProperty("maven_home");
      if (AnalyzeDepsInRelevantRepos.REPOS_ANALYSIS_FILE_NAME.equals("") || AnalyzeDepsInRelevantRepos.REPOS_FOLDER.equals("") || AnalyzeDepsInRelevantRepos.MAVEN_HOME.equals("")) {
        System.out.println("ERROR: Need suitable configuration information");
        System.exit(1);
      }
      File reposFolderFile = new File(AnalyzeDepsInRelevantRepos.REPOS_FOLDER);
      if (reposFolderFile.exists()) {
        System.out.println("ERROR: Delete the repos folder before running experiments:" + AnalyzeDepsInRelevantRepos.REPOS_FOLDER);
        System.exit(1);
      } else {
        reposFolderFile.mkdir();
      }
      AnalyzeDepsInRelevantRepos rra = new AnalyzeDepsInRelevantRepos();
      rra.findRelevantProjects(AnalyzeDepsInRelevantRepos.REPOS_ANALYSIS_FILE_NAME);
    }
    catch(Exception e){
      e.printStackTrace(System.out);
    }

  }

  private void findRelevantProjects(String relevantReposFileName){
    try {
      BufferedReader br = new BufferedReader(new FileReader(relevantReposFileName));
      JsonElement resultsElement = JsonParser.parseReader(br);
      br.close();
      JsonArray reposInfoArray = resultsElement.getAsJsonObject().getAsJsonArray("results");
      FileWriter resultsFileWriter = new FileWriter("/tmp/repos_deps_info.txt", false);
      System.out.println(reposInfoArray.size());
      int count = 0;
      for(int i=0; i<reposInfoArray.size(); ++i) {
        JsonObject repoAnalysis = reposInfoArray.get(i).getAsJsonObject();
        analyzeDepsInRepo(repoAnalysis, resultsFileWriter);
        count++;
      }
      resultsFileWriter.close();
    }
    catch(Exception e){
      System.out.println("Error while computing info");
      e.printStackTrace(System.out);
    }
  }

    private void analyzeDepsInRepo(JsonObject repoInfo, FileWriter resultsFileWriter) throws IOException {
      String repoName = repoInfo.get("repo_name").getAsString();
      try {
        Commit commit = null;
        String workingDirectoryInRepo = "";
        int passedAndNotFlaky = 0;
        boolean isCommitAnalysis = false;
        //
        JsonObject latestCommitAnalysisResults = repoInfo.get("latest_commit_analysis_results").getAsJsonObject();
        JsonArray testExecutionResultsInCommitAnalysis = latestCommitAnalysisResults.get("test_execution_results").getAsJsonArray();
        int passedAndNotFlakyInCommitAnalysis = AnalysisUtils.checkTestsAllPassAndNotFlaky(testExecutionResultsInCommitAnalysis);
        //
        JsonObject latestTagAnalysisResults = repoInfo.get("latest_tag_analysis_results").getAsJsonObject();
        JsonArray testExecutionResultsInTagAnalysis = latestTagAnalysisResults.get("test_execution_results").getAsJsonArray();
        int passedAndNotFlakyInTagAnalysis = AnalysisUtils.checkTestsAllPassAndNotFlaky(testExecutionResultsInTagAnalysis);
        //check if the result is interesting
        if (passedAndNotFlakyInCommitAnalysis > 0) {
          isCommitAnalysis = true;
          JsonObject analyzedCommit = latestCommitAnalysisResults.get("analyzed_commit").getAsJsonObject();
          String commitId = analyzedCommit.get("id").getAsString();
          int year = analyzedCommit.get("year").getAsInt();
          int month = analyzedCommit.get("month").getAsInt();
          int day = analyzedCommit.get("day").getAsInt();
          commit = new Commit(commitId, year, month, day);
          workingDirectoryInRepo = latestCommitAnalysisResults.get("maven_working_dir_in_repo").getAsString();
          passedAndNotFlaky = passedAndNotFlakyInCommitAnalysis;
        } else if (passedAndNotFlakyInTagAnalysis > 0) {
          JsonObject analyzedCommit = latestTagAnalysisResults.get("analyzed_commit").getAsJsonObject();
          String commitId = analyzedCommit.get("id").getAsString();
          int year = analyzedCommit.get("year").getAsInt();
          int month = analyzedCommit.get("month").getAsInt();
          int day = analyzedCommit.get("day").getAsInt();
          commit = new Commit(commitId, year, month, day);
          workingDirectoryInRepo = latestTagAnalysisResults.get("maven_working_dir_in_repo").getAsString();
          passedAndNotFlaky = passedAndNotFlakyInTagAnalysis;
        } else {
          resultsFileWriter.write(repoName + "\tNOT INTERESTING" + System.lineSeparator());
          resultsFileWriter.flush();
          return;
        }
        String repoDiskName = repoName.replaceAll("/", "_");
        String currRepoFolderName = AnalyzeDepsInRelevantRepos.REPOS_FOLDER + File.separator + repoDiskName;
        File testRepo = new File(currRepoFolderName);
        Git git = Git.cloneRepository()
                .setURI("https://github.com/" + repoName + ".git")
                .setDirectory(testRepo)
                .setTimeout(600)
                .call();
        git.checkout().setName(commit.getId()).call();

        if(repoName.equals("atlarge-research/yardstick") || repoName.equals("TheBusyBiscuit/Slimefun4") || repoName.equals("gbif/gbif-api")) {
          resultsFileWriter.write(repoName + "\t" + (isCommitAnalysis == true ? "commit" : "tag") + "\t" + commit.getId() + "\t" + "=DATE(" + commit.getYear() + "," + commit.getMonth() + "," + commit.getDay() + ")" + "\t" + "COULD NOT GET DEPENDENCY TREE" + "\t" + "COULD NOT GET DEPENDENCY TREE" + System.lineSeparator());
          resultsFileWriter.flush();
          return;
        }

        String originalPomFile = "";
        if (workingDirectoryInRepo.equals("")) {
          originalPomFile = currRepoFolderName + File.separator + "pom.xml";
        } else {
          originalPomFile = currRepoFolderName + File.separator + workingDirectoryInRepo + File.separator + "pom.xml";
        }
        String dependencyTreeFileName = "";
        int mockitoMajorVersion = 0;
        int junitMajorVersion = -1;
        int junitMinorVersion = -1;
        boolean gotDependencyTree = MavenUtils.runMavenDependencyTree(originalPomFile, AnalyzeDepsInRelevantRepos.MAVEN_HOME, "umn_dependency_tree.xml");
        if (!gotDependencyTree) {
          resultsFileWriter.write(repoName + "\tERROR: Could not get dependency tree" + System.lineSeparator());
          resultsFileWriter.flush();
          return;
        }
        if (workingDirectoryInRepo.equals("")) {
          dependencyTreeFileName = currRepoFolderName + File.separator + "umn_dependency_tree.xml";
        } else {
          dependencyTreeFileName = currRepoFolderName + File.separator + workingDirectoryInRepo + File.separator + "umn_dependency_tree.xml";
        }
        //junit
        boolean junit = MavenUtils.checkForDependency(dependencyTreeFileName, MavenUtils.SCOPES, "junit");
        boolean junitJupiter = MavenUtils.checkForDependency(dependencyTreeFileName, MavenUtils.SCOPES, "org.junit.jupiter");
        boolean usesJunit = junit || junitJupiter;
        if (!usesJunit) {
          resultsFileWriter.write(repoName + "\tERROR: Does not use junit" + System.lineSeparator());
          resultsFileWriter.flush();
          return;
        }
        Set<String> junitDependencyLabels = new HashSet<String>();
        if (junitJupiter) {
          junitDependencyLabels.addAll(MavenUtils.getLabelsForDependency(dependencyTreeFileName, MavenUtils.SCOPES, "org.junit.jupiter"));
        }
        else if (junit) {
          junitDependencyLabels.addAll(MavenUtils.getLabelsForDependency(dependencyTreeFileName, MavenUtils.SCOPES, "junit"));
        }
        junitMajorVersion = AnalysisUtils.getJunitMajorVersion(junitDependencyLabels);
        junitMinorVersion = AnalysisUtils.getJunitMinorVersion(junitDependencyLabels);
        //mockito
        Set<String> mockitoDependencyLabels = MavenUtils.getLabelsForDependency(dependencyTreeFileName, MavenUtils.SCOPES, "org.mockito");
        mockitoMajorVersion = AnalysisUtils.getMajorMockitoVersion(AnalysisUtils.getMockitoDep(mockitoDependencyLabels));

        ///////////////////////////////////junit versions checks////////////////////////////////////

        if (junitMajorVersion != 4 && junitMajorVersion != 5) {
          resultsFileWriter.write(repoName + "\tERROR: Junit version not supported" + System.lineSeparator());
          resultsFileWriter.flush();
          return;
        }
        if (mockitoMajorVersion != 2 && mockitoMajorVersion != 3 && mockitoMajorVersion != 4) {
          resultsFileWriter.write(repoName + "\tERROR: Mockito version not supported" + System.lineSeparator());
          resultsFileWriter.flush();
          return;
        }
        resultsFileWriter.write(repoName+"\t"+(isCommitAnalysis==true?"commit":"tag")+"\t"+commit.getId()+"\t"+"=DATE(" + commit.getYear() + "," + commit.getMonth() + "," + commit.getDay() + ")" + "\t" + junitMajorVersion + "\t" + mockitoMajorVersion + System.lineSeparator());
        resultsFileWriter.flush();
        return;
      } catch (Exception e) {
        resultsFileWriter.write(repoName + "\tException:"+ e.getMessage() + System.lineSeparator());
        resultsFileWriter.flush();
        return;
      }
    }
}
