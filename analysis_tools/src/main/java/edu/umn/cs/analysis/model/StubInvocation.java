package edu.umn.cs.analysis.model;

import java.util.Objects;

public class StubInvocation {

    private int traceLineNumber;
    private ExecutionLocation executionLocation;
    private int mockId;
    private int mockResolvedId;
    private String invokedMethodClassName;
    private String invokedMethodName;
    private CodeLocation invocationLocation;
    private CodeLocation stubbingLocation;

    public StubInvocation(int traceLineNumber, ExecutionLocation executionLocation, int mockId, int mockResolvedId, String stubbedMethodClassName, String stubbedMethodName, CodeLocation invocationLocation, CodeLocation stubbingCodeLocation){
        this.traceLineNumber=traceLineNumber;
        this.executionLocation=executionLocation;
        this.mockId=mockId;
        this.mockResolvedId=mockResolvedId;
        this.invokedMethodClassName=stubbedMethodClassName;
        this.invokedMethodName=stubbedMethodName;
        this.invocationLocation =invocationLocation;
        this.stubbingLocation =stubbingCodeLocation;
    }

    public boolean performSanityChecks() {
        boolean passed = true;
        //if(traceLineNumber<1 || mockId<0 || mockResolvedId<0){
        if(traceLineNumber<1){
            passed = false;
            return passed;
        }
        if(!executionLocation.performSanityChecks()){
            passed = false;
            return passed;
        }
        if(invokedMethodClassName==null || invokedMethodClassName.equals("") || invokedMethodClassName.contains(" ")){
            passed = false;
            return passed;
        }
        if(invokedMethodName==null || invokedMethodName.equals("") || invokedMethodName.contains(" ")){
            passed = false;
            return passed;
        }
        if(!invocationLocation.performSanityChecks()){
            passed = false;
            return passed;
        }
        if(!stubbingLocation.performSanityChecks()){
            passed = false;
            return passed;
        }
        return passed;
    }

    public int getTraceLineNumber() {
        return traceLineNumber;
    }

    public ExecutionLocation getExecutionLocation() {
        return executionLocation;
    }

    public int getMockId() {
        return mockId;
    }

    public int getMockResolvedId() {
        return mockResolvedId;
    }

    public String getInvokedMethodClassName() {
        return invokedMethodClassName;
    }

    public String getInvokedMethodName() {
        return invokedMethodName;
    }

    public CodeLocation getInvocationLocation() {
        return invocationLocation;
    }

    public CodeLocation getStubbingLocation() { return stubbingLocation;}

    public String getInfo(){
        return stubbingLocation.getClassName()+stubbingLocation.getMethodName()+stubbingLocation.getLineNum();
    }
    public void print(){
        System.out.println("StubInvocation" + traceLineNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubInvocation that = (StubInvocation) o;
        return Objects.equals(executionLocation, that.executionLocation) && mockId == that.mockId && mockResolvedId == that.mockResolvedId && Objects.equals(invokedMethodClassName, that.invokedMethodClassName)  && Objects.equals(invokedMethodName, that.invokedMethodName) && Objects.equals(invocationLocation, that.invocationLocation) && Objects.equals(stubbingLocation, that.stubbingLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionLocation, mockId, mockResolvedId, invokedMethodClassName, invokedMethodName, invocationLocation, stubbingLocation);
    }
}
