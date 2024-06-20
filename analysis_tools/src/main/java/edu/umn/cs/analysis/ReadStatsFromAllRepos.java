package edu.umn.cs.analysis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class ReadStatsFromAllRepos {

  public static void main(String args[]){
      ReadStatsFromAllRepos rra = new ReadStatsFromAllRepos();
      String reposAnalysisFileName = "/Users/mattia/Faculty/Research/2021_test_mocking_refactoring/repositories/test_mocking_refactoring/analysis_tools/data/all_repos.json";
      rra.printStats(reposAnalysisFileName);
  }

  private void printStats(String reposAnalysisFileName){
    try {
      BufferedReader br = new BufferedReader(new FileReader(reposAnalysisFileName));
      JsonElement resultsElement = JsonParser.parseReader(br);
      br.close();
      JsonArray reposInfoArray = resultsElement.getAsJsonObject().getAsJsonArray("results");
      System.out.println("Projects"+reposInfoArray.size());
      int projectsWithZeroPom = 0;
      int projectsWithSinglePom = 0;
      int projectsWithMultiplePom = 0;
      int projectsWithJunit = 0;
      int projectsWithMockito = 0;
      int projectsWithMockitoCore = 0;
      int projectsWithFeasibleMockitoCore = 0;
      int projectsWithTests = 0;
      int projectsWithTestsAndNotFlaky = 0;
      Set<String> mockitoCoreDeps = new HashSet<String>();
      for(int i=0; i<reposInfoArray.size(); ++i){
        //
        JsonObject repoAnalysis = reposInfoArray.get(i).getAsJsonObject();
        String repoName = repoAnalysis.get("repo_name").getAsString();
        JsonObject latestCommitAnalysisResults = repoAnalysis.get("latest_commit_analysis_results").getAsJsonObject();
        int pomFileCountInCommitAnalysis = latestCommitAnalysisResults.get("pom_files_count").getAsInt();
        boolean usesJunitInCommitAnalysis = latestCommitAnalysisResults.get("uses_junit").getAsBoolean();
        boolean usesMockitoInCommitAnalysis = latestCommitAnalysisResults.get("uses_mockito").getAsBoolean();
        JsonArray mockitoDepsInCommitAnalysis = latestCommitAnalysisResults.get("mockito_deps").getAsJsonArray();
        JsonArray testExecutionResultsInCommitAnalysis = latestCommitAnalysisResults.get("test_execution_results").getAsJsonArray();
        boolean hasTestsInCommitAnalysis = AnalysisUtils.hasTests(testExecutionResultsInCommitAnalysis);
        int passedAndNotFlakyInCommitAnalysis = AnalysisUtils.checkTestsAllPassAndNotFlaky(testExecutionResultsInCommitAnalysis);
        //
        JsonObject latestTagAnalysisResults = repoAnalysis.get("latest_tag_analysis_results").getAsJsonObject();
        int pomFileCountInTagAnalysis = latestTagAnalysisResults.get("pom_files_count").getAsInt();
        boolean usesJunitInTagAnalysis = latestTagAnalysisResults.get("uses_junit").getAsBoolean();
        boolean usesMockitoInTagAnalysis = latestTagAnalysisResults.get("uses_mockito").getAsBoolean();
        JsonArray mockitoDepsInTagAnalysis = latestTagAnalysisResults.get("mockito_deps").getAsJsonArray();
        JsonArray testExecutionResultsInTagAnalysis = latestTagAnalysisResults.get("test_execution_results").getAsJsonArray();
        boolean hasTestsInTagAnalysis = AnalysisUtils.hasTests(testExecutionResultsInTagAnalysis);
        int passedAndNotFlakyInTagAnalysis = AnalysisUtils.checkTestsAllPassAndNotFlaky(testExecutionResultsInTagAnalysis);
        //
        if(pomFileCountInCommitAnalysis==0 && pomFileCountInTagAnalysis==0){
          projectsWithZeroPom++;
        }
        else if(pomFileCountInCommitAnalysis==1){
          projectsWithSinglePom++;
        }
        else if (pomFileCountInTagAnalysis==1){
          projectsWithSinglePom++;
        }
        else if(pomFileCountInCommitAnalysis>1){
          projectsWithMultiplePom++;
        }
        else if (pomFileCountInTagAnalysis>1){
          projectsWithMultiplePom++;
        }
        //
        if (pomFileCountInCommitAnalysis==1 && usesJunitInCommitAnalysis) {
          projectsWithJunit++;
        } else if (pomFileCountInTagAnalysis==1 && usesJunitInTagAnalysis) {
          projectsWithJunit++;
        }
        boolean usesMockitoCoreInCommitAnalysis = false;
        String mockitoCoreDepInCommitAnalysis = "";
        boolean usesMockitoCoreInTagAnalysis = false;
        String mockitoCoreDepInTagAnalysis = "";
        if (pomFileCountInCommitAnalysis==1 && usesJunitInCommitAnalysis && usesMockitoInCommitAnalysis) {
          projectsWithMockito++;
          for (int j = 0; j < mockitoDepsInCommitAnalysis.size(); ++j) {
            String mockitoDep = mockitoDepsInCommitAnalysis.get(j).getAsString();
            if(mockitoDep.contains("mockito-core")){
              usesMockitoCoreInCommitAnalysis = true;
              mockitoCoreDepInCommitAnalysis = mockitoDep;
              mockitoCoreDeps.add(mockitoCoreDepInCommitAnalysis);
              break;
            }
          }
        } else if (pomFileCountInTagAnalysis==1 && usesJunitInTagAnalysis && usesMockitoInTagAnalysis) {
          projectsWithMockito++;
          Set<String> mockitoDeps = new HashSet<String>();
          for (int j = 0; j < mockitoDepsInTagAnalysis.size(); ++j) {
            String mockitoDep = mockitoDepsInTagAnalysis.get(j).getAsString();
            if(mockitoDep.contains("mockito-core")){
              usesMockitoCoreInTagAnalysis = true;
              mockitoCoreDepInTagAnalysis = mockitoDep;
              mockitoCoreDeps.add(mockitoCoreDepInTagAnalysis);
              break;
            }
          }
        }
        if(pomFileCountInCommitAnalysis==1 && usesJunitInCommitAnalysis && usesMockitoInCommitAnalysis && usesMockitoCoreInCommitAnalysis){
          projectsWithMockitoCore++;
        } else if (pomFileCountInTagAnalysis==1 && usesJunitInTagAnalysis && usesMockitoInTagAnalysis && usesMockitoCoreInTagAnalysis){
          projectsWithMockitoCore++;
        }
        boolean feasibleMockitoVersionInCommitAnalysis = checkMockitoVersion(mockitoCoreDepInCommitAnalysis);
        boolean feasibleMockitoVersionInTagAnalysis = checkMockitoVersion(mockitoCoreDepInTagAnalysis);
        if(pomFileCountInCommitAnalysis==1 && usesJunitInCommitAnalysis && usesMockitoInCommitAnalysis && usesMockitoCoreInCommitAnalysis && feasibleMockitoVersionInCommitAnalysis){
          projectsWithFeasibleMockitoCore++;
        } else if (pomFileCountInTagAnalysis==1 && usesJunitInTagAnalysis && usesMockitoInTagAnalysis && usesMockitoCoreInTagAnalysis && feasibleMockitoVersionInTagAnalysis){
          projectsWithFeasibleMockitoCore++;
        }
        //projects with feasible mockito and tests
        if (pomFileCountInCommitAnalysis==1 && usesJunitInCommitAnalysis && usesMockitoInCommitAnalysis && usesMockitoCoreInCommitAnalysis && feasibleMockitoVersionInCommitAnalysis && hasTestsInCommitAnalysis) {
          projectsWithTests++;
        } else if (pomFileCountInTagAnalysis==1 && usesJunitInTagAnalysis && usesMockitoInTagAnalysis && usesMockitoCoreInTagAnalysis && feasibleMockitoVersionInTagAnalysis && hasTestsInTagAnalysis) {
          projectsWithTests++;
        }
        //projects with feasible mockito and not flaky tests
        if (pomFileCountInCommitAnalysis==1 && usesJunitInCommitAnalysis && usesMockitoInCommitAnalysis && usesMockitoCoreInCommitAnalysis && feasibleMockitoVersionInCommitAnalysis && passedAndNotFlakyInCommitAnalysis > 0) {
          projectsWithTestsAndNotFlaky++;
        } else if (pomFileCountInTagAnalysis==1 && usesJunitInTagAnalysis && usesMockitoInTagAnalysis && usesMockitoCoreInTagAnalysis && feasibleMockitoVersionInTagAnalysis && passedAndNotFlakyInTagAnalysis > 0) {
          projectsWithTestsAndNotFlaky++;
        }
      }
      System.out.println("Projects with zero pom:"+projectsWithZeroPom);
      System.out.println("Projects with single pom:"+projectsWithSinglePom);
      System.out.println("Projects with multiple pom:"+projectsWithMultiplePom);
      System.out.println("Projects with junit:"+projectsWithJunit);
      System.out.println("Projects with mockito:"+projectsWithMockito);
      System.out.println("Projects with mockito core:"+projectsWithMockitoCore);
      System.out.println("Projects with feasible mockito core:"+projectsWithFeasibleMockitoCore);
      System.out.println("Projects with tests:"+projectsWithTests);
      System.out.println("Projects with not flaky tests:"+projectsWithTestsAndNotFlaky);
//      List<String> sortedMockitoCoreDeps = new ArrayList<String>();
//      sortedMockitoCoreDeps.addAll(mockitoCoreDeps);
//      Collections.sort(sortedMockitoCoreDeps);
//      for(String mockitoDep:sortedMockitoCoreDeps){
//        System.out.println(mockitoDep);
//      }
    }
    catch(Exception e){
      System.out.println("Error while running experiment");
      e.printStackTrace(System.out);
    }
  }

  private boolean checkMockitoVersion(String mockitoDep) {
    boolean result=false;
    if(mockitoDep.equals("")){
      return result;
    }
    String mockitoDepArray[] = mockitoDep.split(":");
    String mockitoVersionItems[] = mockitoDepArray[3].split("\\.");
    int mainVersionNumber = Integer.parseInt(mockitoVersionItems[0]);
    if(mainVersionNumber==2){
      int subVersionNumber = Integer.parseInt(mockitoVersionItems[1]);
      if(subVersionNumber>=3){
        result = true;
        return result;
      }
    } else if (mainVersionNumber==3){
      result = true;
      return result;
    } else if (mainVersionNumber==4){
      result = true;
      return result;
    }
    return result;
  }

}
