package edu.umn.cs.analysis.model;

import com.google.gson.JsonObject;

import java.util.Objects;

public class Metrics {
    public String fileName, type, className;
    public int loc, numOfFields, numOfMethods, wmc;


    public Metrics(String fileName, String className, String type, int wmc, int numOfMethods, int numOfFields, int loc){
        this.fileName = fileName;
        this.type = type;
        this.wmc = wmc;
        this.className = className;
        this.numOfFields = numOfFields;
        this.numOfMethods = numOfMethods;
        this.loc = loc;
    }

    public JsonObject toJson(){
        JsonObject result = new JsonObject();
        result.addProperty("wmc", wmc);
        result.addProperty("num_of_methods", numOfMethods);
        result.addProperty("num_of_fields", numOfFields);
        result.addProperty("loc", loc);
        return result;
    }

    public String[] covertFormat(){
        String[] result = {this.fileName, this.type, String.valueOf(this.wmc), String.valueOf(this.numOfMethods), String.valueOf(this.numOfFields), String.valueOf(this.loc)};
        return result;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public void setNumOfFields(int numOfFields) {
        this.numOfFields = numOfFields;
    }

    public void setNumOfMethods(int numOfMethods) {
        this.numOfMethods = numOfMethods;
    }

    public void setWmc(int wmc) {
        this.wmc = wmc;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metrics metrics = (Metrics) o;
        return loc == metrics.loc && numOfFields == metrics.numOfFields && numOfMethods == metrics.numOfMethods && wmc == metrics.wmc && Objects.equals(fileName, metrics.fileName) && Objects.equals(type, metrics.type);
    }
}
