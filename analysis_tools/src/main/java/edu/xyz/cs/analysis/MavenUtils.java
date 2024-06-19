package edu.xyz.cs.analysis;

import com.google.gson.JsonArray;
import edu.xyz.cs.analysis.model.TestExecutionResult;
import org.apache.maven.plugin.surefire.log.api.NullConsoleLogger;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.SurefireReportParser;
import org.apache.maven.shared.invoker.*;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MavenUtils {

    public final static Set<String> SCOPES = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList("test","compile")));

    public static List<TestExecutionResult> runTestsWithMaven(String mavenHome, String pomFile, String surfireReportDirectory, int repetitions, List<String> options){
        List<TestExecutionResult> results = new ArrayList<TestExecutionResult>();
        for(int i=0; i<repetitions;++i) {
            try {
                InvocationRequest request = new DefaultInvocationRequest();
                request.setPomFile(new File(pomFile));
                List<String> goals = new ArrayList<String>();
                goals.add("clean");
                goals.add("test");
                for(String option:options){
                    goals.add(option);
                }
                request.setGoals(goals);
                request.setTimeoutInSeconds(600);
                Invoker invoker = new DefaultInvoker();
                invoker.setMavenHome(new File(mavenHome));
                invoker.setOutputHandler(null);
                InvocationResult result = invoker.execute(request);
                if (result.getExitCode() != 0) {
                    results.add(new TestExecutionResult(false, 0, 0, 0, 0));
                    continue;
                }
                //parse xml files to get test execution results
                File surfireReportDirectoryFile = new File(surfireReportDirectory);
                SurefireReportParser surefireReportParser = new SurefireReportParser(
                        Collections.singletonList(surfireReportDirectoryFile), Locale.ENGLISH, new NullConsoleLogger());
                List<ReportTestSuite> reportTestSuites = surefireReportParser.parseXMLReportFiles();
                Map<String, String> summary = surefireReportParser.getSummary(reportTestSuites);
                results.add(new TestExecutionResult(true,
                        Integer.parseInt(summary.get("totalTests")),
                        Integer.parseInt(summary.get("totalFailures")),
                        Integer.parseInt(summary.get("totalErrors")),
                        Integer.parseInt(summary.get("totalSkipped"))));

            } catch (MavenInvocationException mie) {
                results.add(new TestExecutionResult(false, 0, 0, 0, 0));
                System.out.println("Check exception while running tests");
                mie.printStackTrace(System.out);
                continue;
            }
            catch (Exception e) {
                results.add(new TestExecutionResult(false, 0, 0, 0, 0));
                System.out.println("Exception while running tests");
                e.printStackTrace(System.out);
                continue;
            }
        }
        return results;
    }

    public static boolean runMavenDependencyTree(String pomFile, String mavenHome, String dependencyTreeFileName){
        boolean succeeded = false;
        try{
            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(new File(pomFile));
            request.setGoals(Arrays.asList( "clean", "dependency:tree -DoutputFile="+dependencyTreeFileName+" -DoutputType=graphml"));
            request.setTimeoutInSeconds(600);
            Invoker invoker = new DefaultInvoker();
            invoker.setMavenHome(new File(mavenHome));
            invoker.setOutputHandler(null);
            InvocationResult result = invoker.execute(request);
            if(result.getExitCode()==0){
                succeeded = true;
            }
        }
        catch (MavenInvocationException mie) {
            System.out.println("ERROR: Check exception while running maven dependency tree");
            mie.printStackTrace(System.out);
        }
        catch(Exception e){
            System.out.println("ERROR: Exception while running maven dependency tree");
            e.printStackTrace(System.out);
        }
        return succeeded;
    }

    public static boolean checkForDependency(String graphFileName, Set<String> scopes, String dependencyGroupId){
        boolean hasDependency = false;
        try{
            Graph dependencyGraph = TinkerGraph.open();
            dependencyGraph.traversal().io(graphFileName).read().iterate();
            Iterator<Vertex> vertexIterator = dependencyGraph.vertices();
            int rootCount = 0;
            Vertex rootVertex = null;
            while(vertexIterator.hasNext()){
                Vertex vertex = vertexIterator.next();
                Iterator<Edge> incomingEdgesIterator = vertex.edges(Direction.IN);
                int incomingEdgesCount = 0;
                while(incomingEdgesIterator.hasNext()){
                    incomingEdgesIterator.next();
                    incomingEdgesCount++;
                }
                if(incomingEdgesCount==0){
                    //root vertex
                    rootVertex = vertex;
                    rootCount++;
                }
            }
            if(rootCount!=1){
                System.out.println("Multiple roots in dependency tree");
                return hasDependency;
            }
            else{
                Iterator<Edge> outgoingEdgesIterator = rootVertex.edges(Direction.OUT);
                while(outgoingEdgesIterator.hasNext()){
                    Edge outgoingEdge = outgoingEdgesIterator.next();
                    String targetId = outgoingEdge.inVertex().id().toString();
                    String label = getLabel(graphFileName, targetId);
                    String labelArray[] = label.split(":");
                    if(labelArray.length==5){
                        if(labelArray[0].equals(dependencyGroupId) && scopes.contains(labelArray[4])){
                            hasDependency = true;
                            return hasDependency;
                        }
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println("ERROR: Exception checking for dependency");
            e.printStackTrace(System.out);
        }
        return hasDependency;
    }

    public static Set<String> getLabelsForDependency(String graphFileName, Set<String> scopes, String dependencyGroupId){
        Set<String> resultLabels = new HashSet<String>();
        try{
            Graph dependencyGraph = TinkerGraph.open();
            dependencyGraph.traversal().io(graphFileName).read().iterate();
            Iterator<Vertex> vertexIterator = dependencyGraph.vertices();
            int rootCount = 0;
            Vertex rootVertex = null;
            while(vertexIterator.hasNext()){
                Vertex vertex = vertexIterator.next();
                Iterator<Edge> incomingEdgesIterator = vertex.edges(Direction.IN);
                int incomingEdgesCount = 0;
                while(incomingEdgesIterator.hasNext()){
                    incomingEdgesIterator.next();
                    incomingEdgesCount++;
                }
                if(incomingEdgesCount==0){
                    //root vertex
                    rootVertex = vertex;
                    rootCount++;
                }
            }
            if(rootCount!=1){
                return resultLabels;
            }
            else{
                Set<Vertex> visited = new HashSet<Vertex>();
                List<Vertex> workList = new ArrayList<Vertex>();
                workList.add(rootVertex);
                while(!workList.isEmpty()) {
                    Vertex currVertex = workList.remove(0);
                    Iterator<Edge> outgoingEdgesIterator = currVertex.edges(Direction.OUT);
                    while (outgoingEdgesIterator.hasNext()) {
                        Edge outgoingEdge = outgoingEdgesIterator.next();
                        Vertex nextVertex = outgoingEdge.inVertex();
                        String targetId = nextVertex.id().toString();
                        String label = MavenUtils.getLabel(graphFileName, targetId);
                        String labelArray[] = label.split(":");
                        if (labelArray.length == 5) {
                            if (labelArray[0].equals(dependencyGroupId) && scopes.contains(labelArray[4])) {
                                resultLabels.add(label);
                            }
                        }
                        if(!visited.contains(nextVertex)){
                            visited.add(nextVertex);
                            workList.add(nextVertex);
                        }
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println("Exception while getting label for dependency");
            e.printStackTrace(System.out);
        }
        return resultLabels;
    }

    public static String getLabel(String graphFileName, String targetId){
        String label = "";
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            String xmlContent = new String (Files.readAllBytes(Paths.get(graphFileName)));
            Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
            Element rootElement = document.getDocumentElement();
            NodeList graphNodeList = rootElement.getElementsByTagName("graph");
            if(graphNodeList.getLength()!=1){
                return label;
            }

            Node graphNode = graphNodeList.item(0);
            if(!(graphNode instanceof Element)){
                return label;
            }
            Element graphElement = (Element) graphNode;
            NodeList nodeNodeList = graphElement.getElementsByTagName("node");
            for(int i=0; i<nodeNodeList.getLength(); ++i){
                Node nodeNode = nodeNodeList.item(i);
                if(nodeNode instanceof Element){
                    Element nodeElement = (Element) nodeNode;
                    if(nodeElement.hasAttribute("id")){
                        if(nodeElement.getAttribute("id").equals(targetId)){
                            label = nodeElement.getTextContent();
                            return label;
                        }
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println("ERROR: Exception while getting label from graphml xml file");
            e.printStackTrace(System.out);
        }
        return  label;
    }
}
