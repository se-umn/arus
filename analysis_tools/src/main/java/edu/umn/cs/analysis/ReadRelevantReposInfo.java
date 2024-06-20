package edu.umn.cs.analysis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ReadRelevantReposInfo {

  public static void main(String args[]){
      ReadRelevantReposInfo rra = new ReadRelevantReposInfo();
      String reposAnalysisFileName = "data/relevant_repos.json";
      rra.printRelevantProjects(reposAnalysisFileName);
  }

  private void printRelevantProjects(String reposAnalysisFileName){
    try {
      BufferedReader br = new BufferedReader(new FileReader(reposAnalysisFileName));
      JsonElement resultsElement = JsonParser.parseReader(br);
      br.close();
      JsonArray reposInfoArray = resultsElement.getAsJsonObject().getAsJsonArray("results");
      int usesMockitoCount = 0;
      JsonArray relevantReposArray = new JsonArray();
      System.out.println(reposInfoArray.size());
      for(int i=0; i<reposInfoArray.size(); ++i){
        //
        JsonObject repoAnalysis = reposInfoArray.get(i).getAsJsonObject();
        String repoName = repoAnalysis.get("repo_name").getAsString();
        //
        JsonObject latestCommitAnalysisResults = repoAnalysis.get("latest_commit_analysis_results").getAsJsonObject();
        String mockitoDepInCommitAnalysis = getMockitoDep(latestCommitAnalysisResults.get("mockito_deps").getAsJsonArray());
        boolean isValidVersionOfMockitoInCommitAnalysis = checkMockitoVersion(mockitoDepInCommitAnalysis);
        JsonArray testExecutionResultsInCommitAnalysis = latestCommitAnalysisResults.get("test_execution_results").getAsJsonArray();
        int passedAndNotFlakyInCommitAnalysis = checkTestsAllPassAndNotFlaky(testExecutionResultsInCommitAnalysis);
        String junit4DepInCommitAnalysis = getJunit4Dep(latestCommitAnalysisResults.get("junit_deps").getAsJsonArray());
        //
        JsonObject latestTagAnalysisResults = repoAnalysis.get("latest_tag_analysis_results").getAsJsonObject();
        String mockitoDepInTagAnalysis = getMockitoDep(latestTagAnalysisResults.get("mockito_deps").getAsJsonArray());
        boolean isValidVersionOfMockitoInTagAnalysis = checkMockitoVersion(mockitoDepInTagAnalysis);
        JsonArray testExecutionResultsInTagAnalysis = latestTagAnalysisResults.get("test_execution_results").getAsJsonArray();
        int passedAndNotFlakyInTagAnalysis = checkTestsAllPassAndNotFlaky(testExecutionResultsInTagAnalysis);
        String junit4DepInTagAnalysis = getJunit4Dep(latestTagAnalysisResults.get("junit_deps").getAsJsonArray());
        //check if the result is interesting
        if(isValidVersionOfMockitoInCommitAnalysis && passedAndNotFlakyInCommitAnalysis>0 && !junit4DepInCommitAnalysis.equals("")){
          JsonObject analyzedCommit = latestCommitAnalysisResults.get("analyzed_commit").getAsJsonObject();
          String commitId = analyzedCommit.get("id").getAsString();
          int year = analyzedCommit.get("year").getAsInt();
          int month = analyzedCommit.get("month").getAsInt();
          int day = analyzedCommit.get("day").getAsInt();
          System.out.println("https://github.com/" + repoName + "\t" + "commit" + "\t" + commitId + "\t"
              + "=DATE(" + year + "," + month + "," + day + ")" + "\t" + mockitoDepInCommitAnalysis + "\t" + passedAndNotFlakyInCommitAnalysis);
          relevantReposArray.add(reposInfoArray.get(i));
          usesMockitoCount++;
        }
        else if (isValidVersionOfMockitoInTagAnalysis && passedAndNotFlakyInTagAnalysis>0 && !junit4DepInTagAnalysis.equals("")){
          JsonObject analyzedCommit = latestTagAnalysisResults.get("analyzed_commit").getAsJsonObject();
          String commitId = analyzedCommit.get("id").getAsString();
          int year = analyzedCommit.get("year").getAsInt();
          int month = analyzedCommit.get("month").getAsInt();
          int day = analyzedCommit.get("day").getAsInt();
          System.out.println("https://github.com/" + repoName + "\t" + "tag" + "\t" + commitId + "\t"
              + "=DATE(" + year + "," + month + "," + day + ")" + "\t" + mockitoDepInTagAnalysis + "\t" + passedAndNotFlakyInTagAnalysis);
          relevantReposArray.add(reposInfoArray.get(i));
          usesMockitoCount++;
        }
      }
      System.out.println(usesMockitoCount);
    }
    catch(Exception e){
      System.out.println("Error while running experiment");
      e.printStackTrace(System.out);
    }
  }

  private String getJunit4Dep(JsonArray junitLabels){
    String result = "";
    for(int j=0; j<junitLabels.size(); ++j) {
      String junitDep = junitLabels.get(j).getAsString();
      String junitDepItems[] = junitDep.split(":");
      if (junitDepItems[0].equals("junit") && junitDepItems[1].equals("junit")) {
        result = junitDep;
        return result;
      }
    }
    return result;
  }


  private String getMockitoDep(JsonArray mockitoLabels){
    String result = "";
    for(int j=0; j<mockitoLabels.size(); ++j) {
      String mockitoDep = mockitoLabels.get(j).getAsString();
      String mockitoDepItems[] = mockitoDep.split(":");
      if (mockitoDepItems[1].equals("mockito-core")) {
        result = mockitoDep;
        return result;
      }
    }
    return result;
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

  private int checkTestsAllPassAndNotFlaky(JsonArray testExecutionResults){
    int passsedAndNotFlaky = 0;
    List<Integer> executionsList = new ArrayList<Integer>();
    for(int i=0; i<testExecutionResults.size(); ++i){
      JsonObject testExecutionResult = testExecutionResults.get(i).getAsJsonObject();
      boolean mavenSuccess = testExecutionResult.get("maven_success").getAsBoolean();
      int executions = Integer.parseInt(testExecutionResult.get("executions").getAsString());
      int failed = Integer.parseInt(testExecutionResult.get("failed").getAsString());
      int errors = Integer.parseInt(testExecutionResult.get("errors").getAsString());
      if(mavenSuccess && executions>0 && failed==0 && errors==0){
        executionsList.add(Integer.valueOf(executions));
      } else {
        passsedAndNotFlaky = 0;
        return passsedAndNotFlaky;
      }
    }
    if(executionsList.size()==0){
      passsedAndNotFlaky = 0;
      return passsedAndNotFlaky;
    }
    int firstRunExecutions = executionsList.get(0).intValue();
    for(Integer executions:executionsList){
      if (firstRunExecutions!=executions.intValue()) {
        passsedAndNotFlaky = 0;
        return passsedAndNotFlaky;
      }
    }
    passsedAndNotFlaky = firstRunExecutions;
    return passsedAndNotFlaky;
  }
}
