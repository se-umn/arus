package edu.umn.cs.analysis;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.umn.cs.analysis.model.*;
import org.checkerframework.framework.qual.Unused;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class SolutionB {

    public static Set<String> fixUnusedStubs(List<TestAnalysis> testAnalyses, Set<String> javaFileNamesInTestsFolder, AnalysisStatistics analysisStatistics, boolean remove_uus) throws IOException {
        FixUnusedStubsState  fixUnusedStubsState = new FixUnusedStubsState();
        fixUnusedStubsState.setSolutionOption(2);
        int insertIfStatementsCount = 0;


        //////get a list of change information
        List<ChangeInfo> ChangeInfoList = SolutionB.identifyUnusedStubs(testAnalyses, javaFileNamesInTestsFolder, fixUnusedStubsState, analysisStatistics);

        if(fixUnusedStubsState.isusInMultipleCase()){
            SolutionA.fixUnusedStubs(testAnalyses,javaFileNamesInTestsFolder,analysisStatistics,remove_uus);
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
                if (!case2Map.get(key).getTaList().contains(ta) && fixUnusedStubsState.getRelatedClassMap().get(case2Map.get(key).getUnusedStub().getStubbingLocation().getClassName()).contains(taTestClassName)) {
                    String fileName = getFileName(javaFileNamesInTestsFolder, ta.getTestClassName());
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
                            if (((file.contains(ta.getTestClassName().replace(".", "/")) || fixUnusedStubsState.getRelatedClassMap().get(case2Map.get(key).getUnusedStub().getStubbingLocation().getClassName()).contains(ta.getTestClassName())) && !case2Map.get(key).getTaList().contains(ta))) {
                                String fileName = getFileName(javaFileNamesInTestsFolder, ta.getTestClassName());
                                case2taUsingStubs.put(ta.getTestMethodName()+fileName, fileName);
                                fixUnusedStubsState.setExtend(true);
                            }
                        }
                    }
                }
            }

            //for the class is using the method check
            //////if the size of case2taUsingStubs is still 0, it means the method in the testClass is used by other testClasses
            //////so we are going to add all tests in the repo to the case2taUsingStubs
            if(case2taUsingStubs.size() == 0){
//                System.out.println("CASE2: Adding tests in the whole repo");
                /////////store the file contains test into the addTestNameRule List
                for(String fn: javaFileNamesInTestsFolder){
                    boolean containsTest = RemoveStubUtils.ifContainsTest(fn);
                    if(containsTest){
                        fixUnusedStubsState.getAddTestNameRuleList().add(fn);
                    }
                }
                //////////get the testAnalyses use the stub
                for (TestAnalysis ta : testAnalyses) {
                    if (!case2Map.get(key).getTaList().contains(ta)) {
                        String fileName = getFileName(javaFileNamesInTestsFolder, ta.getTestClassName());
                        case2taUsingStubs.put(ta.getTestMethodName()+fileName, fileName);
                    }
                }
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
//                System.out.println("Case2 doesn't have tests in the file");
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

            //////for case2 insert test names
            List<List> case2UnusedStubsStubbingLocLineNumsList = new ArrayList<>();
            List<List> case2TestAnalysesList = new ArrayList<>();
            List<List> case2PostfixList = new ArrayList<>();
            List<String> case2MethodNames = new ArrayList<>();


            List<String> addTestNameList = new ArrayList<>();
            ////////for case3 so that the stackcomponents can be later transformed to Edges
            Set<List> UnusedStubStackcomponents = new HashSet<>();


            for (ChangeInfo info : fileWithStubsMap.get(fileName)) {
                UnusedStub us = info.getUnusedStub();

                //gather case1 information
                if (info.getType() == 1) {
                    case1UnusedStubsStubbingLocLineNums.add(us.getStubbingLocation().getLineNum());
                    case1UnusedStubsStubbedMethodName.add(us.getStubbedMethodName());
                    case1UnusedStubsStubbingLocMethodName.add(us.getStubbingLocation().getMethodName());
                }

                //gather case2 information
                else if (info.getType() == 2) {
                    boolean hasTest = RemoveStubUtils.ifContainsTest(info.getSource());
                    if(!hasTest){
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
                    else{
//                        System.out.println("-----------------");
//                        for(StackComponent sct: l){
//                            System.out.println(sct.getClassName()+" "+sct.getMethodName()+ " "+ sct.getLineInvokedInMethod());
//
//                        }
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
                            fixUnusedStubsState.getAddTestNameRuleList().add(fn);
                        }
                    }
                }
                boolean containsTest = RemoveStubUtils.ifContainsTest(fileName);
                boolean finalBoolean = false;
                if(containsTest &&  fixUnusedStubsState.getAddTestNameRuleList().contains(fileName)){
                    finalBoolean = true;
                }
//                System.out.println("case2 Solution: "+ fileName + case2TestAnalysesList + " " + case2UnusedStubsStubbingLocLineNumsList + " " + case2MethodNames + " " + finalBoolean);
                cu = RemoveStubUtils.insertIfStatement(cu, case2TestAnalysesList, case2UnusedStubsStubbingLocLineNumsList, case2MethodNames,finalBoolean, case2PostfixList,fileName,fixUnusedStubsState);
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
        if(fixUnusedStubsState.getAddTestNameRuleList().size() != 0 ){
            for(String fn: fixUnusedStubsState.getAddTestNameRuleList()){
                RuleUtil.addTestNameRules(fn, true);
                CompilationUnit cu = StaticJavaParser.parse(new File(fn));
                cu = RemoveStubUtils.insertTestName(cu,fn);
                Files.write(new File(fn).toPath(), Collections.singleton(cu.toString()), StandardCharsets.UTF_8);
                fixUnusedStubsState.getChangedFilesName().add(fn.replace("_experiment", ""));
            }
        }
        //////add imports if method names to be changed and the method is duplicated are not in the same class
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

    public static List<ChangeInfo> identifyUnusedStubs(List<TestAnalysis> transformTestAnalyses, Set<String> javaFileNamesInTestsFolder, FixUnusedStubsState fixUnusedStubsState, AnalysisStatistics analysisStatistics) throws IOException {

        Map<UnusedStub, List<TestAnalysis>> unusedStubMap = new HashMap<>();
        Set<String> usDefinitions = new HashSet<>();



        //////get all unusedStubs from test
        for (TestAnalysis ta : transformTestAnalyses) {
            //get all unusedStubs from test
            for (UnusedStub unusedStub : ta.getUnusedStubs()) {
                usDefinitions.add(unusedStub.getInfo());

                if(unusedStubMap.containsKey(unusedStub)){
                    unusedStubMap.get(unusedStub).add(ta);
                }
                else{
                    List<TestAnalysis> innerList = new ArrayList<>();
                    innerList.add(ta);
                    unusedStubMap.put(unusedStub,innerList);
                }
            }
        }
        analysisStatistics.setUSDB(usDefinitions.size());
        //////put the unusedStubs and the corresponding testAnalysis into a map
        //////key is the string which contains the information of the unusedStub, value is a inner map
        //////the inner map is used for tracking how many times the stub is appeared in the testAnalysis
        Map<String, Map<TestAnalysis, Integer>> unusedStubInfoMap = new HashMap<>();

        for (TestAnalysis ta : transformTestAnalyses) {
            for (UnusedStub unusedStub : ta.getUnusedStubs()) {
                String key = unusedStub.getInfo();
                if (!unusedStubInfoMap.containsKey(key)) {
                    unusedStubInfoMap.put(key, new HashMap<>());
                }
                Map<TestAnalysis, Integer> innerMap = unusedStubInfoMap.get(key);
                innerMap.put(ta, innerMap.getOrDefault(ta, 0) + 1);
            }
        }


        List<UnusedStub> unusedStubList = new ArrayList<>();

        //////putting the unusedstub into unusedStubList and its corresponding testAnalyses into TestAnalysisList.
        for(UnusedStub key: unusedStubMap.keySet()){
            boolean isTheSameStub = true;
            String keyString = key.getInfo();
            List<TestAnalysis> testAnalyses = new ArrayList<>();
            for(TestAnalysis ta: unusedStubInfoMap.get(keyString).keySet()){
                if(unusedStubInfoMap.get(keyString).get(ta)>1){
                    isTheSameStub = false;
                    break;
                }
            }
            if(isTheSameStub){
                testAnalyses.addAll(unusedStubInfoMap.get(keyString).keySet());
                key.setTestAnalysisList(testAnalyses);
            }
            else{
                key.setTestAnalysisList(unusedStubMap.get(key));
            }
            unusedStubList.add(key);
        }

        //////the list of ChangeInfo to be returned
        List<ChangeInfo> infos = new ArrayList<>();
        Set<String> case3set = new HashSet<>();

        /////////////////////////loop through the unusedStubList, identify each stub's type and covert stub to changeInfo//////////////////////
        for(int i =0; i<unusedStubList.size();i++){

            UnusedStub us = unusedStubList.get(i);
            List<TestAnalysis> testAnalysisList = us.testAnalysisList;

            ///////three booleans used for identify case1
            boolean usingElsewhere = false;
            boolean usingEverywhere = false;

            //////a set consists of all testAnalyses in the TestClass which exists the stub
            Set<TestAnalysis> taInOneFile = new HashSet<>();
            StubInvocation si_tmp = null;

            for (TestAnalysis ta : transformTestAnalyses) {
                /////get all testAnalysese
                if((ta.getTestClassName()+".java").contains(us.getStubbingLocation().getFileName())){
                    taInOneFile.add(ta);
                }
                //////get all stubInvocations in each testAnalysis to see if the stub is used in somewhere else

                List<StubInvocation> stubInvocationsList = ta.getStubInvocations();
                for (StubInvocation si : stubInvocationsList) {
                    if (si.getInfo().equals(us.getInfo())) {
                        usingElsewhere = true;
                        si_tmp=si;
                    }
                }
            }

            String unusedStubInfo = us.getInfo();

            boolean appearOnce = true;
            int methodContaingStubIsCalledCount = 0;
            boolean ifInLoop = false;

            ///////Only when the number of testAnalysis in the testClass equals to the number of testAnalysis in the testClass contains the stub (means the stub is in every test and the stub,
            // the stub only appears in the testAnalysis once
            /////// and the method contains the stub is only called once, the unused stub then can be considered using in every test, and can be removed safely
            if(taInOneFile.size() == unusedStubInfoMap.get(unusedStubInfo).size()){
                for(TestAnalysis ta: unusedStubInfoMap.get(unusedStubInfo).keySet()){
                    if(unusedStubInfoMap.get(unusedStubInfo).get(ta) != 1){
                        appearOnce = false;
                        break;
                    }
                }
                for (String testFileName : javaFileNamesInTestsFolder) {
                    String[] paths = testFileName.split("/");
                    if (paths[paths.length-1].equals(us.getStubbingLocation().getFileName())) {
                        List<StackComponent> sctList = us.getStubbingLocationStack();
                        if(sctList.size()>=2){
                            methodContaingStubIsCalledCount = RemoveStubUtils.ifMoreThanOnce(testFileName, sctList.get(1).getMethodName() , us.getStubbingLocation().getMethodName());
                        }

                    }
                }

                if(appearOnce && methodContaingStubIsCalledCount <= 1){
                    usingEverywhere = true;
                }
            }
            ///////check if the unusedStub is used in a loop
            for (String testFileName : javaFileNamesInTestsFolder) {
                String[] paths = testFileName.split("/");
                if (paths[paths.length-1].equals(us.getStubbingLocation().getFileName())) {
                    ifInLoop = RemoveStubUtils.ifInLoop(testFileName, us.getStubbingLocation().getLineNum(), us.getStubbingLocation().getMethodName());
                    break;
                }
            }

            //////////////////case 1 identification//////////////////////////
            if (!usingElsewhere || usingEverywhere) {
                //////if the unusedStub is not in loop collect the information
                if(!ifInLoop || !usingElsewhere){
                    for (String testFileName : javaFileNamesInTestsFolder) {
                        String[] paths = testFileName.split("/");
                        if (paths[paths.length-1].equals(us.getStubbingLocation().getFileName())) {
                            ChangeInfo case1Info = new ChangeInfo(testFileName, us.getStubbingLocation().getLineNum(), us,1);
                            infos.add(case1Info);
                        }
                    }
                }
            }
            else {
                StubCreation matchingStubCreation = null;

                //////get the stub creation of the unused stub so that we can check where it is executed
                for (TestAnalysis ta : testAnalysisList) {
                    for (StubCreation sc : ta.getStubCreations()) {
                        if (sc.getInfo().equals(us.getInfo())){
                            matchingStubCreation = sc;
                            break;
                        }
                    }
                }
                if(matchingStubCreation !=null){
                    ///case of parameterzied test
                    if(us.getStubbingLocation().getMethodName().equals(matchingStubCreation.getExecutionLocation().getMethodName())&&matchingStubCreation.getExecutionLocation().getType().equals(ExecutionLocationType.TEST_METHOD)) {
                        System.out.println("may happen because of Parameterized tests");
                        for (String testFilename : javaFileNamesInTestsFolder) {
                            String[] path = testFilename.split("/");
                            if (path[path.length-1].equals(us.getStubbingLocation().getFileName())) {
                                ChangeInfo case4Info = new ChangeInfo(testFilename, us.getStubbingLocation().getLineNum(), us, 4);
                                infos.add(case4Info);
                            }
                        }
                    }
                    else{
                        List<TestAnalysis> case2List = new ArrayList<>();
                        for (TestAnalysis ta : testAnalysisList) {
                            ///////a count to count us invocations
                            int invokedCount = IfStubIsInvoked(ta, us);

                            //count the number of stub creation
                            int SCcount = 0;
                            for(StubCreation sc: ta.getStubCreations()){
                                if (sc.getStack().containsAll(us.getStubbingLocationStack())){
                                    SCcount++;
                                }
                            }

                            //////if there's no si in the methods, it means the stub is not invoked at all, so it is a case2 test which will later be used in the if contions
                            if (invokedCount == 0 && !ifInLoop) {
                                case2List.add(ta);
                            }
                            //////otherwise it means the testAnalysis invokes some stubs and we have to check if the stub is inovked
                            else {
                                //////if the stub is only created once or the stub is created as much as the number of the method which contains stubs is called, it is case3

                                if(SCcount <= 1){
                                    for (String testFilename : javaFileNamesInTestsFolder) {
                                        String[] path = testFilename.split("/");
                                        if (path[path.length-1].equals(us.getStubbingLocation().getFileName())) {
//                                            System.out.println("1case3: " + us.getStubbingLocation().getClassName()+"/"+us.getStubbingLocation().getMethodName()+"/"+us.getStubbingLocation().getLineNum());
                                            ChangeInfo case3Info = new ChangeInfo(testFilename, us.getStubbingLocation().getLineNum(), us,3);
                                            infos.add(case3Info);
                                            case3set.add(us.getInfo());
                                        }
                                    }
                                }
                                //////it is in complicated loop
                                else{
                                    for (String testFilename : javaFileNamesInTestsFolder) {
                                        String[] path = testFilename.split("/");
                                        if (path[path.length-1].equals(us.getStubbingLocation().getFileName())) {
                                            ChangeInfo case4Info = new ChangeInfo(testFilename, us.getStubbingLocation().getLineNum(), us, 4);
                                            infos.add(case4Info);
                                        }
                                    }
                                }
                            }
                        }
                        if (!case2List.isEmpty()) {
                            String testFileName = getMatchingFileName(javaFileNamesInTestsFolder, us);
                            //get related class map which can be later used to generate the if statement condition
                            getRelatedClassMap(javaFileNamesInTestsFolder, us, fixUnusedStubsState);
                            if(!testFileName.equals("")){
//                                System.out.println("1case2: " + us.getStubbingLocation().getClassName()+"/"+us.getStubbingLocation().getMethodName()+"/"+us.getStubbingLocation().getLineNum());
                                ChangeInfo case2Info = new ChangeInfo(testFileName, case2List, us, us.getStubbingLocation().getMethodName(), 2, si_tmp);
                                infos.add(case2Info);
                            }
                        }
                    }
                }
            }
        }
        Set<ChangeInfo> bothCase3case2 = new HashSet<>();
        List<ChangeInfo> transformedCase3 = new ArrayList<>();
        for(ChangeInfo ci: infos){
            if(case3set.contains(ci.getUnusedStub().getInfo()) && ci.getType() == 2){
                fixUnusedStubsState.setusInMultipleCase(true);
                bothCase3case2.add(ci);
                ChangeInfo case3Info = new ChangeInfo(ci.getSource(), ci.getUnusedStub().getStubbingLocation().getLineNum(), ci.getUnusedStub(),3);
                transformedCase3.add(case3Info);
            }
        }

        for(ChangeInfo ci: bothCase3case2){
            infos.remove(ci);
        }
        infos.addAll(transformedCase3);
//        ////track the number of stubbing locations in types.

        Set<String> case1 = new HashSet<>();
        Set<String> case2 = new HashSet<>();
        Set<String> case3 = new HashSet<>();
        Set<String> case2_s = new HashSet<>();
        Set<ChangeInfo> case2Set = new HashSet<>();
        for (ChangeInfo info: infos){

            if(info.getType() == 1){
                case1.add(info.getUnusedStub().getInfo());
            }
            else if(info.getType() == 2){
                if(!case2.contains(info.getUnusedStub().getInfo())){
                    case2Set.add(info);
                }

                case2.add(info.getUnusedStub().getInfo());

                String FileName = getMatchingFileName(javaFileNamesInTestsFolder, info.getUnusedStub());
                if(isBefore(info.getUnusedStub().getStubbingLocation().getMethodName(), FileName)){
                    case2_s.add(info.getUnusedStub().getInfo());
                }
            }
            else if(info.getType() == 3){
                case3.add(info.getUnusedStub().getInfo());
                String FileName = getMatchingFileName(javaFileNamesInTestsFolder, info.getUnusedStub());
                if(isBefore(info.getUnusedStub().getStubbingLocation().getMethodName(), FileName)){
                    case2_s.add(info.getUnusedStub().getInfo());
                }
            }
        }
        analysisStatistics.setUS1COUNT(case1.size());
        analysisStatistics.setUS2COUNT(case2.size());
        analysisStatistics.setUS3COUNT(case3.size());
        analysisStatistics.setUSAT_S(case2_s.size());
//        System.out.println("---------------------SetUp Count---------------------------"+case2_s.size());
//        System.out.println(case2_s);

        for(ChangeInfo info: case2Set){
            Map<String, List<TestAnalysis>> taFile = new HashMap<>();
            for(TestAnalysis ta: info.getTaList()){
                if(taFile.containsKey(ta.getTestClassName())){
                    taFile.get(ta.getTestClassName()).add(ta);
                }
                else{
                    List<TestAnalysis> tal = new ArrayList<>();
                    tal.add(ta);
                    taFile.put(ta.getTestClassName(),tal);
                }
            }
            for(String s: taFile.keySet()){
                String fn = SolutionA.getMatchingFileName(javaFileNamesInTestsFolder,s);
//                System.out.println("TEST CONTAINS US COUNT: "+info.getUnusedStub().getInfo()+ " "+taFile.get(s).size() + " "+ fn+ " " + FileUtils.countTestsInFile(fn));
            }
        }

        return infos;
    }

    private static boolean isBefore(String methodName, String fileName) throws FileNotFoundException {
        boolean result = false;
        CompilationUnit cu = StaticJavaParser.parse(new File(fileName));
//        List<MethodCallExpr> l = cu.findAll(MethodCallExpr.class);
//        for(MethodCallExpr mce: l){
//            if(methodName.contains(mce.getName().getIdentifier())){
//                if(mce.)
//                result = true;
//                break;
//            }
//        }
        List<ClassOrInterfaceDeclaration>list = cu.findAll(ClassOrInterfaceDeclaration.class);
        for(ClassOrInterfaceDeclaration l: list){
                List<MethodDeclaration> methodDeclarations = l.findAll(MethodDeclaration.class);
            for (MethodDeclaration md : methodDeclarations) {
//                System.out.println(methodName+" "+md.getName().getIdentifier());
                if(md.getName().getIdentifier().equals(methodName)){
                    for(AnnotationExpr anno: md.getAnnotations()){
//                        System.out.println(anno.getName().getIdentifier() + " "+methodName);
                        if(anno.getName().getIdentifier().contains("Before") || md.getName().getIdentifier().equalsIgnoreCase("before")){
                            result = true;
                            break;
                        }
                    }
                }

            }
        }
        return result;
    }

    public static String getFileName(Set<String> javaFileNamesInTestsFolder, String testClassName) {
        for(String fileName: javaFileNamesInTestsFolder){
            if(fileName.contains(testClassName.replace(".", "/")+".java")){
                return fileName;
            }
        }
        return "Empty";
    }

    private static String getMatchingFileName(Set<String> javaFileNamesInTestsFolder, UnusedStub us){
        for (String file : javaFileNamesInTestsFolder) {
            String[] paths = file.split("/");
            if (paths[paths.length - 1].equals(us.getStubbingLocation().getFileName())) {
                return file;
            }
        }
        return null;
    }
    private static int IfStubIsInvoked(TestAnalysis ta, UnusedStub us){
        int invokedCount = 0;
        for (StubInvocation si : ta.getStubInvocations()) {
            if(si.getInfo().equals(us.getInfo())){
                invokedCount++;
            }
        }
        return invokedCount;
    }
    public static void getRelatedClassMap(Set<String> javaFileNamesInTestsFolder, UnusedStub us, FixUnusedStubsState fixUnusedStubsState) throws FileNotFoundException {
        //////get the testClass name of each testAnalysis because they may not in the same testClass
        Set<String> case2taTestClassNameList = new HashSet<>();
        for (TestAnalysis ta : us.testAnalysisList) {
            String[] fullPath = ta.getTestClassName().split("\\.");
            String taTestClassName = fullPath[fullPath.length - 1];
            case2taTestClassNameList.add(taTestClassName);
        }
        ////////get the test classes related to the test class which has the unused stub
        Map<String, Set<String>> relatedClassMap = fixUnusedStubsState.getRelatedClassMap();
        if (relatedClassMap.containsKey(us.getStubbingLocation().getClassName())) {
            relatedClassMap.get(us.getStubbingLocation().getClassName()).addAll(case2taTestClassNameList);
        } else {
            //get a list contains all testClasses which use the class that has the unused stub, and store their fileName for later use
            List<String> case2AllTestClassNamesList = RemoveStubUtils.ifIsImported(javaFileNamesInTestsFolder, us.getStubbingLocation().getClassName());
            for (String fileFullPath : case2AllTestClassNamesList) {
                String[] splittedFileFullPath = fileFullPath.split("/");
                String fileName = splittedFileFullPath[splittedFileFullPath.length - 1];
                String[] fileNameSplits = fileName.split(".java");
                case2taTestClassNameList.add(fileNameSplits[0]);
                fixUnusedStubsState.getAddTestNameRuleList().add(fileFullPath);
            }
            relatedClassMap.put(us.getStubbingLocation().getClassName(), case2taTestClassNameList);
        }
    }
}
