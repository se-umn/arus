package edu.xyz.cs.analysis.model;

import java.util.List;
import java.util.Objects;

public class StubCreation {

    private int traceLineNumber;
    private String stubIdentifier;
    private ExecutionLocation executionLocation;
    private StubCreationStyle stubCreationStyle;
    private int mockId;
    private int mockResolvedId;
    private String stubbedMethodClassName;
    private String stubbedMethodName;
    private CodeLocation stubbingLocation;
    private List<StackComponent> stack;

    public StubCreation(int traceLineNumber, String stubIdentifier, ExecutionLocation executionLocation, StubCreationStyle stubCreationStyle, int mockId, int mockResolvedId, String stubbedMethodClassName, String stubbedMethodName, CodeLocation stubbingCodeLocation, List<StackComponent> stack){
        this.traceLineNumber=traceLineNumber;
        this.stubIdentifier=stubIdentifier;
        this.executionLocation=executionLocation;
        this.stubCreationStyle=stubCreationStyle;
        this.mockId=mockId;
        this.mockResolvedId=mockResolvedId;
        this.stubbedMethodClassName=stubbedMethodClassName;
        this.stubbedMethodName=stubbedMethodName;
        this.stubbingLocation =stubbingCodeLocation;
        this.stack=stack;
    }

    public boolean performSanityChecks() {
        boolean passed = true;
        //if(traceLineNumber<1 || mockId<0 || mockResolvedId<0){
        if(traceLineNumber<1){
            passed = false;
            return passed;
        }
        if(stubIdentifier==null || stubIdentifier.equals("") || stubIdentifier.contains(" ")){
            passed = false;
            return passed;
        }
        if(!executionLocation.performSanityChecks()){
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
        if(stack.size()==0){
            passed = false;
            return passed;
        }
        return passed;
    }

    public int getTraceLineNumber() {
        return traceLineNumber;
    }

    public String getStubIdentifier() {
        return stubIdentifier;
    }

    public ExecutionLocation getExecutionLocation() {
        return executionLocation;
    }

    public StubCreationStyle getStubCreationStyle() {
        return stubCreationStyle;
    }

    public int getMockId() {
        return mockId;
    }

    public int getMockResolvedId() {
        return mockResolvedId;
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

    public List<StackComponent> getStack() {
        return stack;
    }

    public String getInfo(){
        return stubbingLocation.getClassName()+stubbingLocation.getMethodName()+stubbingLocation.getLineNum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubCreation that = (StubCreation) o;
        return Objects.equals(executionLocation, that.executionLocation) && Objects.equals(stubIdentifier, that.stubIdentifier) && mockId == that.mockId && mockResolvedId == that.mockResolvedId && stubCreationStyle == that.stubCreationStyle && Objects.equals(stubbedMethodClassName, that.stubbedMethodClassName) && Objects.equals(stubbedMethodName, that.stubbedMethodName) && Objects.equals(stubbingLocation, that.stubbingLocation)  && Objects.equals(stack, that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionLocation, stubIdentifier, stubCreationStyle, mockId, mockResolvedId, stubbedMethodClassName, stubbedMethodName, stubbingLocation, stack);
    }

    public void print(){
        System.out.println("StubCreation" + traceLineNumber);
        for(StackComponent stackComponent:stack){
            System.out.println(stackComponent.getFileName()+"#"+stackComponent.getClassName()+"#"+stackComponent.getMethodName()+"#"+stackComponent.getLineInvokedInMethod());
        }
    }
}
