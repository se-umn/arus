package edu.xyz.cs.analysis.model;

import com.google.gson.JsonObject;

public class Commit {

  private String id = "";
  private int year = 0;
  private int month = 0;
  private int day = 0;

  public Commit(){
  }

  public Commit(String id, int year, int month, int day){
    this.id = id;
    this.year = year;
    this.month = month;
    this.day = day;
  }

  public String getId() {
    return id;
  }

  public int getYear() {
    return year;
  }

  public int getMonth() {
    return month;
  }

  public int getDay() {
    return day;
  }

  public JsonObject toJson(){
    JsonObject result = new JsonObject();
    result.addProperty("id", id);
    result.addProperty("year", year);
    result.addProperty("month", month);
    result.addProperty("day", day);
    return result;
  }
}
