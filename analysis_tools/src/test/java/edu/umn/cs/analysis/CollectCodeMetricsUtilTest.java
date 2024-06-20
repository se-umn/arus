package edu.umn.cs.analysis;

import edu.umn.cs.analysis.model.ComplexityMetric;
import edu.umn.cs.analysis.model.Metrics;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CollectCodeMetricsUtilTest{
    @Test
    public void testParseReportValidFile() throws IOException, InterruptedException {
        ComplexityMetric result = CollectCodeMetricsUtil.runComplexityTool("src/test/java/edu/umn/cs/analysis/testSource/");
        assertEquals(90.3, result.cognitiveComplexity, 0.1);
        assertEquals(14, result.cyclomaticComplexity,0.1);
    }

    @Test(expected = IOException.class)
    public void testParseReportFileNotFound() throws IOException, InterruptedException {
        CollectCodeMetricsUtil.runComplexityTool("src/test/java/edu/umn/cs/analysis/testSources/"); // should throw IOException
    }

    @Test
    public void testGetCSVFilesSuccess() throws IOException, InterruptedException {
        File file = new File("/tmp/class.csv");
        if(file.exists()){
            file.delete();
        }
        CollectCodeMetricsUtil.getcsvFiles("src/test/java/edu/umn/cs/analysis/testSource/", "src/test/java/edu/umn/cs/analysis/testSource/ck-0.7.1-SNAPSHOT-jar-with-dependencies.jar");
        assertEquals(true, file.exists());
    }

    @Test
    public void testgetTotalMetrics() throws IOException {
        Metrics result = CollectCodeMetricsUtil.getTotalMetrics(CollectCodeMetricsUtil.readToArray());
        assertEquals(79, result.loc);
        assertEquals(18, result.wmc);
        assertEquals(8, result.numOfMethods);
        assertEquals(1,result.numOfFields);
    }

}
