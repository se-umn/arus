package edu.umn.cs.analysis.model;

import java.util.ArrayList;
import java.util.List;

public class TestAnalysis {

    private String testClassName="";
    private String testMethodName="";
    private List<StubCreation> stubCreations = new ArrayList<StubCreation>();
    private List<StubInvocation> stubInvocations = new ArrayList<StubInvocation>();
    private List<UnusedStub> unusedStubs = new ArrayList<UnusedStub>();

    public TestAnalysis(){

    }

    public List<StubCreation> findStubsThatShouldBeChanged(){
        List<StubCreation> result = new ArrayList<StubCreation>();
        for(StubCreation stubCreation:stubCreations){
            if(stubCreation.getStubCreationStyle()==StubCreationStyle.THEN_RETURN){
                for(StubInvocation stubInvocation:stubInvocations){
                    if(stubCreation.getStubbedMethodClassName().equals(stubInvocation.getInvokedMethodClassName()) &&
                        stubCreation.getStubbedMethodName().equals(stubInvocation.getInvokedMethodName()) &&
                        stubCreation.getStubbingLocation().equals(stubInvocation.getInvocationLocation())){
                        result.add(stubCreation);
                    }
                }
            }
        }
        return result;
    }

    public boolean performSanityChecks() {
        boolean passed = true;
        if(testClassName==null || testClassName.equals("") || testClassName.contains(" ")){
            System.out.println("no class name");
            passed = false;
            return passed;
        }
        else if(testMethodName==null || testMethodName.equals("") || testMethodName.contains(" ")){
            System.out.println("no method name");
            passed = false;
            return passed;
        }
        for(StubCreation stubCreation:stubCreations){
            if(!stubCreation.performSanityChecks()){
                System.out.println("STUB CREATION SANITY CHECK ERROR");
                stubCreation.print();
                passed = false;
                return passed;
            }
        }
        for(StubInvocation stubInvocation:stubInvocations){
            if(!stubInvocation.performSanityChecks()){
                System.out.println("STUB INVOCATION SANITY CHECK ERROR");
                passed = false;
                stubInvocation.print();
                return passed;
            }
            boolean stubbingPresent = false;
            for(StubCreation stubCreation:stubCreations){
                if(stubInvocation.getMockId()==stubCreation.getMockId() && stubInvocation.getStubbingLocation().equals(stubCreation.getStubbingLocation())){
                    stubbingPresent = true;
                    break;
                }
            }
            if(!stubbingPresent) {
                System.out.println("STUB INVOCATION SANITY CHECK ERROR WRT STUB CREATION");
                stubInvocation.print();
                passed = false;
                return passed;
            }
        }
        for(UnusedStub unusedStub:unusedStubs){
            if(!unusedStub.performSanityChecks()){
                unusedStub.print();
                passed = false;
                return passed;
            }
            boolean stubbingPresent = false;
            for(StubCreation stubCreation:stubCreations){
                if(unusedStub.getStubbingLocation().equals(stubCreation.getStubbingLocation())){
                    stubbingPresent = true;
                    break;
                }
            }
            if(!stubbingPresent) {
                unusedStub.print();
                passed = false;
                return passed;
            }
        }
        return passed;
    }

    public String getTestClassName(){
        return testClassName;
    }

    public String getTestMethodName(){
        return testMethodName;
    }

    public List<StubCreation> getStubCreations() {
        return stubCreations;
    }

    public List<StubInvocation> getStubInvocations() {
        return stubInvocations;
    }

    public List<UnusedStub> getUnusedStubs() {
        return unusedStubs;
    }

    public void setTestClassName(String testClassName) {
        this.testClassName = testClassName;
    }

    public void setTestMethodName(String testMethodName) {
        this.testMethodName = testMethodName;
    }

}
