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

public class ReadCollectedDepsInfo {

  public static void main(String args[]){
      ReadCollectedDepsInfo rra = new ReadCollectedDepsInfo();
      String reposAnalysisFileName = "/Users/mattia/Desktop/analyzed_repos.json";
      rra.findRelevantProjects(reposAnalysisFileName);
  }

  private void findRelevantProjects(String reposAnalysisFileName){
    try {
      BufferedReader br = new BufferedReader(new FileReader(reposAnalysisFileName));
      JsonElement resultsElement = JsonParser.parseReader(br);
      br.close();
      JsonArray reposInfoArray = resultsElement.getAsJsonObject().getAsJsonArray("results");
      System.out.println(reposInfoArray.size());
      for(int i=0; i<reposInfoArray.size(); ++i){
        //
        JsonObject repoAnalysis = reposInfoArray.get(i).getAsJsonObject();
        String repoName = repoAnalysis.get("repo_name").getAsString();
        //
        JsonObject latestCommitAnalysisResults = repoAnalysis.get("latest_commit_analysis_results").getAsJsonObject();
        JsonArray testExecutionResultsInCommitAnalysis = latestCommitAnalysisResults.get("test_execution_results").getAsJsonArray();
        int passedAndNotFlakyInCommitAnalysis = checkTestsAllPassAndNotFlaky(testExecutionResultsInCommitAnalysis);
        JsonArray directDepsInCommitAnalysis = latestCommitAnalysisResults.get("root_deps").getAsJsonArray();
        int usesCommonsCodecInCommitAnalysis = hasDep(directDepsInCommitAnalysis, "commons-codec", "commons-codec");
        int usesCommonsExecInCommitAnalysis = hasDep(directDepsInCommitAnalysis, "org.apache.commons", "commons-exec");
        int usesCommonsValidatorInCommitAnalysis = hasDep(directDepsInCommitAnalysis, "commons-validator", "commons-validator");
        int usesCommonsFileuploadInCommitAnalysis = hasDep(directDepsInCommitAnalysis, "commons-fileupload", "commons-fileupload");
        int usesJodaConvertInCommitAnalysis = hasDep(directDepsInCommitAnalysis, "org.joda", "joda-convert");
        int usesJtarInCommitAnalysis = hasDep(directDepsInCommitAnalysis, "org.kamranzafar", "jtar");
        int usesJavaConcurrentHashTrieMapInCommitAnalysis = hasDep(directDepsInCommitAnalysis, "com.github.romix", "java-concurrent-hash-trie-map");
        //
        JsonObject latestTagAnalysisResults = repoAnalysis.get("latest_tag_analysis_results").getAsJsonObject();
        JsonArray testExecutionResultsInTagAnalysis = latestTagAnalysisResults.get("test_execution_results").getAsJsonArray();
        int passedAndNotFlakyInTagAnalysis = checkTestsAllPassAndNotFlaky(testExecutionResultsInTagAnalysis);
        JsonArray directDepsInTagAnalysis = latestTagAnalysisResults.get("root_deps").getAsJsonArray();
        int usesCommonsCodecInTagAnalysis = hasDep(directDepsInTagAnalysis, "commons-codec", "commons-codec");
        int usesCommonsExecInTagAnalysis = hasDep(directDepsInTagAnalysis, "org.apache.commons", "commons-exec");
        int usesCommonsValidatorInTagAnalysis = hasDep(directDepsInTagAnalysis, "commons-validator", "commons-validator");
        int usesCommonsFileuploadInTagAnalysis = hasDep(directDepsInTagAnalysis, "commons-fileupload", "commons-fileupload");
        int usesJodaConvertInTagAnalysis = hasDep(directDepsInTagAnalysis, "org.joda", "joda-convert");
        int usesJtarInTagAnalysis = hasDep(directDepsInTagAnalysis, "org.kamranzafar", "jtar");
        int usesJavaConcurrentHashTrieMapInTagAnalysis = hasDep(directDepsInTagAnalysis, "com.github.romix", "java-concurrent-hash-trie-map");
        //check if the result is interesting
        if(passedAndNotFlakyInCommitAnalysis>0 &&
                (usesCommonsCodecInCommitAnalysis==1
                        || usesCommonsExecInCommitAnalysis==1
                        || usesCommonsValidatorInCommitAnalysis==1
                        || usesCommonsFileuploadInCommitAnalysis==1
                        || usesJodaConvertInCommitAnalysis==1
                        || usesJtarInCommitAnalysis==1
                        || usesJavaConcurrentHashTrieMapInCommitAnalysis==1)
        ){
          JsonObject analyzedCommit = latestCommitAnalysisResults.get("analyzed_commit").getAsJsonObject();
          String commitId = analyzedCommit.get("id").getAsString();
          int year = analyzedCommit.get("year").getAsInt();
          int month = analyzedCommit.get("month").getAsInt();
          int day = analyzedCommit.get("day").getAsInt();
          System.out.println("https://github.com/" + repoName + "\t" + commitId + "\t"
                  + "=DATE(" + year + "," + month + "," + day + ")" + "\t" + passedAndNotFlakyInCommitAnalysis
                  + "\t" + usesCommonsCodecInCommitAnalysis
                  + "\t" + usesCommonsExecInCommitAnalysis
                  + "\t" + usesCommonsValidatorInCommitAnalysis
                  + "\t" + usesCommonsFileuploadInCommitAnalysis
                  + "\t" + usesJodaConvertInCommitAnalysis
                  + "\t" + usesJtarInCommitAnalysis
                  + "\t" + usesJavaConcurrentHashTrieMapInCommitAnalysis
          );
        }
        else if (passedAndNotFlakyInTagAnalysis>0 &&
                (usesCommonsCodecInTagAnalysis==1
                        || usesCommonsExecInTagAnalysis==1
                        || usesCommonsValidatorInTagAnalysis==1
                        || usesCommonsFileuploadInTagAnalysis==1
                        || usesJodaConvertInTagAnalysis==1
                        || usesJtarInTagAnalysis==1
                        || usesJavaConcurrentHashTrieMapInTagAnalysis==1)
        ){
          JsonObject analyzedCommit = latestTagAnalysisResults.get("analyzed_commit").getAsJsonObject();
          String commitId = analyzedCommit.get("id").getAsString();
          int year = analyzedCommit.get("year").getAsInt();
          int month = analyzedCommit.get("month").getAsInt();
          int day = analyzedCommit.get("day").getAsInt();
          System.out.println("https://github.com/" + repoName + "\t" + commitId + "\t"
              + "=DATE(" + year + "," + month + "," + day + ")" + "\t" + passedAndNotFlakyInTagAnalysis
                  + "\t" + usesCommonsCodecInTagAnalysis
                  + "\t" + usesCommonsExecInTagAnalysis
                  + "\t" + usesCommonsValidatorInTagAnalysis
                  + "\t" + usesCommonsFileuploadInTagAnalysis
                  + "\t" + usesJodaConvertInTagAnalysis
                  + "\t" + usesJtarInTagAnalysis
                  + "\t" + usesJavaConcurrentHashTrieMapInTagAnalysis
          );
        }
      }
    }
    catch(Exception e){
      System.out.println("Error while running experiment");
      e.printStackTrace(System.out);
    }
  }

  //javax.json:javax.json-api:jar:1.1.4:compile
  private int hasDep(JsonArray deps, String depToSearchGroup, String depToSearchName){
    int result = 0;
    for(int i=0; i<deps.size(); ++i){
      String dep = deps.get(i).getAsString();
      String depParts[] = dep.split(":");
      String depGroup = depParts[0];
      String depName = depParts[1];
      String depType = depParts[4];
      if(depGroup.equals(depToSearchGroup) && depName.equals(depToSearchName) && !depType.equals("test")){
        result = 1;
        return result;
      }
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
