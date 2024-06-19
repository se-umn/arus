package edu.xyz.cs.analysis.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UnusedStub {

    private int traceLineNumber;
    private String stubbedMethodClassName;
    private String stubbedMethodName;
    private CodeLocation stubbingLocation;
    private List<StackComponent> stubbingLocationStack;
    public List<TestAnalysis> testAnalysisList = new ArrayList<>();

    public UnusedStub(int traceLineNumber, String stubbedMethodClassName, String stubbedMethodName, CodeLocation stubbingLocation, List<StackComponent> stubbingLocationStack) {
        this.traceLineNumber=traceLineNumber;
        this.stubbedMethodClassName=stubbedMethodClassName;
        this.stubbedMethodName=stubbedMethodName;
        this.stubbingLocation=stubbingLocation;
        this.stubbingLocationStack=stubbingLocationStack;
    }

    public boolean performSanityChecks() {
        boolean passed = true;
        if(traceLineNumber<1){
            passed = false;
            return passed;
        }
        if(stubbedMethodClassName==null || stubbedMethodClassName.equals("") || stubbedMethodClassName.contains(" ")){
            passed = false;
            return passed;
        }
        if(stubbedMethodName==null || stubbedMethodName.equals("") || stubbedMethodName.contains(" ")){
            passed = false;
            return passed;
        }
        if(!stubbingLocation.performSanityChecks()){
            passed = false;
            return passed;
        }
        if(stubbingLocationStack.size()==0){
            passed = false;
            return passed;
        }
        return passed;
    }

    public int getTraceLineNumber() {
        return traceLineNumber;
    }

    public String getStubbedMethodClassName() {
        return stubbedMethodClassName;
    }

    public String getStubbedMethodName() {
        return stubbedMethodName;
    }

    public CodeLocation getStubbingLocation() {
        return stubbingLocation;
    }

    public void print(){
        System.out.println("UnusedStub" + traceLineNumber);
    }

    public List<StackComponent> getStubbingLocationStack() {
        return stubbingLocationStack;
    }

    public void setTestAnalysisList(List<TestAnalysis> testAnalysisList) {
        this.testAnalysisList = testAnalysisList;
    }

    public String getInfo(){
        return stubbingLocation.getClassName()+stubbingLocation.getMethodName()+stubbingLocation.getLineNum();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnusedStub that = (UnusedStub) o;
        return Objects.equals(stubbedMethodClassName, that.stubbedMethodClassName) && Objects.equals(stubbedMethodName, that.stubbedMethodName) && Objects.equals(stubbingLocation, that.stubbingLocation) && Objects.equals(stubbingLocationStack, that.stubbingLocationStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stubbedMethodClassName, stubbedMethodName, stubbingLocation, stubbingLocationStack);
    }
}
