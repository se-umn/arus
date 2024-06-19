package edu.xyz.cs.analysis.model;

import java.time.ZonedDateTime;

public class Tag {

  private String name;
  private String commitId;
  private ZonedDateTime commitTime;

  public Tag(String name, String commitId, ZonedDateTime commitTime){
    this.name = name;
    this.commitId = commitId;
    this.commitTime = commitTime;
  }

  public String getName() {
    return name;
  }

  public String getCommitId() {
    return commitId;
  }

  public ZonedDateTime getCommitTime() {
    return commitTime;
  }

}
