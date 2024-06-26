package edu.umn.cs.analysis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.umn.cs.analysis.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class CollectDepInfo {
  private static final String DEPENDENCY_TREE_FILE_NAME = "umn_dependency_tree.xml";
  private static String REPOS_FILE_NAME = "";
  private static String REPOS_FOLDER = "";
  private static String  RESULTS_FILE_NAME = "";
  private static String  MAVEN_HOME = "";
  private static int REPOS_TO_PROCESS_NUM = 0;
  private static final boolean DELETE_REPOS = true;

  public static void main(String args[]){
    if(args.length!=1){
      System.out.println("usage: ./gradlew clean -PmainClass=edu.umn.cs.analysis.CollectDepInfo run --args=\"config_file_name\"");
      System.exit(1);
    }
    try {
      Properties prop = new Properties();
      //prop.load(new FileInputStream("config/config_mattia_laptop.txt"));
      prop.load(new FileInputStream(args[0]));
      CollectDepInfo.REPOS_FILE_NAME = prop.getProperty("repos_file_name");
      CollectDepInfo.REPOS_FOLDER = prop.getProperty("repos_folder");
      CollectDepInfo.RESULTS_FILE_NAME = prop.getProperty("results_file_name");
      CollectDepInfo.MAVEN_HOME = prop.getProperty("maven_home");
      String reposToProcessNumString = prop.getProperty("repos_to_process");
      if(CollectDepInfo.REPOS_FILE_NAME.equals("") || CollectDepInfo.REPOS_FOLDER.equals("") || CollectDepInfo.RESULTS_FILE_NAME.equals("") || CollectDepInfo.MAVEN_HOME.equals("") || reposToProcessNumString.equals("")){
        System.out.println("Need suitable configuration information");
        System.exit(1);
      }
      if(reposToProcessNumString.equals("NO_LIMIT")){
        CollectDepInfo.REPOS_TO_PROCESS_NUM = Integer.MAX_VALUE;
      }
      else{
        CollectDepInfo.REPOS_TO_PROCESS_NUM = Integer.parseInt(reposToProcessNumString);
      }
      CollectDepInfo pr = new CollectDepInfo();
      pr.processRepos(CollectDepInfo.REPOS_FILE_NAME, CollectDepInfo.RESULTS_FILE_NAME);
    }
    catch(Exception e){
      System.out.println("Error while running experiment");
      e.printStackTrace(System.out);
    }
  }

  public void processRepos(String reposFileName, String resultsFileName){
    Set<String> repoNamesSet = new HashSet<String>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(reposFileName));
      String line = reader.readLine();
      while (line != null) {
        repoNamesSet.add(line.trim());
        line = reader.readLine();
      }
      reader.close();
    }
    catch (Exception e){
      System.out.println("Could not read repos list");
      System.exit(1);
    }
    System.out.println("Repos="+repoNamesSet.size());

    //read results file
    JsonObject resultsJson = null;
    try {
      BufferedReader br = new BufferedReader(new FileReader(resultsFileName));
      JsonElement resultsElement = JsonParser.parseReader(br);
      if(resultsElement.isJsonObject()){
        resultsJson = (JsonObject) resultsElement;
      }
      else{
        JsonArray resultsJsonArray = new JsonArray();
        resultsJson = new JsonObject();
        resultsJson.add("results", resultsJsonArray);
      }
      br.close();
    }
    catch(Exception e){
      System.out.println("Could not read current json results");
      System.exit(1);
    }
    JsonArray resultsJsonArray = resultsJson.getAsJsonArray("results");
    System.out.println("Results="+resultsJsonArray.size());
    Set<String> initiallyAnalyzedRepoNames = new HashSet<String>();
    for(int i=0; i<resultsJsonArray.size(); ++i){
      JsonObject repoAnalysis = resultsJsonArray.get(i).getAsJsonObject();
      initiallyAnalyzedRepoNames.add(repoAnalysis.get("repo_name").getAsString());
    }

    //process repos
    int count = 0;
    for(String repoName:repoNamesSet){
      if(count>= CollectDepInfo.REPOS_TO_PROCESS_NUM){
        break;
      }
      if(repoName.equals("Aegeaner/kafka-connector-mysql")){
        continue;
      }
      count++;
      if(isAlreadyAnalyzed(initiallyAnalyzedRepoNames, repoName)){
        System.out.println(count+"#processed#"+repoName);
        continue;
      }
      String repoDiskName = repoName.replaceAll("/","_");
      String repoDiskNameTag = repoName.replaceAll("/","_")+"_tag";
      System.out.println(count+"#processing#"+repoName);
      CollectedDepInfo latestCommitAnalysisResults = analyzeRepo(false, repoName, repoDiskName);
      CollectedDepInfo latestTagAnalysisResults = null;
      //optimize clone based on whether there are tags
      if(latestCommitAnalysisResults.getTagsCount()==0){
        latestTagAnalysisResults = new CollectedDepInfo();
      } else {
        latestTagAnalysisResults = analyzeRepo(true, repoName, repoDiskNameTag);
      }
      JsonObject repoAnalysisResult = createJsonObject(repoName, latestCommitAnalysisResults, latestTagAnalysisResults);
      resultsJsonArray.add(repoAnalysisResult);
      saveResults(resultsJsonArray);
    }
  }

  private boolean isAlreadyAnalyzed(Set<String> analyzedRepoNames, String repoName){
    boolean analyzed = false;
    if(analyzedRepoNames.contains(repoName)){
      analyzed = true;
    }
    return analyzed;
  }

  private CollectedDepInfo analyzeRepo(boolean isTagAnalysis, String repoName, String repoDiskName){
    CollectedDepInfo result = new CollectedDepInfo();
    try {
      String currRepoFolderName = CollectDepInfo.REPOS_FOLDER+File.separator+repoDiskName;
      File testRepo = new File(currRepoFolderName);
      Git git = Git.cloneRepository()
          .setURI("https://github.com/"+repoName+".git")
          .setDirectory(testRepo)
          .setTimeout(600)
          .call();
      List<Ref> tagsJGit = git.tagList().call();
      result.setTagsCount(tagsJGit.size());
      if(isTagAnalysis){
        String commitIdOfLatestTag = "";
        List<Tag> tags = new ArrayList<Tag>();
        for(Ref tagJGit:tagsJGit){
          String currTagName = tagJGit.getName();
          String currTagCommitId = tagJGit.getObjectId().getName();
          if(tagJGit.getPeeledObjectId()!=null){
            currTagName = tagJGit.getName();
            currTagCommitId = tagJGit.getPeeledObjectId().getName();
          }
          for (RevCommit commit : git.log().call()) {
            String currCommitId = commit.getName();
            if(currTagCommitId.equals(currCommitId)){
              int commitTime = commit.getCommitTime();
              ZonedDateTime dateTime = Instant.ofEpochSecond(commitTime)
                  .atZone(ZoneId.of("America/Chicago"));
              tags.add(new Tag(currTagName, currTagCommitId, dateTime));
              break;
            }
          }
        }
        Collections.sort(tags, new Comparator<Tag>() {
          @Override
          public int compare(Tag t1, Tag t2) {
            if(t1.getCommitTime().isAfter(t2.getCommitTime())){
              return -1;
            } else if (t1.getCommitTime().isBefore(t2.getCommitTime())) {
              return  1;
            }
            else{
              return 0;
            }
          }
        });
        if(tagsJGit.size()!= tags.size()){
          System.out.println("Potential problem with tags "+tagsJGit.size() + "vs" + tags.size());
        }
        if(tags.size()>0){
          commitIdOfLatestTag = tags.get(0).getCommitId();
          System.out.println("TAG:"+tags.get(0).getName()+"#"+tags.get(0).getCommitId());
        }
        if(commitIdOfLatestTag.equals("")){
          if(CollectDepInfo.DELETE_REPOS) {
            FileUtils.deleteDirectory(new File(currRepoFolderName));
          }
          return result;
        }
        RevCommit commitOfTag = null;
        for (RevCommit commit : git.log().call()) {
          String currCommitId = commit.getName();
          if(currCommitId.equals(commitIdOfLatestTag)) {
            int commitTime = commit.getCommitTime();
            ZonedDateTime dateTime = Instant.ofEpochSecond(commitTime)
                .atZone(ZoneId.of("America/Chicago"));
            result.setCommit(new Commit(currCommitId, dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth()));
            commitOfTag = commit;
            break;
          }
        }
        if(commitOfTag==null){
          if(CollectDepInfo.DELETE_REPOS) {
            FileUtils.deleteDirectory(new File(currRepoFolderName));
          }
          return result;
        }
        git.checkout().setName(commitIdOfLatestTag).call();
      } else {
        for (RevCommit commit : git.log().call()) {
          int commitTime = commit.getCommitTime();
          ZonedDateTime dateTime = Instant.ofEpochSecond(commitTime)
              .atZone(ZoneId.of("America/Chicago"));
          result.setCommit(new Commit(commit.getName(), dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth()));
          break;
        }
      }

      //check if the project is single repo
      int pomFilesCount = findFileOccurrences(currRepoFolderName, "pom.xml");
      result.setPomFilesCount(pomFilesCount);
      if(pomFilesCount!=1) {
        //save results
        if(CollectDepInfo.DELETE_REPOS) {
          FileUtils.deleteDirectory(new File(currRepoFolderName));
        }
        return result;
      }
      String mavenWorkingDirInRepo = findFileMavenWorkingDirectoryInRepo(currRepoFolderName,"pom.xml");
      result.setMavenWorkingDirInRepo(mavenWorkingDirInRepo);
      boolean gotDependencyTree = MavenUtils.runMavenDependencyTree(currRepoFolderName+mavenWorkingDirInRepo+File.separator+"pom.xml", CollectDepInfo.MAVEN_HOME, CollectDepInfo.DEPENDENCY_TREE_FILE_NAME);
      result.setGotDependencyTree(gotDependencyTree);
      if(!gotDependencyTree){
        if(CollectDepInfo.DELETE_REPOS) {
          FileUtils.deleteDirectory(new File(currRepoFolderName));
        }
        return result;
      }
      String dependencyTreeFileName = currRepoFolderName+mavenWorkingDirInRepo+File.separator+DEPENDENCY_TREE_FILE_NAME;
      System.out.println("DD:###############################");
      Set<String> rootDeps = getRootDependencies(dependencyTreeFileName);
      result.setRootDeps(rootDeps);
      System.out.println("AD:###############################");
      Set<String> allDeps = getAllDependencies(dependencyTreeFileName);
      result.setAllDeps(allDeps);

      boolean junit = MavenUtils.checkForDependency(dependencyTreeFileName, MavenUtils.SCOPES, "junit");
      boolean junitJupiter = MavenUtils.checkForDependency(dependencyTreeFileName, MavenUtils.SCOPES, "org.junit.jupiter");
      boolean usesJunit = junit || junitJupiter;
      result.setUsesJunit(usesJunit);
      if(!usesJunit){
        if(CollectDepInfo.DELETE_REPOS) {
          FileUtils.deleteDirectory(new File(currRepoFolderName));
        }
        return result;
      }

      //run test cases n times
      List<TestExecutionResult> testExecutionResults = MavenUtils.runTestsWithMaven(CollectDepInfo.MAVEN_HOME, currRepoFolderName+mavenWorkingDirInRepo+File.separator+"pom.xml",
          currRepoFolderName+mavenWorkingDirInRepo+File.separator+"target"+File.separator+"surefire-reports",
          1, new ArrayList<String>());
      result.setTestExecutionResults(testExecutionResults);
      //delete working dir
      if(CollectDepInfo.DELETE_REPOS) {
        FileUtils.deleteDirectory(new File(currRepoFolderName));
      }
      return result;
    }
    catch (TransportException e){
      System.out.println("Exception while cloning repo");
      e.printStackTrace(System.out);
      result.setCloneException(true);
      return result;
    }
    catch (Exception e){
      System.out.println("Exception while processing report");
      e.printStackTrace(System.out);
      result.setOtherException(true);
      return result;
    }
  }

  private int findFileOccurrences(String rootFolder, String fileName){
    int occurrences = 0;
    List<String> workList = new ArrayList<String>();
    workList.add(rootFolder);
    while(!workList.isEmpty()){
      String currFileName = workList.remove(0);
      File currFile = new File(currFileName);
      if(currFile.isDirectory()){
        File containedFiles[] = currFile.listFiles();
        for(File containedFile:containedFiles){
          workList.add(containedFile.getAbsolutePath());
        }
      } else {
        if(currFile.getName().equals(fileName)){
          occurrences++;
        }
      }
    }
    return occurrences;
  }

  private String findFileMavenWorkingDirectoryInRepo(String rootFolder, String fileName){
    String mavenWorkingDirectoryInRepo = "";
    List<String> workList = new ArrayList<String>();
    workList.add(rootFolder);
    while(!workList.isEmpty()){
      String currFileName = workList.remove(0);
      File currFile = new File(currFileName);
      if(currFile.isDirectory()){
        File containedFiles[] = currFile.listFiles();
        for(File containedFile:containedFiles){
          workList.add(containedFile.getAbsolutePath());
        }
      } else {
        if(currFile.getName().equals(fileName)){
          mavenWorkingDirectoryInRepo = currFile.getParentFile().getAbsolutePath();
          mavenWorkingDirectoryInRepo = mavenWorkingDirectoryInRepo.replace(rootFolder, "");
          break;
        }
      }
    }
    return mavenWorkingDirectoryInRepo;
  }

  private Set<String> getRootDependencies(String graphFileName){
    Set<String> deps = new HashSet<String>();
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
        return deps;
      }
      else{
        Iterator<Edge> outgoingEdgesIterator = rootVertex.edges(Direction.OUT);
        while(outgoingEdgesIterator.hasNext()){
          Edge outgoingEdge = outgoingEdgesIterator.next();
          String targetId = outgoingEdge.inVertex().id().toString();
          String label = MavenUtils.getLabel(graphFileName, targetId);
          deps.add(label);
          System.out.println(label);
        }
      }
    }
    catch (Exception e){
      System.out.println("Exception while getting root dependencies");
      e.printStackTrace(System.out);
    }
    return deps;
  }

  private Set<String> getAllDependencies(String graphFileName){
    Set<String> deps = new HashSet<String>();
    try{
      Graph dependencyGraph = TinkerGraph.open();
      dependencyGraph.traversal().io(graphFileName).read().iterate();
      Iterator<Vertex> vertexIterator = dependencyGraph.vertices();
      while(vertexIterator.hasNext()){
        Vertex vertex = vertexIterator.next();
        String vertexId = vertex.id().toString();
        String label = MavenUtils.getLabel(graphFileName, vertexId);
        deps.add(label);
        System.out.println(label);
      }
    }
    catch (Exception e){
      System.out.println("Exception while getting all dependencies");
      e.printStackTrace(System.out);
    }
    return deps;
  }

  private JsonObject createJsonObject(String repoName,
                                      CollectedDepInfo analysisResultsLatestCommit,
                                      CollectedDepInfo analysisResultsLatestTag){
    JsonObject repoJson = new JsonObject();
    repoJson.addProperty("repo_name", repoName);
    repoJson.add("latest_commit_analysis_results", analysisResultsLatestCommit.toJson());
    repoJson.add("latest_tag_analysis_results", analysisResultsLatestTag.toJson());
    return repoJson;
  }

  private void saveResults(JsonArray resultsArray){
    try {
      JsonObject resultsJson = new JsonObject();
      resultsJson.add("results", resultsArray);
      FileWriter resultsFileWriter = new FileWriter(CollectDepInfo.RESULTS_FILE_NAME);
      resultsFileWriter.write(resultsJson.toString());
      resultsFileWriter.close();
    }
    catch(Exception e){
      System.out.println("Could not save results to file");
      System.exit(1);
    }
  }
}
