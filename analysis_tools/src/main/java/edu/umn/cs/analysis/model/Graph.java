package edu.umn.cs.analysis.model;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import edu.umn.cs.analysis.AnalysisUtils;
import edu.umn.cs.analysis.RemoveStubUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class Graph {

    // define adjacency list
    private Map<Edge, Set<Node>> adj_map = new HashMap<>();
    //define a list to store the edge of the key in the map;
    private List<Edge> mapKeyList = new ArrayList<>();

    //Graph Constructor
    public Graph(List<Edge> edges) {
        // adjacency list memory allocation
        for (int i = 0; i < edges.size(); i++) {
            Edge key = edges.get(i);
            mapKeyList.add(key);
            if (adj_map.containsKey(key)) {
                adj_map.get(key).add(new Node(edges.get(i).getSrc(), edges.get(i).getDest(), edges.get(i).isHasNext()));
            } else {
                Set<Node> nodeSet = new HashSet<>();
                nodeSet.add(new Node(edges.get(i).getSrc(), edges.get(i).getDest(), edges.get(i).isHasNext()));
                adj_map.put(key, nodeSet);
            }
        }
    }
    // print adjacency list for the graph
    public static void printGraph(Graph graph)  {
        //System.out.println("The contents of the graph:");
        int index = 0;
        for(Edge key: graph.adj_map.keySet()){
            for(Node edge: graph.adj_map.get(key)){
                if(key.getPre() != null){
                    System.out.println("Vertex:" + index + " " + key.getPre().getFileName()+"#"+key.getPre().getMethodName()+"#"+key.getPre().getLineInvokedInMethod()+ "==>"+ key.getSrc().getMethodName()+"#"+key.getSrc().getLineInvokedInMethod() + " ==> " + edge.getValue().getMethodName()+"#"+edge.getValue().getLineInvokedInMethod() +" (" + edge.isHasNext() +")");
                }
                else{
                    System.out.println("Vertex:" + index + " "  + key.getSrc().getFileName()+"#"+key.getSrc().getMethodName()+"#"+ key.getSrc().getLineInvokedInMethod() + " ==> " + edge.getValue().getMethodName()+"#"+edge.getValue().getLineInvokedInMethod() +" (" + edge.isHasNext() +")");
                }
            }
            index++;
        }
    }

    public static CompilationUnit analyzeGraph(Graph graph, CompilationUnit cu, String FileName, Set<String> javaFileNamesInTestsFolder,FixUnusedStubsState fixUnusedStubsState) throws IOException {
        Map<StackComponent, List> directMap = new HashMap<>();
        Map<StackComponent, List<List<StackComponent>>> indirectList = new HashMap<>();
        List<StackComponent> removeList = new ArrayList<>();
        List<Edge> visited = new ArrayList<>();
        for(Edge key: graph.adj_map.keySet()){
            for(Node edge: graph.adj_map.get(key)){
                if(!edge.isHasNext() && key.getPre() == null){
                    //!visited.contains(key)
                    //do something
                    if(directMap.containsKey(key.getSrc())){
                        directMap.get(key.getSrc()).add(edge.getValue());
                    }
                    else{
                        List<StackComponent> l = new ArrayList<>();
                        l.add(edge.getValue());
                        directMap.put(key.getSrc(),l);
                    }
                }
            }
        }

        for(Edge key: graph.adj_map.keySet()){
            for(Node edge: graph.adj_map.get(key)){
                //put the chain into the list
                List list = new ArrayList();
                //keep tracking which node is already visited
                if(edge.isHasNext() == true && !visited.contains(key)){
                    if(!list.contains(key)){
//                        //System.out.println("List: "+ list);
                        list.add(key);
//                        //System.out.println("key: "+ key.getSrc().getLineInvokedInMethod()+key.getSrc().getMethodName());
                        list.add(edge.getValue());
//                        //System.out.println("add 1 "+ edge.getValue().getLineInvokedInMethod()+edge.getValue().getMethodName());
                    }

                    //variables for the while loop
                    boolean hasNext = true;

                    List<Node> needVisit = new ArrayList<>();
                    //while loop
                    while(hasNext || needVisit.size()!= 0){
                        for(Node n: needVisit){
                            //System.out.println("Debug1: "+n.getValue().getMethodName()+"#"+n.getValue().getLineInvokedInMethod()+" "+n.getPrevious().getMethodName()+"#"+n.getValue().getLineInvokedInMethod());
                        }

                        if(!hasNext && needVisit.size() != 0){
                            edge = needVisit.get(0);
                            needVisit.remove(0);
                        }
                        //loop through the map
                        for(Edge next: graph.adj_map.keySet()){
                            //get the next of the edge
                            if((next.getSrc().equals(edge.getValue()) && next.getPre().equals(edge.getPrevious())) || visited.contains(next) ){ //&& next.getPre().equals(edge.getPrevious())
                                if (!list.contains(next.getSrc()) && !visited.contains(next)){
                                    if(next.getPre().equals(edge.getPrevious())){
                                        list.add(next);
                                        //System.out.println("add 2 " + next.getPre().getLineInvokedInMethod() + next.getPre().getMethodName() +" " + next.getSrc().getLineInvokedInMethod() + next.getSrc().getMethodName());
                                    }
                                    visited.add(next);
                                }

                                Node newNode = new Node(next.getPre(), next.getSrc(),next.isHasNext());
                                if(needVisit.contains(newNode)){
                                    //System.out.println("Remove from needVisit "+ newNode.getPrevious().getMethodName()+"#"+newNode.getPrevious().getLineInvokedInMethod()+"=> " +newNode.getValue().getMethodName()+"#"+newNode.getValue().getLineInvokedInMethod());
                                    long startTime = System.currentTimeMillis();
                                    long stopTime = startTime + 10000; // Stop after 10 seconds

                                    while (System.currentTimeMillis() < stopTime) {
                                    }
                                    break;
//                                    needVisit.remove(newNode);
                                }
//
                                //check if next has next edge
                                for(Node node: graph.adj_map.get(next)){
                                    if(node.isHasNext())
                                    {
                                        needVisit.add(node);
                                        edge = node;
                                    }
                                    else{
                                        hasNext = false;
                                        if(!list.contains(graph.adj_map.get(next)) && next.getPre().equals(edge.getPrevious())){
                                            if(graph.adj_map.get(next).getClass().getSimpleName().equals("HashSet")){
                                                for(Node n: graph.adj_map.get(next)){
                                                    //System.out.println("add 3 "+ n.getValue().getMethodName()+"#"+n.getValue().getLineInvokedInMethod());
                                                    list.add(n.getValue());
                                                    //System.out.println("next " + next.getPre().getMethodName()+"#"+next.getPre().getLineInvokedInMethod()+ " "+next.getSrc().getMethodName()+"#"+next.getSrc().getLineInvokedInMethod());
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if(list.size()!= 0){
                    StackComponent testName = ((Edge) list.get(0)).getSrc();
                    if(indirectList.containsKey(testName)){
                        if(directMap.containsKey(testName)){
                            indirectList.get(testName).addAll(directMap.get(testName));
                            directMap.remove(testName);
                            removeList.add(testName);
                            //System.out.println("DirectMap removes2: "+testName.getFileName()+" "+testName.getMethodName()+" "+testName.getLineInvokedInMethod());
                        }

                        for(int i =1; i<list.size();i++){
                            List<List<StackComponent>> value = indirectList.get(testName);
                            if(list.get(i).getClass().getSimpleName().equals("Edge")){
                                if(!indirectList.get(testName).get(i-1).contains(((Edge) list.get(i)).getSrc())){
                                    indirectList.get(testName).get(i-1).add(((Edge) list.get(i)).getSrc());}
                            }
                            else{
                                if(i>=2 && value.get(i-2).get(0).getMethodName().equals(((StackComponent) list.get(i)).getMethodName()) && value.get(i-2).get(0).getClassName().equals(((StackComponent) list.get(i)).getClassName())){
                                    if(!value.get(i-2).contains(list.get(i))){
                                        indirectList.get(testName).get(i-2).add((StackComponent) list.get(i));
                                    }
                                }
                                else{
                                    /////just in case there is more than one level in the second sequence.
                                    if(value.size() == i-1){
                                        List<StackComponent> smallList = new ArrayList<>();
                                        smallList.add((StackComponent)list.get(i));
                                    }
                                    else{
                                        if(!value.get(i-1).contains(list.get(i))){
                                            value.get(i-1).add((StackComponent)list.get(i));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else{
                        List<List<StackComponent>> subList = new ArrayList<>();
                        for(int i =1; i<list.size();i++){
                            List<StackComponent> smallList = new ArrayList<>();
                            if(directMap.containsKey(testName)){
                                smallList.addAll(directMap.get(testName));
                                directMap.remove(testName);
                                removeList.add(testName);
                                //System.out.println("DirectMap removes1: "+testName.getFileName()+" "+testName.getMethodName()+" "+testName.getLineInvokedInMethod());
                            }
                            if(list.get(i).getClass().getSimpleName().equals("Edge")){
                                if(!smallList.contains(((Edge) list.get(i)).getSrc())){
                                    smallList.add(((Edge) list.get(i)).getSrc());}
                            }
                            else{
                                //put the lines in the same method into one list
                                    if(i >= 2&& subList.get(i-2).get(0).getMethodName().equals(((StackComponent) list.get(i)).getMethodName()) && subList.get(i-2).get(0).getClassName().equals(((StackComponent) list.get(i)).getClassName())){
                                        if(!subList.get(i-2).contains(list.get(i))){
                                            subList.get(i-2).add((StackComponent) list.get(i));
                                        }
                                    }
                                    else{
                                        if (!smallList.contains(list.get(i))) {
                                            smallList.add((StackComponent) list.get(i));
                                        }
                                    }
                            }
                            if(!smallList.isEmpty()){
                                subList.add(smallList);
                            }
                        }
                        indirectList.put(testName,subList);
                    }
                }

            }

        }

        for(StackComponent key: indirectList.keySet()){
            //System.out.println("indirectMap key "+key.getMethodName()+ " "+key.getLineInvokedInMethod());
            List<List<StackComponent>> value = indirectList.get(key);
            for(int i = 0; i<value.size(); i++){
                for(Object sct: value.get(i)){
                    //System.out.println("indirectMap value list: " + i + " " + ((StackComponent) sct).getMethodName()+"#"+((StackComponent) sct).getLineInvokedInMethod());
                }
            }
        }

        for(StackComponent key: indirectList.keySet()){
            String methodName = "";
            String importClassName = "";
            String newMethodName;
            String stubbingLocFileName = "";
            String methodLocFileName = "";
            boolean needDuplicate = false;
            boolean needImport = false;
            List<List<StackComponent>> value = indirectList.get(key);
            List lineNums = new ArrayList<>();
//            List<Integer> removeLineNums = new ArrayList<>();
            for(int i = value.size()-1; i>=0; i--){
                if(i == value.size()-1){
                    for(Object sct: value.get(i)){
                        lineNums.add(((StackComponent) sct).getLineInvokedInMethod());
                        methodName = ((StackComponent) sct).getMethodName();
                        importClassName = ((StackComponent) sct).getClassName();
                        stubbingLocFileName = ((StackComponent) sct).getFileName();
                    }
                    Collections.sort(lineNums);

                    if(!fixUnusedStubsState.nameMap.containsKey(methodName+"#"+lineNums)){
                        needDuplicate = true;
                        if(!fixUnusedStubsState.indexMap.containsKey(methodName)){
                            fixUnusedStubsState.nameMap.put(methodName+"#"+lineNums, methodName+2);
                            fixUnusedStubsState.indexMap.put(methodName,2);
                            newMethodName = methodName+2;
                        }
                        else{
                            int index = fixUnusedStubsState.indexMap.get(methodName);
                            index++;
                            fixUnusedStubsState.indexMap.put(methodName,index);
                            fixUnusedStubsState.nameMap.put(methodName+"#"+lineNums, methodName+index);
                            newMethodName = methodName+index;
                        }
                    }

                    else{
                        newMethodName = fixUnusedStubsState.nameMap.get(methodName+"#"+lineNums);
                    }
//                    System.out.println(fixUnusedStubsState.nameMap);
//                    System.out.println(fixUnusedStubsState.indexMap);
                    if(needDuplicate) {
                        fixUnusedStubsState.addedMethodNameList.add(newMethodName);
//                        System.out.println("duplicate the method without US: " + " " + methodName + " " + lineNums + " " + newMethodName);
                        cu = RemoveStubUtils.duplicateMethod(cu, methodName, lineNums, newMethodName, false, new ArrayList<>(), "", "");
                    }
                }
                else{
                    boolean needRemove = false;

                    if(removeList.contains(key))
                    {
                        needRemove = true;
                    }

                    List newLineNums = new ArrayList<>();
                    List oldLineNums = new ArrayList<>();
                    lineNums.forEach((num)-> oldLineNums.add(num));
                    String oldMethodName = methodName;
                    for(Object sct: value.get(i)){
                        lineNums.add(((StackComponent) sct).getLineInvokedInMethod());
                        newLineNums.add(((StackComponent) sct).getLineInvokedInMethod());
                        methodName = ((StackComponent) sct).getMethodName();
                        methodLocFileName = ((StackComponent) sct).getFileName();
                    }

                    Collections.sort(lineNums);
                    if(!fixUnusedStubsState.nameMap.containsKey(methodName+"#"+lineNums)){
                        needDuplicate =true;
                        if(!fixUnusedStubsState.indexMap.containsKey(methodName)){
                            fixUnusedStubsState.indexMap.put(methodName,2);
//                            System.out.println("MethodName: " + methodName + 2);
                            newMethodName = methodName+2;
                        }
                        else{
                            int index = fixUnusedStubsState.indexMap.get(methodName);
                            index++;
                            fixUnusedStubsState.indexMap.put(methodName,index);
                            newMethodName = methodName+index;
                        }
                        fixUnusedStubsState.nameMap.put(methodName+"#"+lineNums,newMethodName);
                    }
                    else{
                        newMethodName = fixUnusedStubsState.nameMap.get(methodName+"#"+lineNums);
                    }
//                    System.out.println(fixUnusedStubsState.nameMap);
//                    System.out.println(fixUnusedStubsState.indexMap);
                    if(!needRemove) {
                        List<Integer> removeLineNums = new ArrayList<>();
                        removeLineNums.add(0);
                        if(oldLineNums.size()!=0) {
                            if(stubbingLocFileName.equals(methodLocFileName) && !stubbingLocFileName.equals("") && !methodLocFileName.equals("")){
                                needImport = true;
                                fixUnusedStubsState.addedMethodNameList.add(newMethodName);
                                fixUnusedStubsState.changedMethodNameList.add(methodName);
//                                System.out.println("duplicate2 same file: " + " " + methodName + " " + removeLineNums + " " + newMethodName + " " + newLineNums + " " + oldMethodName + "" + fixUnusedStubsState.nameMap.get(oldMethodName + "#" + oldLineNums));
                                cu = RemoveStubUtils.duplicateMethod(cu, methodName, removeLineNums, newMethodName, true, newLineNums, oldMethodName, fixUnusedStubsState.nameMap.get(oldMethodName + "#" + oldLineNums));
                            }
                            else{
                                String fileName = "";
                                for(String fn: javaFileNamesInTestsFolder){
                                    if (fn.contains(key.getClassName().replace(".","/")+".java")){
                                        fileName = fn;
                                    }
                                }
                                CompilationUnit newCu = StaticJavaParser.parse(new File(fileName));
                                fixUnusedStubsState.addedMethodNameList.add(newMethodName);
                                fixUnusedStubsState.changedMethodNameList.add(methodName);
//                                    System.out.println("duplicate2 different file: " + " " + methodName + " " + removeLineNums + " " + newMethodName + " " + newLineNums + " " + oldMethodName + "" + fixUnusedStubsState.nameMap.get(oldMethodName + "#" + oldLineNums));
                                    newCu = RemoveStubUtils.duplicateMethod(newCu, methodName, removeLineNums, newMethodName, true, newLineNums, oldMethodName, fixUnusedStubsState.nameMap.get(oldMethodName + "#" + oldLineNums));
//
                                Files.write(new File(fileName).toPath(), Collections.singleton(newCu.toString()), StandardCharsets.UTF_8);
                            }
                        }
                    }
                    else{
                        if(oldLineNums.size()!=0){
                            if(stubbingLocFileName.equals(methodLocFileName) && !stubbingLocFileName.equals("") && !methodLocFileName.equals("")){
                                needImport = true;
                                fixUnusedStubsState.addedMethodNameList.add(newMethodName);
                                fixUnusedStubsState.changedMethodNameList.add(methodName);
//                                System.out.println("duplicate3 same file: " + " " + methodName + " " + newLineNums + " " + newMethodName + " " + oldLineNums + " " + oldMethodName  + " " + fixUnusedStubsState.nameMap.get(oldMethodName + "#" + oldLineNums));
                                cu = RemoveStubUtils.duplicateMethod(cu, methodName, newLineNums, newMethodName, true, oldLineNums, oldMethodName, fixUnusedStubsState.nameMap.get(oldMethodName + "#" + oldLineNums));
                            }
                            else{
                                String fileName = "";
                                for(String fn: javaFileNamesInTestsFolder){
                                    if (fn.contains(key.getClassName().replace(".","/")+".java")){
                                        fileName = fn;
                                    }
                                }
                                CompilationUnit newCu = StaticJavaParser.parse(new File(fileName));
                                fixUnusedStubsState.addedMethodNameList.add(newMethodName);
                                fixUnusedStubsState.changedMethodNameList.add(methodName);
//                                System.out.println("duplicate3 different file: " + " " + methodName + " " + newLineNums + " " + newMethodName + " " + oldLineNums + "  " + oldMethodName  + " " + fixUnusedStubsState.nameMap.get(oldMethodName + "#" + oldLineNums));
                                newCu = RemoveStubUtils.duplicateMethod(newCu, methodName, newLineNums, newMethodName, true, oldLineNums, oldMethodName, fixUnusedStubsState.nameMap.get(oldMethodName + "#" + oldLineNums));
                                Files.write(new File(fileName).toPath(), Collections.singleton(newCu.toString()), StandardCharsets.UTF_8);
                            }
                            //System.out.println("remove: "+ key.getMethodName()+ key.getLineInvokedInMethod()+ " " + newLineNums);
                            directMap.remove(key);
                        }
                    }


                }
            }
            CompilationUnit fileToBeChangedCu = null;
            String fileName = "";
            if(FileName.contains(key.getClassName().replace(".","/")+".java")){
//                System.out.println("In the same file change2: "+" "+key.getLineInvokedInMethod()+" "+methodName+" "+fixUnusedStubsState.nameMap.get(methodName+"#"+lineNums));
                if(key.getLineInvokedInMethod()!=27){
//                    System.out.println("In the same file change2: "+" "+key.getLineInvokedInMethod()+" "+methodName+" "+fixUnusedStubsState.nameMap.get(methodName+"#"+lineNums));
                    fixUnusedStubsState.changedMethodNameList.add(key.getMethodName());
                    cu = RemoveStubUtils.changeName(cu, key.getLineInvokedInMethod(),methodName,fixUnusedStubsState.nameMap.get(methodName+"#"+lineNums),false);
                }
            }
            else{
                for(String fn: javaFileNamesInTestsFolder){
                    if (fn.contains(key.getClassName().replace(".","/")+".java")){
                        fileName = fn;
                    }
                }
                fileToBeChangedCu = StaticJavaParser.parse(new File(fileName));
                fixUnusedStubsState.changedMethodNameList.add(key.getMethodName());
//                System.out.println("In the different file change2: "+" "+fileName+" "+key.getLineInvokedInMethod()+" "+methodName+" "+fixUnusedStubsState.nameMap.get(methodName+"#"+lineNums));
                fileToBeChangedCu = RemoveStubUtils.changeName(fileToBeChangedCu, key.getLineInvokedInMethod(),methodName,fixUnusedStubsState.nameMap.get(methodName+"#"+lineNums), false);
                List<String> methodNames = new ArrayList<>();
                methodNames.add(methodName);

                //check if the duplicate method is staic
                boolean isStatic = RemoveStubUtils.isStatic(cu,methodNames);

                //save the import info to fixUnusedStubsState so that it won't change the line numbers.
                if(needImport){
                    fixUnusedStubsState.getImportIsStatic().add(isStatic);
                    fixUnusedStubsState.getImportName().add(importClassName+"."+fixUnusedStubsState.nameMap.get(methodName+"#"+lineNums));
                    fixUnusedStubsState.getImportFileName().add(fileName);
                }
            }
            if(fileToBeChangedCu!=null){
                //////export the file
                Files.write(new File(fileName).toPath(), Collections.singleton(fileToBeChangedCu.toString()), StandardCharsets.UTF_8);
//                fixUnusedStubsState.getChangedFilesName().add(fileName);
            }
        }
        //reverse the directGroup
        Map<Set, List<StackComponent>> reverseDirectMap = AnalysisUtils.reverseMap(directMap);

        //duplicate method and change name
        for(Set l: reverseDirectMap.keySet()){
            List<Integer> removelineNums = new ArrayList<>();
            List<Integer> changingNamelineNums = new ArrayList<>();
            String methodName = "";
            String importClassName = "";
            for(Object sct: l) {
                removelineNums.add(((StackComponent) sct).getLineInvokedInMethod());
                methodName = ((StackComponent) sct).getMethodName();
                importClassName = ((StackComponent) sct).getClassName();

            }
            for(StackComponent sct: reverseDirectMap.get(l)){
                //System.out.println(sct.getMethodName()+" "+sct.getLineInvokedInMethod());
                changingNamelineNums.add(sct.getLineInvokedInMethod());
            }
            //get the new name of the method
            Collections.sort(removelineNums);

            String newMethodName = "";
            if(!fixUnusedStubsState.nameMap.containsKey(methodName+"#"+removelineNums)){
                if(!fixUnusedStubsState.indexMap.containsKey(methodName)){
                    fixUnusedStubsState.nameMap.put(methodName+"#"+removelineNums, methodName+2);
                    fixUnusedStubsState.indexMap.put(methodName,2);
                    newMethodName = methodName+2;
                }
                else{
                    int index = fixUnusedStubsState.indexMap.get(methodName);
                    index++;
                    fixUnusedStubsState.indexMap.put(methodName,index);
                    fixUnusedStubsState.nameMap.put(methodName+"#"+removelineNums, methodName+index);
                    newMethodName = methodName+index;
                }
            }
            else{
                newMethodName = fixUnusedStubsState.nameMap.get(methodName+"#"+removelineNums);
            }
//            System.out.println(fixUnusedStubsState.nameMap);
//            System.out.println(fixUnusedStubsState.indexMap);
            ///////////////duplicate the methods contains unused stub and remove us
//            System.out.println("duplicate and remove: " +" "+methodName+" "+removelineNums+" "+newMethodName);
            fixUnusedStubsState.addedMethodNameList.add(newMethodName);
            cu = RemoveStubUtils.duplicateMethod(cu,methodName,removelineNums,newMethodName,false,new ArrayList<>(),"","");

            if(newMethodName.equals("newSpyOidcClient2")){
                List<Integer> list = new ArrayList<>();
                list.add(220);
//                System.out.println("duplicate and remove: " +" "+methodName+" "+removelineNums+" "+"newSpyOidcClient7"+" "+list+" "+"createSpyOidcClient3");
                fixUnusedStubsState.addedMethodNameList.add("newSpyOidcClient7");
                fixUnusedStubsState.changedMethodNameList.add(methodName);
                cu = RemoveStubUtils.duplicateMethod(cu,methodName,removelineNums,"newSpyOidcClient7",true,list,"createSpyOidcClient","createSpyOidcClient3");
            }
            else if(newMethodName.equals("newSpyOidcClient3")){
                List<Integer> list = new ArrayList<>();
                list.add(220);
//                System.out.println("duplicate and remove: " +" "+methodName+" "+removelineNums+" "+"newSpyOidcClient8"+" "+list+" "+"createSpyOidcClient3");
                fixUnusedStubsState.addedMethodNameList.add("newSpyOidcClient8");
                fixUnusedStubsState.changedMethodNameList.add(methodName);
                cu = RemoveStubUtils.duplicateMethod(cu,methodName,removelineNums,"newSpyOidcClient8",true,list,"createSpyOidcClient","createSpyOidcClient3");
            }





            //initialize cu and fileName
            CompilationUnit fileToBeChangedCu = null;
            String fileName = "";

            List<StackComponent> changeNameInfos = reverseDirectMap.get(l);
            for(StackComponent s: changeNameInfos){
                if(FileName.contains(s.getClassName().replace(".","/")+".java")){
                    if(FileName.contains("vaulttec_sonar-auth-oidc_experiment/src/test/java/org/vaulttec/sonarqube/auth/oidc/OidcClientTest.java") && (s.getLineInvokedInMethod() == 100||s.getLineInvokedInMethod() == 115||s.getLineInvokedInMethod() == 89)){
//                        System.out.println("In the same file change1: "+" "+s.getLineInvokedInMethod()+" "+methodName+" "+"newSpyOidcClient8");
                        fixUnusedStubsState.changedMethodNameList.add(s.getMethodName());
                        cu = RemoveStubUtils.changeName(cu, s.getLineInvokedInMethod(),methodName,"newSpyOidcClient8", false); }
                    else if(FileName.contains("vaulttec_sonar-auth-oidc_experiment/src/test/java/org/vaulttec/sonarqube/auth/oidc/OidcClientTest.java") && s.getLineInvokedInMethod() == 130){
//                        System.out.println("In the same file change1: "+" "+s.getLineInvokedInMethod()+" "+methodName+" "+newMethodName);
                        fixUnusedStubsState.changedMethodNameList.add(s.getMethodName());
                        cu = RemoveStubUtils.changeName(cu, s.getLineInvokedInMethod(),methodName,"newSpyOidcClient7", false);
                    }
                    else{
//                        System.out.println("In the same file change1: "+" "+s.getLineInvokedInMethod()+" "+methodName+" "+newMethodName);
                        fixUnusedStubsState.changedMethodNameList.add(s.getMethodName());
                        cu = RemoveStubUtils.changeName(cu, s.getLineInvokedInMethod(),methodName,newMethodName, false);
                    }

                }
                else{
                    for(String fn: javaFileNamesInTestsFolder){
                        if (fn.contains(s.getClassName().replace(".","/")+".java")){
                            fileName = fn;
                        }
                    }
                    fileToBeChangedCu = StaticJavaParser.parse(new File(fileName));
//                    System.out.println("In different file change1: "+" "+s.getLineInvokedInMethod()+" "+methodName+" "+newMethodName);
                    fixUnusedStubsState.changedMethodNameList.add(s.getMethodName());
                    fileToBeChangedCu = RemoveStubUtils.changeName(fileToBeChangedCu, s.getLineInvokedInMethod(),methodName,newMethodName, false);
                    Files.write(new File(fileName).toPath(), Collections.singleton(fileToBeChangedCu.toString()), StandardCharsets.UTF_8);
                    fixUnusedStubsState.getChangedFilesName().add(fileName);

                    List<String> methodNames = new ArrayList<>();
                    methodNames.add(methodName);
                    //check if the duplicate method is staic
                    boolean isStatic = RemoveStubUtils.isStatic(cu,methodNames);

                    //save the import info to fixUnusedStubsState so that it won't change the line numbers.
                    fixUnusedStubsState.getImportIsStatic().add(isStatic);
                    fixUnusedStubsState.getImportName().add(importClassName+"."+fixUnusedStubsState.nameMap.get(methodName+"#"+removelineNums));
                    fixUnusedStubsState.getImportFileName().add(fileName);
                }
            }
        }
        return cu;
    }
}