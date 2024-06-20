package edu.umn.cs.analysis;

import edu.umn.cs.analysis.model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class CustomizedMockitoUtils {

    //structure
    private static final String TEST_ANALYSIS_START = "###test_analysis_start";
    private static final String TEST_ANALYSIS_METHOD_CLASS = "###test_analysis_method_class";
    private static final String TEST_ANALYSIS_METHOD_NAME = "###test_analysis_method_name";
    private static final String TEST_ANALYSIS_END = "###test_analysis_end";
    private static final String TEST_CONSTRUCTOR_EXECUTION_START = "###test_constructor_execution_start";
    private static final String TEST_CONSTRUCTOR_CLASS = "###test_constructor_class";
    private static final String TEST_CONSTRUCTOR_NAME = "###test_constructor_name";
    private static final String TEST_CONSTRUCTOR_EXECUTION_END = "###test_constructor_execution_end";
    private static final String TEST_BEFORE_EXECUTION_START = "###test_before_execution_start";
    private static final String TEST_BEFORE_CLASS = "###test_before_class";
    private static final String TEST_BEFORE_NAME = "###test_before_name";
    private static final String TEST_BEFORE_EXECUTION_END = "###test_before_execution_end";
    private static final String TEST_METHOD_EXECUTION_START = "###test_method_execution_start";
    private static final String TEST_METHOD_CLASS = "###test_method_class";
    private static final String TEST_METHOD_NAME = "###test_method_name";
    private static final String TEST_METHOD_EXECUTION_END = "###test_method_execution_end";
    private static final String TEST_AFTER_EXECUTION_START = "###test_after_execution_start";
    private static final String TEST_AFTER_CLASS = "###test_after_class";
    private static final String TEST_AFTER_NAME = "###test_after_name";
    private static final String TEST_AFTER_EXECUTION_END = "###test_after_execution_end";

    //stub creation
    private static final String STUB_CREATION_INFO_START = "###stub_creation_info_start";
    private static final String MOCKITO_TD_ID = "###mockito_td_id";
    private static final String MOCKITO_TD_RESOLVED_ID = "###mockito_td_resolved_id";
    private static final String STUB_CREATION_STYLE = "###stub_creation_style";
    private static final String STUBBED_METHOD_CLASS = "###stubbed_method_class";
    private static final String STUBBED_METHOD_NAME = "###stubbed_method_name";
    private static final String STUBBING_LOCATION = "###stubbing_location";
    private static final String STACK = "###stack";
    private static final String STUBBING_IDENTIFIER = "###stubbing_identifier";
    private static final String STUB_CREATION_INFO_END = "###stub_creation_info_end";

    //stub invocation
    private static final String STUB_INVOCATION_INFO_START = "###stub_invocation_info_start";
    private static final String INVOKED_METHOD_CLASS = "###invoked_method_class";
    private static final String INVOKED_METHOD_NAME = "###invoked_method_name";
    private static final String INVOCATION_LOCATION = "###invocation_location";
    private static final String STUB_INVOCATION_INFO_END = "###stub_invocation_info_end";
    private static final String INVOKED_LOCATION_NATIVE_FILE = "Native Method";
    private static final String INVOKED_LOCATION_UNKNOWN_SOURCE = "Unknown Source";

    //unused stub info
    private static final String UNUSED_STUB_INFO_START = "###unused_stub_info_start";
    private static final String UNUSED_STUB_INFO_END = "###unused_stub_info_end";

    //mismatched stub info
    private static final String MISMATCHED_STUB_INFO_START = "###mismatched_stub_info_start";
    private static final String MISMATCHED_STUB_INFO_END = "###mismatched_stub_info_end";



    private static Map<String, List<StackComponent>> stubbingIdentifierToStackMap = new HashMap<String, List<StackComponent>>();

    public static void readStubCreation(BufferedReader brTrace,Counter lineCount, TestAnalysis testAnalysis, int traceLineNum, ExecutionLocation location) throws Exception {
        //creation style
        String stubCreationStyleLine = brTrace.readLine();
        lineCount.increment();
        if(stubCreationStyleLine==null || !stubCreationStyleLine.startsWith(CustomizedMockitoUtils.STUB_CREATION_STYLE)){
            throw new RuntimeException("did not get a stub creation style in stub creation "+"#"+lineCount.getCount()+"#"+(stubCreationStyleLine==null?"null":stubCreationStyleLine));
        }
        StubCreationStyle stubCreationStyle;
        stubCreationStyleLine = stubCreationStyleLine.replace(CustomizedMockitoUtils.STUB_CREATION_STYLE+":","");
        if(stubCreationStyleLine.equals("then_return")){
            stubCreationStyle = StubCreationStyle.THEN_RETURN;
        }
        else if(stubCreationStyleLine.equals("do_return")){
            stubCreationStyle = StubCreationStyle.DO_RETURN;
        }
        else{
            throw new RuntimeException("do not know the stub creation style"+"#"+lineCount.getCount()+"#"+(stubCreationStyleLine==null?"null":stubCreationStyleLine));
        }
        //id
        String mockIdLine = brTrace.readLine();
        lineCount.increment();
        if(mockIdLine==null || !mockIdLine.startsWith(CustomizedMockitoUtils.MOCKITO_TD_ID)){
            throw new RuntimeException("did not get a id in stub creation "+"#"+lineCount.getCount()+"#"+(mockIdLine==null?"null":mockIdLine));
        }
        int mockId = Integer.parseInt(mockIdLine.replace(CustomizedMockitoUtils.MOCKITO_TD_ID+":",""));
        //resolved id
        String mockResolvedIdLine = brTrace.readLine();
        lineCount.increment();
        if(mockResolvedIdLine==null || !mockResolvedIdLine.startsWith(CustomizedMockitoUtils.MOCKITO_TD_RESOLVED_ID)){
            throw new RuntimeException("did not get a resolved id in stub creation"+"#"+lineCount.getCount()+"#"+(mockResolvedIdLine==null?"null":mockResolvedIdLine));
        }
        int mockResolvedId = Integer.parseInt(mockResolvedIdLine.replace(CustomizedMockitoUtils.MOCKITO_TD_RESOLVED_ID+":",""));
        //stubbed method class
        String stubbedMethodClassLine = brTrace.readLine();
        lineCount.increment();
        if(stubbedMethodClassLine==null || !stubbedMethodClassLine.startsWith(CustomizedMockitoUtils.STUBBED_METHOD_CLASS)){
            throw new RuntimeException("did not get a stubbed class in stub creation"+"#"+lineCount.getCount()+"#"+(stubbedMethodClassLine==null?"null":stubbedMethodClassLine));
        }
        String stubbedClassName = processClassName(stubbedMethodClassLine.replace(CustomizedMockitoUtils.STUBBED_METHOD_CLASS+":",""));
        //stubbed method name
        String methodNameLine = brTrace.readLine();
        lineCount.increment();
        if(methodNameLine==null || !methodNameLine.startsWith(CustomizedMockitoUtils.STUBBED_METHOD_NAME)){
            throw new RuntimeException("did not get a stubbed method name in stub creation"+"#"+lineCount.getCount()+"#"+(methodNameLine==null?"null":methodNameLine));
        }
        String stubbedMethodName = methodNameLine.replace(CustomizedMockitoUtils.STUBBED_METHOD_NAME+":","");
        //stubbing location
        String stubbingLocationLine = brTrace.readLine();
        lineCount.increment();
        if(stubbingLocationLine==null || !stubbingLocationLine.startsWith(CustomizedMockitoUtils.STUBBING_LOCATION)){
            throw new RuntimeException("did not get a stubbed location in stub creation"+"#"+lineCount.getCount()+"#"+(stubbingLocationLine==null?"null":stubbingLocationLine));
        }
        String stubbingLocationInfo = stubbingLocationLine.replace(CustomizedMockitoUtils.STUBBING_LOCATION +":-> at ","");
        String stubbingLocationClassAndMethodInfo = stubbingLocationInfo.substring(0, stubbingLocationInfo.indexOf("("));
        String stubbingLocationClassName = stubbingLocationClassAndMethodInfo.substring(0, stubbingLocationClassAndMethodInfo.lastIndexOf("."));
        String stubbingLocationMethodName = stubbingLocationClassAndMethodInfo.substring(stubbingLocationClassAndMethodInfo.lastIndexOf(".")+1);
        String stubbingFileInfo = stubbingLocationInfo.substring(stubbingLocationInfo.indexOf("(") + 1, stubbingLocationInfo.lastIndexOf(")"));
        String stubbingFileInfoArray[] = stubbingFileInfo.split(":");
        String stubbingFileName = stubbingFileInfoArray[0];
        int stubbingLineNumber = Integer.parseInt(stubbingFileInfoArray[1]);
        CodeLocation stubbingCodeLocation = new CodeLocation(stubbingFileName, stubbingLocationClassName, stubbingLocationMethodName, stubbingLineNumber);
        //stack
        String stackLine = brTrace.readLine();
        lineCount.increment();
        if(stackLine==null || !stackLine.startsWith(CustomizedMockitoUtils.STACK)){
            throw new RuntimeException("did not get a stack in stub creation"+"#"+lineCount.getCount()+"#"+(stackLine==null?"null":stackLine));
        }
        List<StackComponent> stack = new ArrayList<StackComponent>();
        //solution for running cloudbees-jenkins-advisor-plugin
//        System.out.println("****");
//        System.out.println(stubbingCodeLocation.getClassName());
//        System.out.println(stubbingCodeLocation.getMethodName());
//        System.out.println(location.getClassName());
//        System.out.println(location.getMethodName());
        if(stubbingCodeLocation.getClassName().equals("com.cloudbees.jenkins.plugins.advisor.AdvisorGlobalConfigurationTest$DoConfigureInfo") &&
                stubbingCodeLocation.getMethodName().equals("call") &&
                (location.getClassName().equals("com.cloudbees.jenkins.plugins.advisor.AdvisorGlobalConfigurationTest") && location.getMethodName().equals("testServerConnectionFailure") ||
                location.getClassName().equals("com.cloudbees.jenkins.plugins.advisor.AdvisorGlobalConfigurationTest") && location.getMethodName().equals("testServerConnectionPass") ||
                location.getClassName().equals("com.cloudbees.jenkins.plugins.advisor.AdvisorGlobalConfigurationTest") && location.getMethodName().equals("testSendEmail") ||
                location.getClassName().equals("com.cloudbees.jenkins.plugins.advisor.AdvisorGlobalConfigurationTest") && location.getMethodName().equals("testConfigure") ||
                location.getClassName().equals("com.cloudbees.jenkins.plugins.advisor.AdvisorGlobalConfigurationTest") && location.getMethodName().equals("testPersistence") ||
                location.getClassName().equals("com.cloudbees.jenkins.plugins.advisor.AdvisorGlobalConfigurationTest") && location.getMethodName().equals("testSaveExcludedComponents"))){
                StackComponent dummyStackComponent = new StackComponent("AdvisorGlobalConfigurationTest.java", "com.cloudbees.jenkins.plugins.advisor.AdvisorGlobalConfigurationTest$DoConfigureInfo", "call", 268);
                stack.add(dummyStackComponent);
        }
        else if(stubbingCodeLocation.getClassName().equals("com.datapipe.jenkins.vault.VaultBindingStepWithMockAccessor$1") &&
                stubbingCodeLocation.getMethodName().equals("read") &&
                (location.getClassName().equals("com.datapipe.jenkins.vault.it.folder.FolderIT") && location.getMethodName().equals("jenkinsfileShouldOverrideFolderConfig"))){
            StackComponent dummyStackComponent = new StackComponent("VaultBuildWrapperTest.java", "com.datapipe.jenkins.vault.VaultBindingStepWithMockAccessor$1", "read", 76);
            stack.add(dummyStackComponent);
        }
        else {
            stackLine = stackLine.replace("###stack:", "");
            String stackParts[] = stackLine.split("#");
            int interestingStackStart = -1;
            int interestingStackEnd = -1;
//            System.out.println("start");
//            System.out.println(stubbingLocationClassName);
//            System.out.println(stubbingLocationMethodName);
//            System.out.println("end");
//            System.out.println(location.getClassName());
//            System.out.println(location.getMethodName());
            for (int i = 0; i < stackParts.length; ++i) {
                String methodCallParts[] = stackParts[i].split(";");
                String methodCallClassName = methodCallParts[1];
                String methodCallMethodName = methodCallParts[2];
                //MF: FIXME not sure why it was added
                //project that does not need this: jenkinsci/hashicorp-vault-plugin
                ////////
                //null;com.datapipe.jenkins.vault.VaultAccessor$MockitoMock$1375540915;init;-1
                //VaultBuildWrapperTest.java;com.datapipe.jenkins.vault.VaultBuildWrapperTest$TestWrapper;<init>;98
                //VaultBuildWrapperTest.java;com.datapipe.jenkins.vault.VaultBuildWrapperTest;testWithNonExistingPath;46
                ////////
                //project that needs this: ?
//                if (methodCallMethodName.equals("<init>")) {
//                    //System.out.println("<init>");
//                    String newMethodCallMethodName = "";
//                    int lastDotIndex = methodCallClassName.lastIndexOf('.');
//                    if (lastDotIndex != -1) {
//                        newMethodCallMethodName = methodCallClassName.substring(lastDotIndex + 1);
//                    } else {
//                        newMethodCallMethodName = methodCallClassName;
//                    }
//                    methodCallMethodName = newMethodCallMethodName;
//                    //System.out.println(methodCallMethodName);
//                }
                int lineInvokedInMethodCall = Integer.parseInt(methodCallParts[3]);
//                System.out.println("#####");
//                System.out.println(methodCallClassName);
//                System.out.println(methodCallMethodName);
                if (methodCallClassName.equals(stubbingLocationClassName) && methodCallMethodName.equals(stubbingLocationMethodName) && lineInvokedInMethodCall == stubbingLineNumber) {
                    interestingStackStart = i;
                }
                if (methodCallClassName.equals(location.getClassName()) && methodCallMethodName.equals(location.getMethodName())) {
                    interestingStackEnd = i;
                }
            }
//            System.out.println(interestingStackStart);
//            System.out.println(interestingStackEnd);
            if (interestingStackStart == -1 || interestingStackEnd == -1) {
                throw new RuntimeException("did not find the right stack parts:" + lineCount.getCount() + "#" + interestingStackStart + "#" + interestingStackEnd);
            }
            for (int i = interestingStackStart; i <= interestingStackEnd; ++i) {
                String methodCallParts[] = stackParts[i].split(";");
                String methodCallFileName = methodCallParts[0];
                String methodCallClassName = methodCallParts[1];
                String methodCallMethodName = methodCallParts[2];
                int lineInvokedInMethodCall = Integer.parseInt(methodCallParts[3]);
                StackComponent stackComponent = new StackComponent(methodCallFileName, methodCallClassName, methodCallMethodName, lineInvokedInMethodCall);
                stack.add(stackComponent);
            }
        }
        //stubbing identifier
        String stubbingIdentifierLine = brTrace.readLine();
        lineCount.increment();
        if(stubbingIdentifierLine==null || !stubbingIdentifierLine.startsWith(CustomizedMockitoUtils.STUBBING_IDENTIFIER)){
            throw new RuntimeException("did not get a stubbing identifier in stub creation"+"#"+lineCount.getCount()+"#"+(stubbingIdentifierLine==null?"null":stubbingIdentifierLine));
        }
        String stubbingIdentifier = stubbingIdentifierLine.replace(CustomizedMockitoUtils.STUBBING_IDENTIFIER+":","");
        if(stubbingIdentifierToStackMap.containsKey(stubbingIdentifier)){
            List<StackComponent> existingStack = stubbingIdentifierToStackMap.get(stubbingIdentifier);
            boolean sameStacks = true;
            if(existingStack.size()!=stack.size()){
                sameStacks = false;
            }
            else {
                for (int i = 0; i < existingStack.size(); ++i) {
                    StackComponent existingStackComponent = existingStack.get(i);
                    StackComponent currentStackComponent = stack.get(i);
                    if(!existingStackComponent.equals(currentStackComponent)){
                        sameStacks = false;
                    }
                }
            }
            if(!sameStacks){
                throw new RuntimeException("two stubs with the same identifier" + "#" + lineCount.getCount() + "#" + (stubbingIdentifierLine == null ? "null" : stubbingIdentifierLine));
            }
        }
        stubbingIdentifierToStackMap.put(stubbingIdentifier, stack);
        //stubbing creation end
        String stubbingCreationEndLine = brTrace.readLine();
        lineCount.increment();
        if(stubbingCreationEndLine==null || !stubbingCreationEndLine.equals(CustomizedMockitoUtils.STUB_CREATION_INFO_END)){
            throw new RuntimeException("did not get the end of stubbing creation "+"#"+lineCount.getCount()+"#"+(stubbingCreationEndLine==null?"null":stubbingCreationEndLine));
        }
        StubCreation stubCreation = new StubCreation(traceLineNum, stubbingIdentifier, location, stubCreationStyle, mockId, mockResolvedId, stubbedClassName, stubbedMethodName, stubbingCodeLocation, stack);
        testAnalysis.getStubCreations().add(stubCreation);
    }

    public static void readStubInvocation(BufferedReader brTrace,Counter lineCount, TestAnalysis testAnalysis, int traceLineNum, ExecutionLocation location) throws Exception {
        //id
        String mockIdLine = brTrace.readLine();
        lineCount.increment();
        if(mockIdLine==null || !mockIdLine.startsWith(CustomizedMockitoUtils.MOCKITO_TD_ID)){
            throw new RuntimeException("did not get a id in stub invocation "+"#"+lineCount.getCount()+"#"+(mockIdLine==null?"null":mockIdLine));
        }
        int mockId = Integer.parseInt(mockIdLine.replace(CustomizedMockitoUtils.MOCKITO_TD_ID+":",""));
        //resolved id
        String mockResolvedIdLine = brTrace.readLine();
        lineCount.increment();
        if(mockResolvedIdLine==null || !mockResolvedIdLine.startsWith(CustomizedMockitoUtils.MOCKITO_TD_RESOLVED_ID)){
            throw new RuntimeException("did not get a resolved id in stub invocation"+"#"+lineCount.getCount()+"#"+(mockResolvedIdLine==null?"null":mockResolvedIdLine));
        }
        int mockResolvedId = Integer.parseInt(mockResolvedIdLine.replace(CustomizedMockitoUtils.MOCKITO_TD_RESOLVED_ID+":",""));
        //invoked method class
        String invokedMethodClassLine = brTrace.readLine();
        lineCount.increment();
        if(invokedMethodClassLine==null || !invokedMethodClassLine.startsWith(CustomizedMockitoUtils.INVOKED_METHOD_CLASS)){
            throw new RuntimeException("did not get a stubbed class in stub invocation"+"#"+lineCount.getCount()+"#"+(invokedMethodClassLine==null?"null":invokedMethodClassLine));
        }
        String invokedClassName = processClassName(invokedMethodClassLine.replace(CustomizedMockitoUtils.INVOKED_METHOD_CLASS+":",""));
        //invoked method name
        String invokedMethodNameLine = brTrace.readLine();
        lineCount.increment();
        if(invokedMethodNameLine==null || !invokedMethodNameLine.startsWith(CustomizedMockitoUtils.INVOKED_METHOD_NAME)){
            throw new RuntimeException("did not get a stubbed method name in stub invocation"+"#"+lineCount.getCount()+"#"+(invokedMethodNameLine==null?"null":invokedMethodNameLine));
        }
        String invokedMethodName = invokedMethodNameLine.replace(CustomizedMockitoUtils.INVOKED_METHOD_NAME+":","");
        //invocation location
        String invocationLocationLine = brTrace.readLine();
        lineCount.increment();
        if(invocationLocationLine==null || !invocationLocationLine.startsWith(CustomizedMockitoUtils.INVOCATION_LOCATION)){
            throw new RuntimeException("did not get a stubbed location in stub invocation"+"#"+lineCount.getCount()+"#"+(invocationLocationLine==null?"null":invocationLocationLine));
        }
        String invocationLocationInfo = invocationLocationLine.replace(CustomizedMockitoUtils.INVOCATION_LOCATION+":-> at ","");
        String invocationLocationClassAndMethodInfo = invocationLocationInfo.substring(0, invocationLocationInfo.indexOf("("));
        String invocationLocationClassName = invocationLocationClassAndMethodInfo.substring(0, invocationLocationClassAndMethodInfo.lastIndexOf("."));
        String invocationLocationMethodName = invocationLocationClassAndMethodInfo.substring(invocationLocationClassAndMethodInfo.lastIndexOf(".")+1);
        String invocationFileInfo = invocationLocationInfo.substring(invocationLocationInfo.indexOf("(") + 1, invocationLocationInfo.lastIndexOf(")"));
        String invocationFileName = "";
        int invocationLineNumber = -1;
        if(invocationFileInfo.equals(CustomizedMockitoUtils.INVOKED_LOCATION_NATIVE_FILE) || invocationFileInfo.equals(CustomizedMockitoUtils.INVOKED_LOCATION_UNKNOWN_SOURCE)){
            //special case for invocation location in native code
            invocationFileName = CodeLocation.NATIVE_LOCATION;
            invocationLineNumber = 0;
        }
        else{
            String invocationFileInfoArray[] = invocationFileInfo.split(":");
            invocationFileName = invocationFileInfoArray[0];
            invocationLineNumber = Integer.parseInt(invocationFileInfoArray[1]);
        }
        CodeLocation invocationCodeLocation = new CodeLocation(invocationFileName, invocationLocationClassName, invocationLocationMethodName, invocationLineNumber);
        //stubbing location
        String stubbingLocationLine = brTrace.readLine();
        lineCount.increment();
        if(stubbingLocationLine==null || !stubbingLocationLine.startsWith(CustomizedMockitoUtils.STUBBING_LOCATION)){
            throw new RuntimeException("did not get a stubbed location in stub invocation"+"#"+lineCount.getCount()+"#"+(stubbingLocationLine==null?"null":stubbingLocationLine));
        }
        String stubbingLocationInfo = stubbingLocationLine.replace(CustomizedMockitoUtils.STUBBING_LOCATION +":-> at ","");
        String stubbingLocationClassAndMethodInfo = stubbingLocationInfo.substring(0, stubbingLocationInfo.indexOf("("));
        String stubbingLocationClassName = stubbingLocationClassAndMethodInfo.substring(0, stubbingLocationClassAndMethodInfo.lastIndexOf("."));
        String stubbingLocationMethodName = stubbingLocationClassAndMethodInfo.substring(stubbingLocationClassAndMethodInfo.lastIndexOf(".")+1);
        String stubbingFileInfo = stubbingLocationInfo.substring(stubbingLocationInfo.indexOf("(") + 1, stubbingLocationInfo.lastIndexOf(")"));
        String stubbingFileInfoArray[] = stubbingFileInfo.split(":");
        String stubbingFileName = stubbingFileInfoArray[0];
        int stubbingLineNumber = Integer.parseInt(stubbingFileInfoArray[1]);
        CodeLocation stubbingCodeLocation = new CodeLocation(stubbingFileName, stubbingLocationClassName, stubbingLocationMethodName, stubbingLineNumber);
        //stubbing creation end
        String invocationEndLine = brTrace.readLine();
        lineCount.increment();
        if(invocationEndLine==null || !invocationEndLine.equals(CustomizedMockitoUtils.STUB_INVOCATION_INFO_END)){
            throw new RuntimeException("did not get the end of stubbing creation "+"#"+lineCount.getCount()+"#"+(invocationEndLine==null?"null":invocationEndLine));
        }
        StubInvocation stubInvocation = new StubInvocation(traceLineNum, location, mockId, mockResolvedId, invokedClassName, invokedMethodName, invocationCodeLocation, stubbingCodeLocation);
        testAnalysis.getStubInvocations().add(stubInvocation);
    }

    public static void readUnusedStub(BufferedReader brTrace,Counter lineCount, TestAnalysis testAnalysis, int traceLineNum) throws Exception {
        //stubbed method class
        String stubbedMethodClassLine = brTrace.readLine();
        lineCount.increment();
        if(stubbedMethodClassLine==null || !stubbedMethodClassLine.startsWith(CustomizedMockitoUtils.STUBBED_METHOD_CLASS)){
            throw new RuntimeException("did not get a stubbed class in unused stub"+"#"+lineCount.getCount()+"#"+(stubbedMethodClassLine==null?"null":stubbedMethodClassLine));
        }
        String stubbedClassName = processClassName(stubbedMethodClassLine.replace(CustomizedMockitoUtils.STUBBED_METHOD_CLASS+":",""));
        //stubbed method name
        String methodNameLine = brTrace.readLine();
        lineCount.increment();
        if(methodNameLine==null || !methodNameLine.startsWith(CustomizedMockitoUtils.STUBBED_METHOD_NAME)){
            throw new RuntimeException("did not get a stubbed method name in unused stub"+"#"+lineCount.getCount()+"#"+(methodNameLine==null?"null":methodNameLine));
        }
        String stubbedMethodName = methodNameLine.replace(CustomizedMockitoUtils.STUBBED_METHOD_NAME+":","");
        //stubbing location
        String stubbingLocationLine = brTrace.readLine();
        lineCount.increment();
        if(stubbingLocationLine==null || !stubbingLocationLine.startsWith(CustomizedMockitoUtils.STUBBING_LOCATION)){
            throw new RuntimeException("did not get a stubbed location in unused stub"+"#"+lineCount.getCount()+"#"+(stubbingLocationLine==null?"null":stubbingLocationLine));
        }
        String stubbingLocationInfo = stubbingLocationLine.replace(CustomizedMockitoUtils.STUBBING_LOCATION +":-> at ","");
        String stubbingLocationClassAndMethodInfo = stubbingLocationInfo.substring(0, stubbingLocationInfo.indexOf("("));
        String stubbingLocationClassName = stubbingLocationClassAndMethodInfo.substring(0, stubbingLocationClassAndMethodInfo.lastIndexOf("."));
        String stubbingLocationMethodName = stubbingLocationClassAndMethodInfo.substring(stubbingLocationClassAndMethodInfo.lastIndexOf(".")+1);
        String stubbingFileInfo = stubbingLocationInfo.substring(stubbingLocationInfo.indexOf("(") + 1, stubbingLocationInfo.lastIndexOf(")"));
        String stubbingFileInfoArray[] = stubbingFileInfo.split(":");
        String stubbingFileName = stubbingFileInfoArray[0];
        int stubbingLineNumber = Integer.parseInt(stubbingFileInfoArray[1]);
        CodeLocation stubbingCodeLocation = new CodeLocation(stubbingFileName, stubbingLocationClassName, stubbingLocationMethodName, stubbingLineNumber);
        //retrieve stack
        String stubbingIdentifierLine = brTrace.readLine();
        lineCount.increment();
        if(stubbingIdentifierLine==null || !stubbingIdentifierLine.startsWith(CustomizedMockitoUtils.STUBBING_IDENTIFIER)){
            throw new RuntimeException("did not get a stubbing identifier in stub creation"+"#"+lineCount.getCount()+"#"+(stubbingIdentifierLine==null?"null":stubbingIdentifierLine));
        }
        String stubbingIdentifier = stubbingIdentifierLine.replace(CustomizedMockitoUtils.STUBBING_IDENTIFIER+":","");
        if(!stubbingIdentifierToStackMap.containsKey(stubbingIdentifier)){
            throw new RuntimeException("did not find stub identifier"+"#"+lineCount.getCount()+"#"+(stubbingIdentifierLine==null?"null":stubbingIdentifierLine));
        }
        List<StackComponent> stack = stubbingIdentifierToStackMap.get(stubbingIdentifier);
        //stubbing creation end
        String unusedStubEndLine = brTrace.readLine();
        lineCount.increment();
        if(unusedStubEndLine==null || !unusedStubEndLine.equals(CustomizedMockitoUtils.UNUSED_STUB_INFO_END)){
            throw new RuntimeException("did not get the end of unused stub "+"#"+lineCount.getCount()+"#"+(unusedStubEndLine==null?"null":unusedStubEndLine));
        }
        UnusedStub unusedStub = new UnusedStub(traceLineNum, stubbedClassName, stubbedMethodName, stubbingCodeLocation, stack);
        testAnalysis.getUnusedStubs().add(unusedStub);
    }

    private static void readBeforeExecution(BufferedReader brTrace, Counter lineCount, TestAnalysis testAnalysis) throws Exception {
        String line=null;
        line = brTrace.readLine();
        lineCount.increment();
        //test class name
        if(line==null || !line.startsWith(CustomizedMockitoUtils.TEST_BEFORE_CLASS)){
            throw new RuntimeException("expected test_before_class but got"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
        String beforeClassName=line.split(":")[1];//do something with this if needed
        //test method name
        line = brTrace.readLine();
        lineCount.increment();
        if(line==null || !line.startsWith(CustomizedMockitoUtils.TEST_BEFORE_NAME)){
            throw new RuntimeException("expected test_before_name but got "+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
        String beforeMethodName=line.split(":")[1];//do something with this if needed
        ExecutionLocation executionLocation = new ExecutionLocation(ExecutionLocationType.BEFORE_METHOD, beforeClassName, beforeMethodName);
        boolean completed = false;
        while((line=brTrace.readLine())!=null){
            lineCount.increment();
            if(line.equals(CustomizedMockitoUtils.TEST_BEFORE_EXECUTION_END)){
                completed=true;
                break;
            }
            else if(line.equals(CustomizedMockitoUtils.STUB_CREATION_INFO_START)){
                readStubCreation(brTrace, lineCount, testAnalysis, lineCount.getCount(), executionLocation);
                continue;
            }
            else if(line.equals(CustomizedMockitoUtils.STUB_INVOCATION_INFO_START)){
                readStubInvocation(brTrace, lineCount, testAnalysis, lineCount.getCount(), executionLocation);
                continue;
            }
            else {
                throw new RuntimeException("this line should not be inside test_before_execution"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
            }
        }
        if(!completed){
            throw new RuntimeException("could not suitably read test_before_execution"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
    }

    private static void readConstructorExecution(BufferedReader brTrace, Counter lineCount, TestAnalysis testAnalysis) throws Exception {
        String line=null;
        line = brTrace.readLine();
        lineCount.increment();
        //test class name
        if(line==null || !line.startsWith(CustomizedMockitoUtils.TEST_CONSTRUCTOR_CLASS)){
            throw new RuntimeException("expected test_constructor_class but got"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
        String constructorClassName=line.split(":")[1];//do something with this if needed
        //test method name
        line = brTrace.readLine();
        lineCount.increment();
        if(line==null || !line.startsWith(CustomizedMockitoUtils.TEST_CONSTRUCTOR_NAME)){
            throw new RuntimeException("expected test_constructor_name but got "+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
        String constructorMethodName=line.split(":")[1];//do something with this if needed
        ExecutionLocation executionLocation = new ExecutionLocation(ExecutionLocationType.CONSTRUCTOR, constructorClassName, constructorMethodName);
        boolean completed = false;
        while((line=brTrace.readLine())!=null){
            lineCount.increment();
            if(line.equals(CustomizedMockitoUtils.TEST_CONSTRUCTOR_EXECUTION_END)){
                completed=true;
                break;
            }
            else if(line.equals(CustomizedMockitoUtils.STUB_CREATION_INFO_START)){
                readStubCreation(brTrace, lineCount, testAnalysis, lineCount.getCount(), executionLocation);
                continue;
            }
            else if(line.equals(CustomizedMockitoUtils.STUB_INVOCATION_INFO_START)){
                readStubInvocation(brTrace, lineCount, testAnalysis, lineCount.getCount(), executionLocation);
                continue;
            }
            else {
                throw new RuntimeException("this line should not be inside test_constructor_execution"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
            }
        }
        if(!completed){
            throw new RuntimeException("could not suitably read test_constructor_execution"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
    }

    private static void readMethodExecution(BufferedReader brTrace, Counter lineCount, TestAnalysis testAnalysis) throws Exception {
        String line=null;
        line = brTrace.readLine();
        lineCount.increment();
        //test class name
        if(line==null || !line.startsWith(CustomizedMockitoUtils.TEST_METHOD_CLASS)){
            throw new RuntimeException("expected test_method_class but got"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
        String testClassName=line.split(":")[1];
        if(!testClassName.equals(testAnalysis.getTestClassName())){
            throw new RuntimeException("got different test class name (expected="+testAnalysis.getTestClassName()+")"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
        //test method name
        line = brTrace.readLine();
        lineCount.increment();
        if(line==null || !line.startsWith(CustomizedMockitoUtils.TEST_METHOD_NAME)){
            throw new RuntimeException("expected test_method_name but got "+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
        String testMethodName=line.split(":")[1];
        if(!testMethodName.equals(testAnalysis.getTestMethodName())){
            throw new RuntimeException("got different test method name (expected="+testAnalysis.getTestMethodName()+")"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
        ExecutionLocation executionLocation = new ExecutionLocation(ExecutionLocationType.TEST_METHOD, testClassName, testMethodName);
        //
        boolean completed = false;
        while((line=brTrace.readLine())!=null){
            lineCount.increment();
            if(line.equals(CustomizedMockitoUtils.TEST_METHOD_EXECUTION_END)){
                completed=true;
                break;
            }
            else if(line.equals(CustomizedMockitoUtils.STUB_CREATION_INFO_START)){
                readStubCreation(brTrace, lineCount, testAnalysis, lineCount.getCount(), executionLocation);
                continue;
            }
            else if(line.equals(CustomizedMockitoUtils.STUB_INVOCATION_INFO_START)){
                readStubInvocation(brTrace, lineCount, testAnalysis, lineCount.getCount(), executionLocation);
                continue;
            }
            else {
                throw new RuntimeException("this line should not be inside test_method_execution"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
            }
        }
        if(!completed){
            throw new RuntimeException("could not suitably read test_method_execution"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
    }


    private static void readAfterExecution(BufferedReader brTrace, Counter lineCount, TestAnalysis testAnalysis) throws Exception {
        String line=null;
        line = brTrace.readLine();
        lineCount.increment();
        //test class name
        if(line==null || !line.startsWith(CustomizedMockitoUtils.TEST_AFTER_CLASS)){
            throw new RuntimeException("expected test_after_class but got"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
        String afterClassName=line.split(":")[1];//do something with this if needed
        //test method name
        line = brTrace.readLine();
        lineCount.increment();
        if(line==null || !line.startsWith(CustomizedMockitoUtils.TEST_AFTER_NAME)){
            throw new RuntimeException("expected test_after_name but got "+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
        String afterMethodName=line.split(":")[1];//do something with this if needed
        ExecutionLocation executionLocation = new ExecutionLocation(ExecutionLocationType.AFTER_METHOD, afterClassName, afterMethodName);
        boolean completed = false;
        while((line=brTrace.readLine())!=null){
            lineCount.increment();
            if(line.equals(CustomizedMockitoUtils.TEST_AFTER_EXECUTION_END)){
                completed=true;
                break;
            }
            else if(line.equals(CustomizedMockitoUtils.STUB_CREATION_INFO_START)){
                readStubCreation(brTrace, lineCount, testAnalysis, lineCount.getCount(), executionLocation);
                continue;
            }
            else if(line.equals(CustomizedMockitoUtils.STUB_INVOCATION_INFO_START)){
                readStubInvocation(brTrace, lineCount, testAnalysis, lineCount.getCount(), executionLocation);
                continue;
            }
            else {
                throw new RuntimeException("this line should not be inside test_after_execution"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
            }
        }
        if(!completed){
            throw new RuntimeException("could not suitably read test_after_execution"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
        }
    }

    public static List<TestAnalysis> parseCustomizedMockitoTrace(String traceFileName) throws Exception {
        List<TestAnalysis> testAnalysisList = new ArrayList<TestAnalysis>();
        BufferedReader brTrace = new BufferedReader(new FileReader(traceFileName));
        Counter lineCount = new Counter();
        String line = null;
        TestAnalysis testAnalysisForConstructor = null;
        while((line=brTrace.readLine())!=null){
            lineCount.increment();
            if(line.equals(CustomizedMockitoUtils.TEST_CONSTRUCTOR_EXECUTION_START)){
                if(testAnalysisForConstructor!=null){
                    throw new RuntimeException("got two constructors in a row"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
                }
                testAnalysisForConstructor = new TestAnalysis();
                readConstructorExecution(brTrace, lineCount, testAnalysisForConstructor);
                continue;
            }
            else if(line.equals(CustomizedMockitoUtils.TEST_ANALYSIS_START)){
                TestAnalysis testAnalysis = new TestAnalysis();
                if(testAnalysisForConstructor!=null){
                    testAnalysis.getStubCreations().addAll(testAnalysisForConstructor.getStubCreations());
                    testAnalysis.getStubInvocations().addAll(testAnalysisForConstructor.getStubInvocations());
                    testAnalysis.getUnusedStubs().addAll(testAnalysisForConstructor.getUnusedStubs());
                    testAnalysisForConstructor=null;
                }
                //test_analysis_method_class
                line = brTrace.readLine();
                lineCount.increment();
                if(line==null || !line.startsWith(CustomizedMockitoUtils.TEST_ANALYSIS_METHOD_CLASS)){
                    throw new RuntimeException("expected test_analysis_method_class but got"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
                }
                String testClassName=line.split(":")[1];
                testAnalysis.setTestClassName(testClassName);
                //test_analysis_method_name
                line = brTrace.readLine();
                lineCount.increment();
                if(line==null || !line.startsWith(CustomizedMockitoUtils.TEST_ANALYSIS_METHOD_NAME)){
                    throw new RuntimeException("expected test_analysis_method_name but got "+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
                }
                String testMethodName=line.split(":")[1];
                testAnalysis.setTestMethodName(testMethodName);
                //
                while((line=brTrace.readLine())!=null){
                    lineCount.increment();
                    if(line.equals(CustomizedMockitoUtils.TEST_ANALYSIS_END)){
                        testAnalysisList.add(testAnalysis);
                        break;
                    }
                    else if(line.equals(CustomizedMockitoUtils.TEST_BEFORE_EXECUTION_START)){
                        readBeforeExecution(brTrace, lineCount, testAnalysis);
                        continue;
                    }
                    else if(line.equals(CustomizedMockitoUtils.TEST_METHOD_EXECUTION_START)){
                        readMethodExecution(brTrace, lineCount, testAnalysis);
                        continue;
                    }
                    else if(line.equals(CustomizedMockitoUtils.TEST_AFTER_EXECUTION_START)){
                        readAfterExecution(brTrace, lineCount, testAnalysis);
                        continue;
                    }
                    else if(line.equals(CustomizedMockitoUtils.UNUSED_STUB_INFO_START)){
                        readUnusedStub(brTrace, lineCount, testAnalysis, lineCount.getCount());
                        continue;
                    }
                    else if(line.equals(CustomizedMockitoUtils.MISMATCHED_STUB_INFO_START)){
//                        System.out.println(line);
//                        readUnusedStub(brTrace, lineCount, testAnalysis, lineCount.getCount());
                        continue;
                    }
                    else if(line.equals(CustomizedMockitoUtils.MISMATCHED_STUB_INFO_END)){
//                        System.out.println(line);
//                        readUnusedStub(brTrace, lineCount, testAnalysis, lineCount.getCount());
                        continue;
                    }
                    else {
//                        throw new RuntimeException("this line should not be inside test_analysis_start"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
                    }
                }
            }
            else {
                //trace that contains something after the analysis because we cannot capture the start of the test, investigate
                throw new RuntimeException("something is wrong because I got something not wrapped by a test_analysis_start"+"#"+lineCount.getCount()+"#"+(line==null?"null":line));
            }
        }
        brTrace.close();
        stubbingIdentifierToStackMap.clear();
        return testAnalysisList;
    }

    private static String processClassName(String className){
        String result = className;
        if(result.startsWith("interface ")){
            result = result.replace("interface ", "");
        }
        return result;
    }
}
