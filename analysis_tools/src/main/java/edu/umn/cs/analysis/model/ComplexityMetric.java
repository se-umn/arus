package edu.umn.cs.analysis.model;

import com.google.gson.JsonObject;

public class ComplexityMetric {
    public double cognitiveComplexity;
    public int cyclomaticComplexity;

    public ComplexityMetric(){
        this.cognitiveComplexity = -1;
        this.cyclomaticComplexity = -1;
    }

    public void setCognitiveComplexity(double cognitiveComplexity) {
        this.cognitiveComplexity = cognitiveComplexity;
    }

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public JsonObject toJson(){
        JsonObject result = new JsonObject();
        result.addProperty("cyclomatic_complexity", cyclomaticComplexity);
        result.addProperty("cognitive_complexity", cognitiveComplexity);
        return result;
    }
}
