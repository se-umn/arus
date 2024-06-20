package edu.umn.cs.analysis.model;

import java.util.HashMap;
import java.util.List;

public class ChangeInfo {
//    private TestAnalysis testAnalysis;
    private String source;
    private int lineNum;
    private UnusedStub unusedStub;
    private int type;
    private List<Integer> lineNums;
    private String methodName;
    private List<TestAnalysis> taList;
    private HashMap<String, String> testAnalysesMap;
    private HashMap<String, List<String>> SolAtestAnalysesMap;
    public StubInvocation si;

    public ChangeInfo(String source, int lineNum, UnusedStub unusedStub, int type){
        this.source = source;
        this.lineNum = lineNum;
        this.unusedStub = unusedStub;
        this.type = type;
    }
//    public ChangeInfo(String source, int lineNum, UnusedStub unusedStub, int type){
////        this.testAnalysis = testAnalysis;
//        this.source = source;
//        this.lineNum = lineNum;
//        this.unusedStub = unusedStub;
//        this.type = type;
//    }

    public ChangeInfo(String source, List<TestAnalysis>talist, UnusedStub unusedStub, String methodName, int type, StubInvocation si){
        this.source = source;
        this.taList = talist;
        this.unusedStub = unusedStub;
        this.methodName = methodName;
        this.type = type;
        this.si = si;
    }

    public ChangeInfo(String source, List<TestAnalysis>talist, UnusedStub unusedStub, String methodName, int type){
        this.source = source;
        this.taList = talist;
        this.unusedStub = unusedStub;
        this.methodName = methodName;
        this.type = type;
    }


    public String getSource(){
        return source;
    }

    public UnusedStub getUnusedStub() {
        return unusedStub;
    }

    public int getType() {
        return type;
    }

    public List<Integer> getLineNums() {
        return lineNums;
    }

    public String getMethodName(){
        return methodName;
    }

    public List<TestAnalysis> getTaList() {
        return taList;
    }

    public HashMap<String, String> getTestAnalysesMap() {
        return testAnalysesMap;
    }

    public  HashMap<String, List<String>> getSolATestAnalysesMap() {
        return SolAtestAnalysesMap;
    }


    public void setLineNums(List<Integer> lineNums) {
        this.lineNums = lineNums;
    }

    public void setTestAnalysesMap(HashMap<String, String> newTestAnalysesMap){
        this.testAnalysesMap = newTestAnalysesMap;
    }
    public void setSolATestAnalysesMap(HashMap<String, List<String>> newTestAnalysesMap){
        this.SolAtestAnalysesMap = newTestAnalysesMap;
    }


}
