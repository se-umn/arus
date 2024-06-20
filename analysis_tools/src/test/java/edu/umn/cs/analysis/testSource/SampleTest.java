package edu.umn.cs.analysis.testSource;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;
import edu.umn.cs.analysis.model.Metrics;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SampleTest {

    private String str = "Hello World";

    class InnerClass {
        void display() {
            System.out.println(str);
        }
        void printNewLine(){
            System.out.println();
        }
    }

    public static List<Metrics> readToArray() throws IOException {
        String filePath = File.separator + "tmp" + File.separator + "class.csv";
        List<Metrics> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if(!values[0].equals("file")){
                    Metrics metric = new Metrics(values[0],values[1], values[2],Integer.parseInt(values[7]),Integer.parseInt(values[15]),Integer.parseInt(values[25]),Integer.parseInt(values[34]));
                    records.add(metric);
                }
            }
        }
        return records;
    }

    public static Metrics getTotalMetrics(List<Metrics> metrics){
        Metrics metric = new Metrics(metrics.get(0).fileName, "none","none", -1, -1,-1,-1);
        int totalLoc = 0;
        int totalWmc = 0;
        int totalMethodQty = 0;
        int totalFieldQty = 0;

        for(int i = 0; i<metrics.size();i++){
            if(metrics.get(i).type.equals("class")){
                totalLoc += metrics.get(i).loc;
            }
            else{
                totalLoc -= metrics.get(i).loc;
            }
            totalFieldQty += metrics.get(i).numOfFields;
            totalMethodQty += metrics.get(i).numOfMethods;
            totalWmc += metrics.get(i).wmc;
        }

        metric.setLoc(totalLoc);
        metric.setNumOfMethods(totalMethodQty);
        metric.setWmc(totalWmc);
        metric.setNumOfFields(totalFieldQty);
        return metric;
    }
    public static Metrics calculateTheDifference(Metrics original, Metrics modified){
        Metrics metricDiff = new Metrics(original.fileName, original.className, original.type, modified.wmc- original.wmc, modified.numOfMethods- original.numOfMethods, modified.numOfFields - original.numOfFields, modified.loc - original.loc);
        return metricDiff;
    }

    public static void getcsvFiles(String file, String jarPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarPath, file, "true", "0", "True", "/tmp/");
        pb.inheritIO();
        Process process = pb.start();
        process.waitFor();
    }
    public static boolean isUseOutside(String fileName, List<String> methodnames) throws FileNotFoundException {
        boolean result = false;
        CompilationUnit cu = StaticJavaParser.parse(new File(fileName));
        List<MethodCallExpr> l = cu.findAll(MethodCallExpr.class);
        for(MethodCallExpr mce: l){
            if(methodnames.contains(mce.getName().getIdentifier())){
                result = true;
                break;
            }
        }
        return result;
    }

    public static int ifMoreThanOnce(String source, String methodName, String stubMethodName) throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser.parse(new File(source));

        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        int count = 0;
        for (MethodDeclaration md : methodDeclarations) {
            if (md.getName().getIdentifier().equals(methodName)) {
                NodeList<Statement> statements = md.getBody().get().getStatements();
                for(Statement s: statements){
                    if(s.toString().contains(stubMethodName)){
                        count++;
                    }
                }
            }
        }
        return count;
    }

}
