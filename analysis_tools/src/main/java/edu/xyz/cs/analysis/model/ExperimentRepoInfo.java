package edu.xyz.cs.analysis.model;

import com.google.gson.JsonObject;

public class ExperimentRepoInfo {

  private String repoName;
  private boolean isInteresting;
  private boolean analysisStarted;
  private boolean analysisFullyCompleted;
  private Commit commit;
  private String originalDirectory;
  private String modifiedDirectory;
  private String issueMessage;
  private boolean issueWithDependencyTree;
  private boolean issueJunitVersion;
  private boolean issueMockitoVersion;
  private boolean issueWithTestDirectory;
  private boolean issueAddingMockitoCode;
  private boolean issueRunningOriginalTests;
  private boolean issueChangingJunit;
  private boolean issueHandlingParallelTests;
  private boolean issueBasicPomChanges;
  private boolean issueWithCustomizedMockito;
  private boolean issueWithOriginalMockito;
  private boolean issueInTrace;
  private boolean isCloneException;
  private boolean isOtherException;
  private AnalysisStatistics analysisStatistics;


  public ExperimentRepoInfo(){
    this.repoName = "";
    this.isInteresting = false;
    this.analysisStarted = false;
    this.analysisFullyCompleted = false;
    this.commit = new Commit();
    this.originalDirectory = "";
    this.modifiedDirectory = "";
    this.issueMessage = "";
    this.issueWithDependencyTree = false;
    this.issueJunitVersion = false;
    this.issueMockitoVersion = false;
    this.issueWithTestDirectory = false;
    this.issueAddingMockitoCode = false;
    this.issueRunningOriginalTests = false;
    this.issueChangingJunit = false;
    this.issueHandlingParallelTests = false;
    this.issueBasicPomChanges = false;
    this.issueWithCustomizedMockito = false;
    this.issueInTrace = false;
    this.isCloneException = false;
    this.isOtherException = false;
    this.analysisStatistics = new AnalysisStatistics();
    this.issueWithOriginalMockito = false;
  }

  public void setRepoName(String repoName) {
    this.repoName = repoName;
  }

  public void setCommit(Commit commit) {
    this.commit = commit;
  }

  public void setInteresting(boolean interesting) {
    isInteresting = interesting;
  }

  public void setAnalysisStarted(boolean analysisStarted) {
    this.analysisStarted = analysisStarted;
  }

  public void setAnalysisFullyCompleted(boolean analysisFullyCompleted) {
    this.analysisFullyCompleted = analysisFullyCompleted;
  }

  public void setOriginalDirectory(String originalDirectory) {
    this.originalDirectory = originalDirectory;
  }

  public void setModifiedDirectory(String modifiedDirectory) {
    this.modifiedDirectory = modifiedDirectory;
  }

  public void setIssueMessage(String issueMessage) {
    this.issueMessage = issueMessage;
  }

  public void setIssueWithDependencyTree(boolean issueWithDependencyTree) {
    this.issueWithDependencyTree = issueWithDependencyTree;
  }

  public void setIssueJunitVersion(boolean issueJunitVersion) {
    this.issueJunitVersion = issueJunitVersion;
  }

  public void setIssueMockitoVersion(boolean issueMockitoVersion) {
    this.issueMockitoVersion = issueMockitoVersion;
  }

  public void setIssueWithTestDirectory(boolean issueWithTestDirectory) {
    this.issueWithTestDirectory = issueWithTestDirectory;
  }

  public void setIssueAddingMockitoCode(boolean issueAddingMockitoCode) {
    this.issueAddingMockitoCode = issueAddingMockitoCode;
  }

  public void setIssueRunningOriginalTests(boolean issueRunningOriginalTests) {
    this.issueRunningOriginalTests = issueRunningOriginalTests;
  }

  public void setIssueChangingJunit(boolean issueChangingJunit) {
    this.issueChangingJunit = issueChangingJunit;
  }

  public void setIssueHandlingParallelTests(boolean issueHandlingParallelTests) {
    this.issueHandlingParallelTests = issueHandlingParallelTests;
  }

  public void setIssueBasicPomChanges(boolean issueBasicPomChanges) {
    this.issueBasicPomChanges = issueBasicPomChanges;
  }

  public void setIssueWithCustomizedMockito(boolean issueWithCustomizedMockito) {
    this.issueWithCustomizedMockito = issueWithCustomizedMockito;
  }

  public void setIssueInTrace(boolean issueInTrace) {
    this.issueInTrace = issueInTrace;
  }

  public void setCloneException(boolean cloneException) {
    isCloneException = cloneException;
  }

  public void setOtherException(boolean otherException) {
    isOtherException = otherException;
  }

  public void setAnalysisStatistics(AnalysisStatistics analysisStatistics) {
    this.analysisStatistics = analysisStatistics;
  }

  public void setIssueWithOriginalMockito(boolean issueWithOriginalMockito) {
    this.issueWithOriginalMockito = issueWithOriginalMockito;
  }

  public String getRepoName() {
    return repoName;
  }
 public Commit getCommit(){
    return commit;
 }
  public JsonObject toJson(){
    JsonObject result = new JsonObject();
    result.addProperty("repo_name", repoName);
    result.addProperty("is_interesting", isInteresting);
    result.addProperty("analysis_started", analysisStarted);
    result.addProperty("analysis_fully_completed", analysisFullyCompleted);
    result.add("analyzed_commit", commit.toJson());
    result.addProperty("original_directory", originalDirectory);
    result.addProperty("modified_directory", modifiedDirectory);
    result.addProperty("issue_message", issueMessage);
    result.addProperty("issue_with_dependency_tree", issueWithDependencyTree);
    result.addProperty("issue_junit_version", issueJunitVersion);
    result.addProperty("issue_mockito_version", issueMockitoVersion);
    result.addProperty("issue_with_test_directory", issueWithTestDirectory);
    result.addProperty("issue_running_original_tests", issueRunningOriginalTests);
    result.addProperty("issue_changing_junit", issueChangingJunit);
    result.addProperty("issue_handling_parallel_tests", issueHandlingParallelTests);
    result.addProperty("issue_basic_pom_changes", issueBasicPomChanges);
    result.addProperty("issue_adding_mockito_code", issueAddingMockitoCode);
    result.addProperty("issue_with_customized_mockito", issueWithCustomizedMockito);
    result.addProperty("issue_with_original_mockito", issueWithOriginalMockito);
    result.addProperty("issue_in_trace", issueInTrace);
    result.addProperty("is_clone_exception", isCloneException);
    result.addProperty("is_other_exception", isOtherException);
    result.add("analysis_statistics", analysisStatistics.toJson());
    return result;
  }

}
