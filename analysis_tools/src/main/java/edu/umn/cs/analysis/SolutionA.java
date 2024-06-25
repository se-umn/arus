package edu.umn.cs.analysis;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.umn.cs.analysis.model.*;
import org.javatuples.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class SolutionA {

    public static Set<String> fixUnusedStubs(List<TestAnalysis> testAnalyses, Set<String> javaFileNamesInTestsFolder, AnalysisStatistics analysisStatistics, boolean remove_uus) throws IOException {
        FixUnusedStubsState  fixUnusedStubsState = new FixUnusedStubsState();
        int insertIfStatementsCount = 0;
        int addedTestCount = 0;
//        Set<String> changedMethodNameList = new HashSet<>();
//        Set<String> addedFileList = new HashSet<>();
//        Set<String> changedFileNameList = new HashSet<>();
//        Set<String> addedMethodNameList = new HashSet<>();
//
        //////get a list of change information
        List<ChangeInfo> ChangeInfoList = SolutionA.identifyUnusedStubs(testAnalyses, javaFileNamesInTestsFolder, fixUnusedStubsState);


        //////get the file path for later use of adding the umnutils package
        String filePath = "";
        for(String s: javaFileNamesInTestsFolder){
            filePath = s;
            break;
        }
        fixUnusedStubsState.setFilePath(filePath.split("test/java")[0]);
//        System.out.println(fixUnusedStubsState.getRelatedClassMap());

        //////////////////Group case2 information again and put name of tests which use the same stub together/////////////
        //////a new list of changeInfo contains us besides type 2
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
            //////get the list of testAnalysis which use the stubs and store the file name which needs to be added testName rule
            HashMap<String, List<String>> case2taUsingStubs = new HashMap<>();
            for (TestAnalysis ta : testAnalyses) {
                if (case2Map.get(key).getTaList().contains(ta)){
                    String fileName = getMatchingFileName(javaFileNamesInTestsFolder, ta.getTestClassName());
                    if(case2taUsingStubs.keySet().contains(fileName)){
                        case2taUsingStubs.get(fileName).add(ta.getTestMethodName());
                    }
                    else{
                        List<String> taList = new ArrayList<>();
                        taList.add(ta.getTestMethodName());
                        case2taUsingStubs.put(fileName, taList);
                    }
                }
            }

            //////get the list of unusedStub stubbing location's line numbers
            List<Integer> case2unusedStubStubblingLocLineNums = new ArrayList<>();
            case2unusedStubStubblingLocLineNums.add(case2Map.get(key).getUnusedStub().getStubbingLocation().getLineNum());

            //////update the testAnalyses and lineNumbers for the unusedstub
            if(case2taUsingStubs.size()!= 0){
//                System.out.println("case2 ta using stubs "+ case2taUsingStubs);
                case2Map.get(key).setSolATestAnalysesMap(case2taUsingStubs);
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
        List<DuplicateTestClassInfo> duplicateTestClassInfoList = new ArrayList<>();
        Map<String, List<DuplicateTestClassInfo>> case2duplicateTestClassInfoMap = new HashMap<>();
        ///////////////////collect the stubs' changeInfo all together for the later removing use////////////////////
        for(String fileName: fileWithStubsMap.keySet()) {
            ////super class here means the class is related to others.
            boolean isSuperClass = false;
//            System.out.println("Map "+fixUnusedStubsState.getRelatedClassMap());

            for(String superclass: fixUnusedStubsState.getRelatedClassMap().keySet()){
                if(fileName.contains(superclass.replace(".","/")+".java")){
                    isSuperClass = true;
                    break;
                }
            }

//            System.out.println("is super "+fileName+" "+isSuperClass);

            //////for case 1 remove the stub
            List<Integer> case1UnusedStubsStubbingLocLineNums = new ArrayList<>();
            List<String> case1UnusedStubsStubbedMethodName = new ArrayList<>();
            List<String> case1UnusedStubsStubbingLocMethodName = new ArrayList<>();

            //////for case2 case3 and case4 insert test names
            Map<String, Integer> testClassIndexMap = new HashMap<>();
            Map<Pair<String, String>, Set<ChangeInfo>> testMethodMap = new HashMap<>();


            ////////for case3 so that the stackcomponents can be later transformed to Edges
            List<List> UnusedStubStackcomponents = new ArrayList<>();
            List<String> case3UnusedStubsStubbedMethodName = new ArrayList<>();


            CompilationUnit cu = StaticJavaParser.parse(new File(fileName));

            for (ChangeInfo info : fileWithStubsMap.get(fileName)) {

                UnusedStub us = info.getUnusedStub();

                //gather case1 information
                if (info.getType() == 1) {
                    case1UnusedStubsStubbingLocLineNums.add(us.getStubbingLocation().getLineNum());
                    case1UnusedStubsStubbedMethodName.add(us.getStubbedMethodName());
                    case1UnusedStubsStubbingLocMethodName.add(us.getStubbingLocation().getMethodName());
                    fixUnusedStubsState.changedMethodNameList.add(us.getStubbingLocation().getMethodName());
                }

                //gather case2,3,4 information
                else if (info.getType() == 2){
                    if(remove_uus){
                        for(String key: info.getSolATestAnalysesMap().keySet()){
                        for(String singleTA: info.getSolATestAnalysesMap().get(key)){
                            Pair<String, String> pair = new Pair(singleTA, key);
                            if (testMethodMap.containsKey(pair)){
                                testMethodMap.get(pair).add(info);
                            }
                            else{
                                Set<ChangeInfo> set = new HashSet<>();
                                set.add(info);
                                testMethodMap.put(pair, set);
                            }
                        }

                    }
                    }
                }
                //////gather case3 stackComponent
                else if(info.getType() == 3){
                    case3UnusedStubsStubbedMethodName.add(info.getUnusedStub().getStubbedMethodName());
                    UnusedStubStackcomponents.add(info.getUnusedStub().getStubbingLocationStack());
                    if(fixUnusedStubsState.case3StubbedMethodMap.containsKey(info.getUnusedStub().getStubbingLocation().getLineNum())){
                        fixUnusedStubsState.case3StubbedMethodMap.get(info.getUnusedStub().getStubbingLocation().getLineNum()).add(info.getUnusedStub().getStubbedMethodName());
                    }
                    else{
                        List<String> stubbedMethodNameList = new ArrayList<>();
                        stubbedMethodNameList.add(info.getUnusedStub().getStubbedMethodName());
                        fixUnusedStubsState.case3StubbedMethodMap.put(info.getUnusedStub().getStubbingLocation().getLineNum(), stubbedMethodNameList);
                    }
                }
            }

            //////solve case1
            if (case1UnusedStubsStubbingLocLineNums.size()!= 0){
//                System.out.println("case1Solution: "+fileName + " " + case1UnusedStubsStubbingLocLineNums + " " + case1UnusedStubsStubbedMethodName+ " " + case1UnusedStubsStubbingLocMethodName);
                cu = RemoveStubUtils.removeStub(cu,case1UnusedStubsStubbingLocLineNums,case1UnusedStubsStubbedMethodName,case1UnusedStubsStubbingLocMethodName);
            }
            //////solve case2
            if(!testMethodMap.isEmpty()){
//                System.out.println(testMethodMap);

                Map<Set<ChangeInfo>, List<Pair<String,String>>> reverseMap = new HashMap<>();
                for(Pair<String, String> dtci: testMethodMap.keySet()){
                    Set<ChangeInfo> set = testMethodMap.get(dtci);
                    if(reverseMap.containsKey(set)){
                        reverseMap.get(set).add(dtci);
                    }
                    else{
                        List<Pair<String,String>> list = new ArrayList<>();
                        list.add(dtci);
                        reverseMap.put(set,list);
                    }
                }
                List<Map<Set<ChangeInfo>, List<Pair<String,String>>>> reverseMapList = new ArrayList<>();
                for(Set<ChangeInfo> key: reverseMap.keySet()){
                    List<Pair<String,String>> value = reverseMap.get(key);
                    List<List<Pair<String,String>>> output = new ArrayList<>();

                    // Step 1: Create an empty HashMap
                    Map<String, List<Pair<String,String>>> map = new HashMap<>();

                    // Step 2: Loop through the input list
                    for (Pair<String,String> pair : value) {
                        // Step 2a: Extract the second element (value) from the pair
                        String v1 = pair.getValue1();

                        // Step 2b: Check if the HashMap already contains a sublist with the same value
                        if (map.containsKey(v1)) {
                            // Step 2c: Add the pair to the existing sublist
                            map.get(v1).add(pair);
                        } else {
                            // Step 2d: Create a new sublist with the pair and add it to the HashMap
                            List<Pair<String,String>> sublist = new ArrayList<>();
                            sublist.add(pair);
                            map.put(v1, sublist);
                        }
                    }
                    output = new ArrayList<>(map.values());
                    for(int i = 0; i<output.size();i++){
                        HashMap<Set<ChangeInfo>, List<Pair<String,String>>> map1 = new HashMap<>();
                        map1.put(key,output.get(i));
                        reverseMapList.add(map1);
                    }
                }
                for(Map<Set<ChangeInfo>, List<Pair<String,String>>> map: reverseMapList){
                    for(Set<ChangeInfo> key: map.keySet()){

                        String stubbingLocClassName = "";

                        List<String> stubbedNameList = new ArrayList<>();
                        List<String> testMethodList = new ArrayList<>();
                        List<Integer> lineNumList = new ArrayList<>();
                        UnusedStub us = null;
                        for(ChangeInfo ci: key){
                            stubbedNameList.add(ci.getUnusedStub().getStubbedMethodName());
                            testMethodList.add(ci.getUnusedStub().getStubbingLocation().getMethodName());
                            lineNumList.add(ci.getUnusedStub().getStubbingLocation().getLineNum());
                            stubbingLocClassName = ci.getUnusedStub().getStubbingLocation().getFileName();
                            us = ci.getUnusedStub();
                        }



                        List<String> taList = new ArrayList<>();
                        String testFileNameContainsTa = "";
                        for(Pair<String,String> dtci: map.get(key)){
                            taList.add(dtci.getValue0());
                            testFileNameContainsTa = dtci.getValue1();
                        }

                        if(us!=null){
                            String fn = getMatchingFileName(javaFileNamesInTestsFolder, us);
//                            System.out.println("TEST CONTAINS US COUNT: "+us.getInfo()+ " "+taList.size() + " "+ fn+ " " + FileUtils.countTestsInFile(fn));
                        }

                        String duplicatedTestClassName = stubbingLocClassName.replace(".java", "");
                        if(testClassIndexMap.containsKey(duplicatedTestClassName)){

                            int index = testClassIndexMap.get(duplicatedTestClassName);
                            index++;
                            testClassIndexMap.replace(duplicatedTestClassName, index);
                        }
                        else{
                            testClassIndexMap.put(duplicatedTestClassName,2);
                        }

                        String newTestName = NumberToString.convert(testClassIndexMap.get(duplicatedTestClassName))+duplicatedTestClassName;
                        String newFilePath = fileName.replace(duplicatedTestClassName, newTestName);

                        if(fileName.contains("repos_tmp/jenkinsci_hashicorp-vault-plugin_experiment/src/test/java/com/datapipe/jenkins")){
//                        System.out.println("ADD DUPLICATE to List " + fileName + " " + duplicatedTestClassName +" "+ newTestName + " "+ newFilePath +" "+lineNumList+ " " +stubbedNameList + " "+testMethodList+" "+taList);
                            DuplicateTestClassInfo case2DuplicateTCinfo = new DuplicateTestClassInfo(cu, fileName, duplicatedTestClassName, newTestName, newFilePath, lineNumList, stubbedNameList, testMethodList, taList);

                            if(case2duplicateTestClassInfoMap.containsKey(fileName)){
                                case2duplicateTestClassInfoMap.get(fileName).add(case2DuplicateTCinfo);
                            }
                            else{
                                List<DuplicateTestClassInfo> value = new ArrayList<>();
                                value.add(case2DuplicateTCinfo);
                                case2duplicateTestClassInfoMap.put(fileName,value);
                            }
                        }
                        else{
                            if(isSuperClass){
//                            System.out.println("SUPER CLASS ADD DUPLICATE to List " + fileName + " " + duplicatedTestClassName +" "+ newTestName + " "+ newFilePath +" "+lineNumList+ " " +stubbedNameList + " "+testMethodList+" "+taList);
                                DuplicateTestClassInfo case2DuplicateTCinfo = new DuplicateTestClassInfo(cu, fileName, duplicatedTestClassName, newTestName, newFilePath, lineNumList, stubbedNameList, testMethodList, taList);

                                if(case2duplicateTestClassInfoMap.containsKey(fileName)){
                                    case2duplicateTestClassInfoMap.get(fileName).add(case2DuplicateTCinfo);
                                }
                                else{
                                    List<DuplicateTestClassInfo> value = new ArrayList<>();
                                    value.add(case2DuplicateTCinfo);
                                    case2duplicateTestClassInfoMap.put(fileName,value);
                                }
                            }
                            else{
                                fixUnusedStubsState.addedFileList.add(newFilePath);
                                fixUnusedStubsState.changedFileNameList.add(fileName.replace("_experiment", ""));
//                                System.out.println("DUPLICATE " + newTestName + " "+ newFilePath +" "+lineNumList+ " " +stubbedNameList + " "+testMethodList+" "+taList);
                                RemoveStubUtils.duplicateTestClasse(cu, duplicatedTestClassName, newTestName, newFilePath, lineNumList, stubbedNameList, testMethodList, taList);
                            }

                        }

                        if(!testFileNameContainsTa.equals(fileName)){
                            DuplicateTestClassInfo duplicatedTCInfo = new DuplicateTestClassInfo(testFileNameContainsTa,duplicatedTestClassName,newTestName,taList);
                            duplicateTestClassInfoList.add(duplicatedTCInfo);
                        }
                    }
                }
            }

            //////convert case3's stackComponent to Edge
            if(UnusedStubStackcomponents.size()!= 0){
                for(int j = 0; j<UnusedStubStackcomponents.size();j++){

                    ////////////preprocess stackcomponents
                    int index = -100;
                    List<StackComponent> toberemovedSctList = new ArrayList<>();
                    List<StackComponent> l = UnusedStubStackcomponents.get(j);
                    for(int i = 0; i <l.size(); i++){
                        if(l.get(i).getMethodName().toLowerCase().equals("foreach")){
                            index = i;
                        }
                        boolean sctFileNameisInTestFolder = false;
                        for(String FN: javaFileNamesInTestsFolder){
                            if(FN.contains(l.get(i).getFileName())){
                                sctFileNameisInTestFolder = true;
                                break;
                            }
                        }
                        if(!sctFileNameisInTestFolder){
                            toberemovedSctList.add(l.get(i));
                        }
                    }

                    if(index!=-100){
                        int size = l.size();
                        for(int i = size-1; i>=index; i--){
                            l.remove(i);
                        }
                    }

                    if(!toberemovedSctList.isEmpty()){
                        for(StackComponent sct: toberemovedSctList){
                            l.remove(sct);
                        }
                    }
                }


                List<Edge> edges = new ArrayList<>();
//
                for(int j = 0; j<UnusedStubStackcomponents.size();j++){
                    List<StackComponent> l = UnusedStubStackcomponents.get(j);
                    if(l.size()!=4){
                        for(int i = l.size()-1; i >= 0; i--) {
//                           //////the first component does not have the previous one
                            if(i == l.size()-1){
                                //////if the stub only has one stackComponent set hasNext to false;
                                if(i == 1){
                                    Edge edge = new Edge(null, l.get(i), l.get(i-1), false);
                                    edges.add(edge);
                                }
                                else{
                                    ////could happen because the us is in @before method
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

//                System.out.println("Current Analyzing: "+fileName);
                //////put the edge into a graph
                Graph graph = new Graph(edges);
                ////print the graph
//                Graph.printGraph(graph);
                ////solve case3
                cu = Graph.analyzeGraph(graph, cu, fileName, javaFileNamesInTestsFolder,fixUnusedStubsState);

            }

            //////export the file if the file is not the super class, otherwise save it.
            if(!isSuperClass || case2duplicateTestClassInfoMap.isEmpty()){
                Files.write(new File(fileName).toPath(), Collections.singleton(cu.toString()), StandardCharsets.UTF_8);
                fixUnusedStubsState.getChangedFilesName().add(fileName.replace("_experiment", ""));
            }
            if(remove_uus){
                //////special repo 2 needs to be handled separately
                if(fileName.contains("jenkinsci_chucknorris-plugin_experiment/src/test/java/hudson/plugins/chucknorris/RoundhouseActionTest.java")){
//                System.out.println("Needs to be DELETED"+ fileName);
                    File fileToBeDeleted = new File(fileName);
                    if(!fileToBeDeleted.delete()){
                        System.out.println("ERROR: file is not deleted");
                    }
                }
            }

        }
        //////add imports if in case3, method names to be changed and the method is duplicated are not in the same class
        if(fixUnusedStubsState.getImportFileName().size()!= 0){
            for(int i = 0; i< fixUnusedStubsState.getImportFileName().size(); i++){
                CompilationUnit cu = StaticJavaParser.parse(new File(fixUnusedStubsState.getImportFileName().get(i)));
                cu.addImport(fixUnusedStubsState.getImportName().get(i), fixUnusedStubsState.getImportIsStatic().get(i), false);
                Files.write(new File(fixUnusedStubsState.getImportFileName().get(i)).toPath(), Collections.singleton(cu.toString()), StandardCharsets.UTF_8);
                fixUnusedStubsState.getChangedFilesName().add(fixUnusedStubsState.getImportFileName().get(i).replace("_experiment", ""));
            }

        }
        ////////solve duplicate test class out of files because sometimes case3 changes the file we are going to duplicate.
        if(!case2duplicateTestClassInfoMap.isEmpty()){
            for(String key: case2duplicateTestClassInfoMap.keySet()){
                List<DuplicateTestClassInfo> list = case2duplicateTestClassInfoMap.get(key);
                CompilationUnit cu = null;
                for(DuplicateTestClassInfo dtc: list){
                    fixUnusedStubsState.addedFileList.add(dtc.newTestName);
                    fixUnusedStubsState.changedFileNameList.add(dtc.fileName.replace("_experiment", ""));
//                    System.out.println("SUPER CLASS DUPLICATE: " + dtc.newTestName + " "+ dtc.newFilePath +" "+dtc.lineNumList+ " " +dtc.stubbedNameList + " "+dtc.testMethodList+" "+dtc.taList);
                    RemoveStubUtils.duplicateTestClasse(dtc.cu, dtc.duplicatedTestClassName, dtc.newTestName, dtc.newFilePath, dtc.lineNumList, dtc.stubbedNameList, dtc.testMethodList, dtc.taList);
                    cu = dtc.cu;
                }
                //////special repo 1 needs to be handled separately
                if(key.contains("jenkinsci_google-compute-engine-plugin_experiment/src/test/java/com/google/jenkins/plugins/computeengine/InstanceConfigurationTest.java")){
//                    System.out.println("Add empty test"+ key);
                    fixUnusedStubsState.addedMethodNameList.add("emptyTest");
                    RemoveStubUtils.addEmptyTest(cu);
                    addedTestCount++;

                }
                Files.write(new File(key).toPath(), Collections.singleton(cu.toString()), StandardCharsets.UTF_8);
            }

        }
        if(!duplicateTestClassInfoList.isEmpty()){
            Map<String, Integer> testClassIndexMap = new HashMap<>();
            for(DuplicateTestClassInfo dtc: duplicateTestClassInfoList){
                //get the testName
                if(testClassIndexMap.containsKey(dtc.fileName)){
                    int index = testClassIndexMap.get(dtc.fileName);
                    testClassIndexMap.replace(dtc.fileName, index++);
                }
                else{
                    testClassIndexMap.put(dtc.fileName,2);
                }

                String[] splittedFileFullPath = dtc.fileName.split("/");
                String oldFileName = splittedFileFullPath[splittedFileFullPath.length - 1];
                String[] fileNameSplits = oldFileName.split("\\.java");

                String newFileName = NumberToString.convert(testClassIndexMap.get(dtc.fileName))+ fileNameSplits[0];
                String newFilePath = dtc.fileName.replace(fileNameSplits[0], newFileName);

//              containsTestBefore = RemoveStubUtils.ifContainsTest(dtc.fileName);
                CompilationUnit cu = StaticJavaParser.parse(new File(dtc.fileName));
//                System.out.println("OUTSIDE: "+dtc.taList+" "+newFilePath+" "+newFileName+" "+ fileNameSplits[0]+dtc.newTCName+" "+dtc.oldTCName);
                fixUnusedStubsState.addedFileList.add(newFileName);
                fixUnusedStubsState.changedFileNameList.add(dtc.fileName.replace("_experiment", ""));
                RemoveStubUtils.duplicateOutsideTestClass(cu, dtc.taList, newFilePath, newFileName, fileNameSplits[0], dtc.newTCName, dtc.oldTCName);
                Files.write(new File(dtc.fileName).toPath(), Collections.singleton(cu.toString()), StandardCharsets.UTF_8);
            }
        }

        if(insertIfStatementsCount>0){
            analysisStatistics.setInsertIfStatementsCount(insertIfStatementsCount);
        }
        if(addedTestCount>0){
            analysisStatistics.setAddedTestCount(addedTestCount);
        }
        analysisStatistics.setAddedFileCount(fixUnusedStubsState.addedFileList.size());
        analysisStatistics.setAddedMethodsCount(fixUnusedStubsState.addedMethodNameList.size());
        analysisStatistics.setChangedFileCount(fixUnusedStubsState.changedFileNameList.size());
        analysisStatistics.setChangedMethodsCount(fixUnusedStubsState.changedMethodNameList.size());
//        System.out.println("added files: "+fixUnusedStubsState.addedFileList);
//        System.out.println("added methods: "+fixUnusedStubsState.addedMethodNameList);
//        System.out.println("changed files: "+fixUnusedStubsState.changedFileNameList);
//        System.out.println("changed methods: "+fixUnusedStubsState.changedMethodNameList);

        return fixUnusedStubsState.getChangedFilesName();
    }

    public static List<ChangeInfo> identifyUnusedStubs(List<TestAnalysis> transformTestAnalyses, Set<String> javaFileNamesInTestsFolder, FixUnusedStubsState fixUnusedStubsState) throws IOException {

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
//        System.out.println("US DEFINITION COUNT: "+usDefinitions.size());

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

        /////////////////////////loop through the unusedStubList, identify each stub's type and covert stub to changeInfo//////////////////////
        for(int i =0; i<unusedStubList.size();i++){

            UnusedStub us = unusedStubList.get(i);
            List<TestAnalysis> testAnalysisList = us.testAnalysisList;

            ///////three booleans used for identify case1
            boolean usingElsewhere = false;
            boolean usingEverywhere = false;
            boolean ifInLoop = false;

            //////a set consists of all testAnalyses in the TestClass which exists the stub
            Set<TestAnalysis> taInOneFile = new HashSet<>();

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
                    }
                }
            }

            String unusedStubInfo = us.getInfo();

            ///////check if the unusedStub is used in a loop

            for (String testFileName : javaFileNamesInTestsFolder) {
                String[] paths = testFileName.split("/");
                if (paths[paths.length-1].equals(us.getStubbingLocation().getFileName())) {
                    ifInLoop = RemoveStubUtils.ifInLoop(testFileName, us.getStubbingLocation().getLineNum(), us.getStubbingLocation().getMethodName());
                    break;
                }
            }

            boolean appearOnce = true;
            int methodContaingStubIsCalledCount = 0;

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
                        break;
                    }
                }
                if(appearOnce && methodContaingStubIsCalledCount <= 1){
                    usingEverywhere = true;
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
                    if(us.getStubbingLocation().getMethodName().equals(matchingStubCreation.getExecutionLocation().getMethodName())) {
                        if(matchingStubCreation.getExecutionLocation().getType().equals(ExecutionLocationType.TEST_METHOD)){
                            System.out.println("may happen because of Parameterized tests");
                        }
                        else{
                            String testFileName = getMatchingFileName(javaFileNamesInTestsFolder, us);
                            //get related class map which can be lated used to generate the if statement condition
                            getRelatedClassMap(javaFileNamesInTestsFolder, us, fixUnusedStubsState);
                            if(!testFileName.equals("")){
//                            System.out.println("1case2: " + us.getStubbingLocation().getClassName()+"/"+us.getStubbingLocation().getMethodName()+"/"+us.getStubbingLocation().getLineNum());
                                ChangeInfo case2Info = new ChangeInfo(testFileName, testAnalysisList, us, us.getStubbingLocation().getMethodName(), 2);
                                infos.add(case2Info);
                            }
                        }
                    }
                    else{
                        List<TestAnalysis> case2List = new ArrayList<>();
                        for (TestAnalysis ta : testAnalysisList) {
                            //A map to store info of stubIvocation which with us are in the same method. Key is SI's location and value is the time it appears
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
                                if(SCcount <= 1){
                                    for (String testFilename : javaFileNamesInTestsFolder) {
                                        String[] path = testFilename.split("/");
                                        if (path[path.length-1].equals(us.getStubbingLocation().getFileName())) {
//                                            System.out.println("1case3: " + us.getStubbingLocation().getClassName()+"/"+us.getStubbingLocation().getMethodName()+"/"+us.getStubbingLocation().getLineNum());
                                            ChangeInfo case3Info = new ChangeInfo(testFilename, us.getStubbingLocation().getLineNum(), us,3);
                                            infos.add(case3Info);
                                        }
                                    }
//                                    }
                                }
                                //////it is in loop
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
                            boolean isSctInBefore = false;
                            for(StackComponent sct: us.getStubbingLocationStack()){
                                String fileName = getMatchingFileName(javaFileNamesInTestsFolder, sct.getClassName());
                                //get related class map which can be lated used to generate the if statement condition
                                getRelatedClassMap(javaFileNamesInTestsFolder, us, fixUnusedStubsState);
                                if(!fileName.equals("")){
                                    ///////////only when the stub is created and executed in
                                    if(RemoveStubUtils.isInBeforeMethod(fileName, sct.getMethodName())){
                                        isSctInBefore = true;
                                    }
                                }
                            }
                            String testFileName = getMatchingFileName(javaFileNamesInTestsFolder, us);
                            //get related class map which can be lated used to generate the if statement condition
                            getRelatedClassMap(javaFileNamesInTestsFolder, us, fixUnusedStubsState);
                            if(!testFileName.equals("")){
                                ///////////only when the stub is created and executed in
                                if(isSctInBefore){
//                                    System.out.println("2case2: " + us.getStubbingLocation().getClassName()+"/"+us.getStubbingLocation().getMethodName()+"/"+us.getStubbingLocation().getLineNum());
                                    ChangeInfo case2Info = new ChangeInfo(testFileName, case2List, us, us.getStubbingLocation().getMethodName(), 2);
                                    infos.add(case2Info);
                                }
                                else{
                                        ChangeInfo case3Info = new ChangeInfo(testFileName, us.getStubbingLocation().getLineNum(), us,3);
                                        infos.add(case3Info);
                                }
                            }
                        }
                    }
                }
            }
        }
        return infos;
    }


    private static String getMatchingFileName(Set<String> javaFileNamesInTestsFolder, UnusedStub us){
        for (String file : javaFileNamesInTestsFolder) {
            String[] paths = file.split("/");
            if (paths[paths.length - 1].equals(us.getStubbingLocation().getFileName())) {
                return file;
            }
        }
        return "";
    }
    public static String getMatchingFileName(Set<String> javaFileNamesInTestsFolder, String className){
        String newClassName = className.replace(".", "/")+".java";
        for (String file : javaFileNamesInTestsFolder) {
            if (file.contains(newClassName) || file.contains(className)) {
                return file;
            }
        }
        return "";
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
            if(!ta.getTestClassName().equals(us.getStubbingLocation().getClassName())){
                case2taTestClassNameList.add(taTestClassName);
            }
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
                String[] fileNameSplits = fileName.split("\\.java");
                case2taTestClassNameList.add(fileNameSplits[0]);
                fixUnusedStubsState.getAddTestNameRuleList().add(fileFullPath);
            }

            if(!case2taTestClassNameList.isEmpty()){
                relatedClassMap.put(us.getStubbingLocation().getClassName(), case2taTestClassNameList);
            }
        }
    }
}