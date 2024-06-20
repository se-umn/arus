package edu.umn.cs.analysis;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.umn.cs.analysis.model.StackComponent;
import edu.umn.cs.analysis.model.TestExecutionResult;

import java.util.*;

public class AnalysisUtils {
    public static String getMockitoDep(JsonArray mockitoLabels){
        String result = "";
        for(int j=0; j<mockitoLabels.size(); ++j) {
            String mockitoDep = mockitoLabels.get(j).getAsString();
            String mockitoDepItems[] = mockitoDep.split(":");
            if (mockitoDepItems[1].equals("mockito-core")) {
                result = mockitoDep;
                return result;
            }
        }
        return result;
    }

    public static String getMockitoDep(Set<String> mockitoLabels){
        String result = "";
        for(String mockitoDep:mockitoLabels) {
            String mockitoDepItems[] = mockitoDep.split(":");
            if (mockitoDepItems[1].equals("mockito-core")) {
                result = mockitoDep;
                return result;
            }
        }
        return result;
    }


    public static int getJunitMajorVersion(Set<String> junitLabels){
        int result = -1;
        boolean usesJunit4 = false;
        boolean usesJunit5 = false;
        for(String junitDep:junitLabels) {
            String junitDepItems[] = junitDep.split(":");
            if (junitDepItems[0].equals("junit") && junitDepItems[1].equals("junit") && junitDepItems[3].startsWith("4")) {
                usesJunit4 = true;
            }
            if (junitDepItems[0].equals("org.junit.jupiter")) {
                usesJunit5 = true;
            }
        }
        if (usesJunit4 && usesJunit5) {
            result = -1;
            return result;
        } else if(usesJunit4) {
            result = 4;
            return result;
        } else if (usesJunit5) {
            result = 5;
            return result;
        } else {
            result = -1;
            return result;
        }
    }

    public static int getJunitMinorVersion(Set<String> junitLabels){
        int result = -1;
        boolean usesJunit4 = false;
        boolean usesJunit5 = false;
        String versionString="";
        for(String junitDep:junitLabels) {
            String junitDepItems[] = junitDep.split(":");
            if (junitDepItems[0].equals("junit") && junitDepItems[1].equals("junit") && junitDepItems[3].startsWith("4")) {
                usesJunit4 = true;
                versionString=junitDepItems[3];
            }
            if (junitDepItems[0].equals("org.junit.jupiter")) {
                usesJunit5 = true;
                versionString=junitDepItems[3];
            }
        }
        if (usesJunit4 && usesJunit5) {
            result = -1;
            return result;
        } else if(usesJunit4) {
            String versionItems[] = versionString.split("\\.");
            if(versionItems[1].endsWith("-beta-3")){
                versionItems[1]=versionItems[1].replace("-beta-3", "");
            }
            result = Integer.parseInt(versionItems[1]);
            return result;
        } else if (usesJunit5) {
            String versionItems[] = versionString.split("\\.");
            result = Integer.parseInt(versionItems[1]);
            return result;
        } else {
            result = -1;
            return result;
        }
    }

    public static int getMajorMockitoVersion(String mockitoDep) {
        int result=0;
        if(mockitoDep.equals("")){
            return result;
        }
        String mockitoDepArray[] = mockitoDep.split(":");
        String mockitoVersionItems[] = mockitoDepArray[3].split("\\.");
        result = Integer.parseInt(mockitoVersionItems[0]);
        return result;
    }

    public static int checkTestsAllPassAndNotFlaky(JsonArray testExecutionResults){
        int passsedAndNotFlaky = 0;
        List<Integer> executionsList = new ArrayList<Integer>();
        for(int i=0; i<testExecutionResults.size(); ++i){
            JsonObject testExecutionResult = testExecutionResults.get(i).getAsJsonObject();
            boolean mavenSuccess = testExecutionResult.get("maven_success").getAsBoolean();
            int executions = Integer.parseInt(testExecutionResult.get("executions").getAsString());
            int failed = Integer.parseInt(testExecutionResult.get("failed").getAsString());
            int errors = Integer.parseInt(testExecutionResult.get("errors").getAsString());
            if(mavenSuccess && executions>0 && failed==0 && errors==0){
                executionsList.add(Integer.valueOf(executions));
            } else {
                passsedAndNotFlaky = 0;
                return passsedAndNotFlaky;
            }
        }
        if(executionsList.size()==0){
            passsedAndNotFlaky = 0;
            return passsedAndNotFlaky;
        }
        int firstRunExecutions = executionsList.get(0).intValue();
        for(Integer executions:executionsList){
            if (firstRunExecutions!=executions.intValue()) {
                passsedAndNotFlaky = 0;
                return passsedAndNotFlaky;
            }
        }
        passsedAndNotFlaky = firstRunExecutions;
        return passsedAndNotFlaky;
    }

    public static boolean hasTests(JsonArray testExecutionResults){
        boolean result = false;
        for(int i=0; i<testExecutionResults.size(); ++i){
            JsonObject testExecutionResult = testExecutionResults.get(i).getAsJsonObject();
            int executions = Integer.parseInt(testExecutionResult.get("executions").getAsString());
            int failed = Integer.parseInt(testExecutionResult.get("failed").getAsString());
            int errors = Integer.parseInt(testExecutionResult.get("errors").getAsString());
            if(executions>0 || failed>0 || errors>0){
                result = true;
                return result;
            }
        }
        return result;
    }

    public static Map<Set, List<StackComponent>> reverseMap(Map<StackComponent, List> map) {
        Map<Set, List<StackComponent>> reverseMap = new HashMap<>();
        for (StackComponent s : map.keySet()) {
            Set key = new HashSet<>();
            for (Object sct : map.get(s)) {
                key.add(sct);
            }

            if (reverseMap.containsKey(key)) {
                reverseMap.get(key).add(s);
            } else {

                List<StackComponent> usList = new ArrayList<>();
                usList.add(s);
                reverseMap.put(key, usList);

            }
        }
        return reverseMap;
    }

    public static int checkConsistencyOfExecutionResults(List<TestExecutionResult> testExecutionResults){
        int result = -1;
        TestExecutionResult firstTestExecutionResult = testExecutionResults.get(0);
        int executions = firstTestExecutionResult.getExecutions();
        int failed = firstTestExecutionResult.getFailed();
        int errors = firstTestExecutionResult.getErrors();
        int skipped = firstTestExecutionResult.getSkipped();
        for(TestExecutionResult testExecutionResult:testExecutionResults){
            if(executions!=testExecutionResult.getExecutions()){
                return result;
            }
            if(failed!=testExecutionResult.getFailed()){
                return result;
            }
            if(errors!=testExecutionResult.getErrors()){
                return result;
            }
            if(skipped!=testExecutionResult.getSkipped()){
                return result;
            }
        }
        result = executions - failed - errors - skipped;
        return result;
    }
}
