package edu.xyz.cs.analysis;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import edu.xyz.cs.analysis.model.ComplexityMetric;
import edu.xyz.cs.analysis.model.Metrics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CollectCodeMetricsUtil {

    public static void main(String args[]) throws IOException, InterruptedException {
        Metrics m = getTotalMetrics(readToArray());
//        System.out.println(m.loc+" "+m.numOfFields+" "+m.numOfMethods+" "+m.wmc);
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
        Metrics metric = new Metrics("","","",-1,-1,-1,-1);
        if(metrics.size()!=0){
            metric = new Metrics(metrics.get(0).fileName, "none","none", -1, -1,-1,-1);
            int totalLoc = 0;
            int totalWmc = 0;
            int totalMethodQty = 0;
            int totalFieldQty = 0;

            for(int i = 0; i<metrics.size();i++){
                if(metrics.get(i).type.equals("class")){
                    totalLoc += metrics.get(i).loc;
                }
                else if(metrics.get(i).type.equals("interface")){
                    totalLoc += metrics.get(i).loc;
                }
                else if(metrics.get(i).type.equals("enum")&& !metrics.get(i).className.contains("$")){
                    totalLoc += metrics.get(i).loc;
//                System.out.println("----------------------------------------------------"+metrics.get(i).fileName+"----------------------------------------------------");
                }
//            else{
//                totalLoc -= metrics.get(i).loc;
//            }
                totalFieldQty += metrics.get(i).numOfFields;
                totalMethodQty += metrics.get(i).numOfMethods;
                totalWmc += metrics.get(i).wmc;
            }

            metric.setLoc(totalLoc);
            metric.setNumOfMethods(totalMethodQty);
            metric.setWmc(totalWmc);
            metric.setNumOfFields(totalFieldQty);
        }

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

    public static ComplexityMetric parseReport() throws IOException {
        ComplexityMetric complexityMetric = new ComplexityMetric();
        double cognitiveComplexity = 0;
        int cyclomaticComplexity = 0;
        String path = "genese/complexity/reports/folder-report.html";
        File input = new File(path);
        Document doc = Jsoup.parse(input, "UTF-8");
        Elements ele = doc.getElementsByAttributeValue("class", "fl pad1y space-right2");

        for(String text: ele.eachText()){
            if(text.contains("Cognitive complexity :")){
                String[] chars = text.split(":");
                cognitiveComplexity = Double.parseDouble(chars[1].trim());
                complexityMetric.setCognitiveComplexity(cognitiveComplexity);
            }
            if(text.contains("Cyclomatic complexity :")){
                String[] chars = text.split(":");
                cyclomaticComplexity = Integer.parseInt(chars[1].trim());
                complexityMetric.setCyclomaticComplexity(cyclomaticComplexity);
            }
        }
        System.out.println(complexityMetric.cognitiveComplexity);
        System.out.println(complexityMetric.cyclomaticComplexity);
        return complexityMetric;
    }


    public static ComplexityMetric runComplexityTool(String file) throws IOException, InterruptedException {
        String command = "genese cpx -l java " + file;
        Runtime r = Runtime.getRuntime();
        Process p = r.exec(command);
        p.waitFor();
        return parseReport();
    }
}
