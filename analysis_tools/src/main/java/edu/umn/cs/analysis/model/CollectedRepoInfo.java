package edu.umn.cs.analysis.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectedRepoInfo {

  private String repoDiskName = "";
  private int tagsCount = 0;
  private Commit commit = new Commit();
  private int pomFilesCount = 0;
  private String mavenWorkingDirInRepo = "";
  private boolean gotDependencyTree = false;
  private boolean usesJunit = false;
  private Set<String> junitDeps = new HashSet<String>();
  private boolean usesMockito = false;
  private Set<String> mockitoDeps = new HashSet<String>();
  private List<TestExecutionResult> testExecutionResults = new ArrayList<TestExecutionResult>();
  private boolean isCloneException = false;
  private boolean isOtherException = false;

  public CollectedRepoInfo(){

  }

  public String getRepoDiskName() {
    return repoDiskName;
  }

  public void setRepoDiskName(String repoDiskName) {
    this.repoDiskName = repoDiskName;
  }

  public int getTagsCount() {
    return tagsCount;
  }

  public void setTagsCount(int tagsCount) {
    this.tagsCount = tagsCount;
  }

  public Commit getCommit() {
    return commit;
  }

  public void setCommit(Commit commit) {
    this.commit = commit;
  }

  public void setPomFilesCount(int pomFilesCount) {
    this.pomFilesCount = pomFilesCount;
  }

  public void setMavenWorkingDirInRepo(String mavenWorkingDirInRepo) {
    this.mavenWorkingDirInRepo = mavenWorkingDirInRepo;
  }

  public void setGotDependencyTree(boolean gotDependencyTree) {
    this.gotDependencyTree = gotDependencyTree;
  }

  public void setUsesJunit(boolean usesJunit) {
    this.usesJunit = usesJunit;
  }

  public void setJunitDeps(Set<String> junitDeps) {
    this.junitDeps = junitDeps;
  }

  public void setUsesMockito(boolean usesMockito) {
    this.usesMockito = usesMockito;
  }

  public void setMockitoDeps(Set<String> mockitoDeps) {
    this.mockitoDeps = mockitoDeps;
  }

  public void setTestExecutionResults(List<TestExecutionResult> testExecutionResults) {
    this.testExecutionResults = testExecutionResults;
  }

  public void setCloneException(boolean cloneException) {
    isCloneException = cloneException;
  }

  public void setOtherException(boolean otherException) {
    isOtherException = otherException;
  }

  public JsonObject toJson(){
    JsonObject result = new JsonObject();
    result.addProperty("repo_disk_name", repoDiskName);
    result.addProperty("tags_count", tagsCount);
    result.add("analyzed_commit", commit.toJson());
    result.addProperty("pom_files_count", pomFilesCount);
    result.addProperty("maven_working_dir_in_repo", mavenWorkingDirInRepo);
    result.addProperty("got_dependency_tree", gotDependencyTree);
    result.addProperty("uses_junit", usesJunit);
    JsonArray junitDepsArrayJson = new JsonArray();
    for(String junitDep:junitDeps){
      junitDepsArrayJson.add(junitDep);
    }
    result.add("junit_deps", junitDepsArrayJson);
    result.addProperty("uses_mockito", usesMockito);
    JsonArray mockitoDepsArrayJson = new JsonArray();
    for(String mockitoDep:mockitoDeps){
      mockitoDepsArrayJson.add(mockitoDep);
    }
    result.add("mockito_deps", mockitoDepsArrayJson);
    JsonArray testExecutionResultsArrayJson = new JsonArray();
    for(TestExecutionResult testExecutionResult:testExecutionResults){
      testExecutionResultsArrayJson.add(testExecutionResult.toJson());
    }
    result.add("test_execution_results", testExecutionResultsArrayJson);
    result.addProperty("is_clone_exception", isCloneException);
    result.addProperty("is_other_exception", isOtherException);
    return result;
  }

}
