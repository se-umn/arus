package edu.umn.cs.analysis;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import edu.umn.cs.analysis.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class SolutionC {

    public static Set<String> fixUnusedStubs(List<TestAnalysis> testAnalyses, Set<String> javaFileNamesInTestsFolder, AnalysisStatistics analysisStatistics, boolean without_setup) throws IOException {
        FixUnusedStubsState  fixUnusedStubsState = new FixUnusedStubsState();
        fixUnusedStubsState.setSolutionOption(3);
        int insertIfStatementsCount = 0;
        //////get a list of change information
        List<ChangeInfo> ChangeInfoList = SolutionB.identifyUnusedStubs(testAnalyses, javaFileNamesInTestsFolder, fixUnusedStubsState, analysisStatistics);


        if(fixUnusedStubsState.isusInMultipleCase()){
            SolutionA.fixUnusedStubs(testAnalyses,javaFileNamesInTestsFolder,analysisStatistics, without_setup);
            return fixUnusedStubsState.getChangedFilesName();
        }


        //////get the file path for later use of adding the umnutils package
        String filePath = "";
        for(String s: javaFileNamesInTestsFolder){
            filePath = s;
            break;
        }
        fixUnusedStubsState.setFilePath(filePath.split("test/java")[0]);

        //////////////////Group case2 information again and put name of tests which use the same stub together/////////////
        //////a new list of changeInfo
        List<ChangeInfo> newChangeInfoList = new ArrayList<>();
        //////two maps help to regroup case 2 tests information
        Map<String, ChangeInfo> case2Map = new HashMap<>();


        for(ChangeInfo ci: ChangeInfoList){
            //////put all information of the same stub type into a map
            if(ci.getType() == 2){
                String unusedStubInfo = ci.getUnusedStub().getStubbingLocation().getClassName()+"#"+ci.getUnusedStub().getStubbingLocation().getMethodName()+"#"+ci.getUnusedStub().getStubbingLocation().getLineNum();
                if(case2Map.containsKey(unusedStubInfo)){
                    case2Map.get(unusedStubInfo).getTaList().addAll(ci.getTaList());
                }
                else{
                    case2Map.put(unusedStubInfo, ci);
                }
            }
            else{
                newChangeInfoList.add(ci);
            }
        }
        //////add the changeInfo in case2Map into a new changeInfo list
        for(String key: case2Map.keySet()){
            String stubClassName = key.split("#")[0];


            //////get the list of testAnalysis which use the stubs and store the file name which needs to be added testName rule
            HashMap<String, String> case2taUsingStubs = new HashMap<>();
            for (TestAnalysis ta : testAnalyses) {
                String[] fullPath = ta.getTestClassName().split("\\.");
                String taTestClassName = fullPath[fullPath.length-1];
                if (case2Map.get(key).getTaList().contains(ta) && fixUnusedStubsState.getRelatedClassMap().get(case2Map.get(key).getUnusedStub().getStubbingLocation().getClassName()).contains(taTestClassName)) {
                    String fileName = SolutionB.getFileName(javaFileNamesInTestsFolder, ta.getTestClassName());
                    case2taUsingStubs.put(ta.getTestMethodName()+fileName, fileName);
                    if(!ta.getTestClassName().equals(stubClassName)){
                        fixUnusedStubsState.getAddTestNameRuleList().add(fileName);
                    }
                }

            }
            //////if there's no tests in case2taUsingStubs, it means the testClass may not have any test or the file is extended/used by others
            if(case2taUsingStubs.size() == 0){
                for (String file : javaFileNamesInTestsFolder)  {
                    //////get a list of file extending this testClass
                    List<String> extendFile = RemoveStubUtils.checkIfExtend(file);
                    for (TestAnalysis ta : testAnalyses) {
                        //////check if the ta is in the file, then check if this file has extensions
                        if (extendFile.size() != 0 && case2Map.get(key).getUnusedStub().getStubbingLocation().getFileName().contains(extendFile.get(0))) {
                            //////check if the extension is the file contains the unused stub and if the ta is using the unused stub
                            if (((file.contains(ta.getTestClassName().replace(".", "/")) || fixUnusedStubsState.getRelatedClassMap().get(case2Map.get(key).getUnusedStub().getStubbingLocation().getClassName()).contains(ta.getTestClassName())) && case2Map.get(key).getTaList().contains(ta))) {
                                String fileName = SolutionB.getFileName(javaFileNamesInTestsFolder, ta.getTestClassName());
                                case2taUsingStubs.put(ta.getTestMethodName()+fileName, fileName);
                                fixUnusedStubsState.setExtend(true);
//                                    fixUnusedStubsState.getAddTestNameRuleList().add(fileName);
                            }
                        }
                    }
                }
            }

//            //for the class is using the method check
//            //////if the size of case2taUsingStubs is still 0, it means the method in the testClass is used by other testClasses
//            //////so we are going to add all tests in the repo to the case2taUsingStubs
            if(case2taUsingStubs.size() == 0){
                System.out.println("CHECK CASE 2 LIST");
            }

            //////get the list of unusedStub stubbing location's line numbers
            List<Integer> case2unusedStubStubblingLocLineNums = new ArrayList<>();
            case2unusedStubStubblingLocLineNums.add(case2Map.get(key).getUnusedStub().getStubbingLocation().getLineNum());

            //////update the testAnalyses and lineNumbers for the unusedstub
            if(case2taUsingStubs.size()!= 0){
                case2Map.get(key).setTestAnalysesMap(case2taUsingStubs);
                case2Map.get(key).setLineNums(case2unusedStubStubblingLocLineNums);
            }
            else{
                System.out.println("Case2 doesn't have tests in the file");
//                System.out.println(case2Map.get(key).getUnusedStub().getStubbingLocation().getFileName() + "#" +case2Map.get(key).getUnusedStub().getStubbingLocation().getLineNum()+" "+case2Map.get(key).getUnusedStub().getStubbedMethodName());
            }

            newChangeInfoList.add(case2Map.get(key));
        }



        ////////////////////////group all stubs in the same file together//////////////////////////
        //////a map contains file name and the stub changeInfo in the file.
        Map<String, List<ChangeInfo>> fileWithStubsMap = new HashMap<>();
        for(int i = 0; i<newChangeInfoList.size(); i++){
            String key = newChangeInfoList.get(i).getSource();
            if(fileWithStubsMap.containsKey(key)){
                fileWithStubsMap.get(key).add(newChangeInfoList.get(i));
            }
            else{
                List<ChangeInfo> changeInfoList = new ArrayList<>();
                changeInfoList.add(newChangeInfoList.get(i));
                fileWithStubsMap.put(key,changeInfoList);
            }
        }
        ///////////////////collect the stubs' changeInfo all together for the later removing use////////////////////
        for(String fileName: fileWithStubsMap.keySet()) {
            //////for case 1 remove the stub
            List<Integer> case1UnusedStubsStubbingLocLineNums = new ArrayList<>();
            List<String> case1UnusedStubsStubbedMethodName = new ArrayList<>();
            List<String> case1UnusedStubsStubbingLocMethodName = new ArrayList<>();

            //////for case2 case3 and case4 insert test names
            List<List> case2UnusedStubsStubbingLocLineNumsList = new ArrayList<>();
            List<List> case2TestAnalysesList = new ArrayList<>();
            List<List> case2PostfixList = new ArrayList<>();
            List<String> case2MethodNames = new ArrayList<>();


            List<String> addTestNameList = new ArrayList<>();
            ////////for case3 so that the stackcomponents can be later transformed to Edges
            List<List> UnusedStubStackcomponents = new ArrayList<>();

//            String methodName = "";

            for (ChangeInfo info : fileWithStubsMap.get(fileName)) {
                UnusedStub us = info.getUnusedStub();

                //gather case1 information
                if (info.getType() == 1) {
                    case1UnusedStubsStubbingLocLineNums.add(us.getStubbingLocation().getLineNum());
                    case1UnusedStubsStubbedMethodName.add(us.getStubbedMethodName());
                    case1UnusedStubsStubbingLocMethodName.add(us.getStubbingLocation().getMethodName());
                }

                //gather case2,3,4 information
                else if (info.getType() == 2) {
                    boolean hasTest = RemoveStubUtils.ifContainsTest(info.getSource());
                    if(!hasTest){
//                        UnusedStub unusedStub = info.getUnusedStub();
                        for(String fn: fileWithStubsMap.keySet()){
                            String[] paths = fn.split("/");
                            if (paths[paths.length-1].equals(us.getStubbingLocation().getFileName())) {
                                if(!addTestNameList.contains(fn)){
                                    addTestNameList.add(fn);
//                                    System.out.println("TEST FILE NAME: "+ fn);
                                    int size = us.getStubbingLocationStack().size();
//                                    System.out.println(us.getStubbingLocationStack().get(size-1).getMethodName());
                                }

                            }
                        }
                    }

                    List<String> postfixList = new ArrayList<>();
                    for(String key: info.getTestAnalysesMap().keySet()){
                        postfixList.add(info.getTestAnalysesMap().get(key));
                    }
                    case2PostfixList.add(postfixList);
                    case2TestAnalysesList.add(Arrays.asList(info.getTestAnalysesMap().keySet().toArray()));
                    case2UnusedStubsStubbingLocLineNumsList.add(info.getLineNums());
                    case2MethodNames.add(info.getMethodName());
                }
//                }
                //////gather case3 stackComponent
                else if(info.getType() == 3){
                    UnusedStubStackcomponents.add(info.getUnusedStub().getStubbingLocationStack());
                }
            }

            ////////parse the target file to remove the unused stubs
            CompilationUnit cu = StaticJavaParser.parse(new File(fileName));


            //////convert case3's stackComponent to Edge
            if(UnusedStubStackcomponents.size()!= 0){
                List<Edge> edges = new ArrayList<>();
                for(List<StackComponent> l: UnusedStubStackcomponents){
                    if(l.size()<4){
                        for(int i = l.size()-1; i >= 0; i--){
                            //////the first component does not have the previous one
                            if(i == l.size()-1){
                                //////if the stub only has one stackComponent set hasNext to false;
                                if(i == 1){
                                    Edge edge = new Edge(null, l.get(i), l.get(i-1), false);
                                    edges.add(edge);
                                }
                                else{
                                    Edge edge = new Edge(null, l.get(i), l.get(i-1),true);
                                    edges.add(edge);
                                }
                            }
                            //////if the stub has more than 2 stackComponents, set the middle ones' hasNext to true;
                            else if(i > 1 && i<l.size()-1){
                                Edge edge = new Edge(l.get(i+1), l.get(i), l.get(i-1), true);
                                edges.add(edge);
                            }
                            //////for the last component, it does not have the next one;
                            else if(i == 1){
                                Edge edge = new Edge(l.get(i+1), l.get(i), l.get(i-1), false);
                                edges.add(edge);
                            }
                        }

                    }
                }

                //////put the edge into a graph
                Graph graph = new Graph(edges);
                //////print the graph
//                Graph.printGraph(graph);
                //////solve case3
                cu = Graph.analyzeGraph(graph, cu, fileName, javaFileNamesInTestsFolder,fixUnusedStubsState);
            }

            //////solve case1
            if (case1UnusedStubsStubbingLocLineNums.size()!= 0){
//                System.out.println("case1Solution: "+fileName + " " + case1UnusedStubsStubbingLocLineNums + " " + case1UnusedStubsStubbedMethodName+ " " + case1UnusedStubsStubbingLocMethodName);
                cu = RemoveStubUtils.removeStub(cu,case1UnusedStubsStubbingLocLineNums,case1UnusedStubsStubbedMethodName,case1UnusedStubsStubbingLocMethodName);
            }

            if (case2TestAnalysesList.size() != 0) {
                if(RemoveStubUtils.isStatic(cu,case2MethodNames)){
                    for(String fn: javaFileNamesInTestsFolder){
                        if(RemoveStubUtils.isUseOutside(fn,case2MethodNames)){
                            fixUnusedStubsState.setOutside(true);
//                            System.out.println("ADDTESTNAME1: "+fn);
                            fixUnusedStubsState.getAddTestNameRuleList().add(fn);
                        }
                    }
                }
                boolean containsTest = RemoveStubUtils.ifContainsTest(fileName);
                boolean finalBoolean = false;
                if(containsTest &&  fixUnusedStubsState.getAddTestNameRuleList().contains(fileName)){
                    finalBoolean = true;
                }
//                System.out.println("case2 NEGEATE Solution: "+ fileName + case2TestAnalysesList + " " + case2UnusedStubsStubbingLocLineNumsList + " " + case2MethodNames + " " + finalBoolean + case2PostfixList);
                cu = RemoveStubUtils.insertIfStatementWithNegateTestCases(cu, case2TestAnalysesList, case2UnusedStubsStubbingLocLineNumsList, case2MethodNames,finalBoolean, case2PostfixList,fileName,fixUnusedStubsState);
                insertIfStatementsCount ++;
            }

            //////export the file
            Files.write(new File(fileName).toPath(), Collections.singleton(cu.toString()), StandardCharsets.UTF_8);
            fixUnusedStubsState.getChangedFilesName().add(fileName.replace("_experiment", ""));

            //////if solve case 2/3/4, we need to add the testName rule
            if(case2TestAnalysesList.size() != 0){
                boolean containsTest = RemoveStubUtils.ifContainsTest(fileName);
//                System.out.println("addTestName " + fileName + " " + containsTest);
                RuleUtil.addTestNameRules(fileName, containsTest);
                if(fixUnusedStubsState.getAddTestNameRuleList().contains(fileName)){
                    outerloop:
                    for(String key: fixUnusedStubsState.getRelatedClassMap().keySet()){
                        Set<String> relatedClassesSet = fixUnusedStubsState.getRelatedClassMap().get(key);
                        for(String s: relatedClassesSet){
                            if(fileName.contains(s.replace("\\.", "/"))){
//                                System.out.println("Insert TestNameUtil in @Before " + fileName);
                                CompilationUnit compilationUnit = StaticJavaParser.parse(new File(fileName));
                                compilationUnit = RemoveStubUtils.insertTestName(compilationUnit,fileName);
                                Files.write(new File(fileName).toPath(), Collections.singleton(compilationUnit.toString()), StandardCharsets.UTF_8);
                                fixUnusedStubsState.getChangedFilesName().add(fileName.replace("_experiment", ""));
                                fixUnusedStubsState.setNeedAddPackage(true);
                                break outerloop;
                            }
                        }
                    }
                    fixUnusedStubsState.getAddTestNameRuleList().remove(fileName);
                }
            }
        }
        //////if we are adding the whole repo's test name, we also need to add testName rule for the others
        if(fixUnusedStubsState.getAddTestNameRuleList().size() != 0){
            for(String fn: fixUnusedStubsState.getAddTestNameRuleList()){
                if(!fn.contains("repos_tmp/jenkinsci_repository-connector-plugin_experiment/src/test/java/org/jvnet/hudson/plugins/repositoryconnector/VersionParameterDefinitionTest.java")){
//                    System.out.println("ADD MORE TEST NAMES: " + fn+ " " + true);
                    RuleUtil.addTestNameRules(fn, true);
                    CompilationUnit cu = StaticJavaParser.parse(new File(fn));
                    cu = RemoveStubUtils.insertTestName(cu,fn);
                    Files.write(new File(fn).toPath(), Collections.singleton(cu.toString()), StandardCharsets.UTF_8);
                    fixUnusedStubsState.getChangedFilesName().add(fn.replace("_experiment", ""));
                }

            }
        }
        //////add imports if in case5, method names to be changed and the method is duplicated are not in the same class
        if(fixUnusedStubsState.getImportFileName().size()!= 0){
            for(int i = 0; i< fixUnusedStubsState.getImportFileName().size(); i++){
                CompilationUnit cu = StaticJavaParser.parse(new File(fixUnusedStubsState.getImportFileName().get(i)));
                cu.addImport(fixUnusedStubsState.getImportName().get(i), fixUnusedStubsState.getImportIsStatic().get(i), false);
                Files.write(new File(fixUnusedStubsState.getImportFileName().get(i)).toPath(), Collections.singleton(cu.toString()), StandardCharsets.UTF_8);
                fixUnusedStubsState.getChangedFilesName().add(fixUnusedStubsState.getImportFileName().get(i).replace("_experiment", ""));
            }

        }

        ///////if we need to add package or the stub is used outside of the class, add the umnutils package
        if(fixUnusedStubsState.isNeedAddPackage() || fixUnusedStubsState.isOutside() || fixUnusedStubsState.getAddTestNameRuleList().size() != 0){
//            System.out.println("need add umnutil package "+ fixUnusedStubsState.getFilePath()+"test/java/umnutils");
            RemoveStubUtils.createPackage(fixUnusedStubsState.getFilePath()+"/test/java/umnutils");
        }
        if(insertIfStatementsCount>0){
            analysisStatistics.setInsertIfStatementsCount(insertIfStatementsCount);
        }
        return fixUnusedStubsState.getChangedFilesName();
    }
}