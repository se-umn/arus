package edu.xyz.cs.analysis.model;

import com.google.gson.JsonObject;

public class TestExecutionResult {

  private boolean mavenSuccess;
  private int executions;
  private int failed;
  private int errors;
  private int skipped;

  public TestExecutionResult(){
    this.mavenSuccess = false;
    this.executions = 0;
    this.failed = 0;
    this.errors = 0;
    this.skipped = 0;
  }

  public TestExecutionResult(boolean mavenSuccess, int passed, int failed, int errors, int skipped){
    this.mavenSuccess = mavenSuccess;
    this.executions = passed;
    this.failed = failed;
    this.errors = errors;
    this.skipped = skipped;
  }

  public JsonObject toJson(){
    JsonObject result = new JsonObject();
    result.addProperty("maven_success", mavenSuccess);
    result.addProperty("executions", executions);
    result.addProperty("failed", failed);
    result.addProperty("errors", errors);
    result.addProperty("skipped", skipped);
    return result;
  }

  public boolean isMavenSuccess() {
    return mavenSuccess;
  }

  public int getExecutions() {
    return executions;
  }

  public int getFailed() {
    return failed;
  }

  public int getErrors() {
    return errors;
  }

  public int getSkipped() {
    return skipped;
  }
}
