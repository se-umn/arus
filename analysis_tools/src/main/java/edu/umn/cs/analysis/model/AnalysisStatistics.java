package edu.umn.cs.analysis.model;

import com.google.gson.JsonObject;

public class AnalysisStatistics {
    private long totalAnalysisTime;
    private long repoCloneTime;
    private int testExecutionRepetitions;
    private long originalTestsTime;
    private int originalTestsPassedCount;
    private long customizedMockitoTestsTime;
    private long originalMockitoTestsTime;
    private int customizedMockitoTestsPassedCount;
    private int originalMockitoTestsPassedCount;
    private int totalTestCount;
    private long testsTimeAfterThenReturnToDoReturnChanges;
    private long thenReturnToDoReturnChangesCount;
    private int USDB, USDA, USOB, USOA, US1COUNT, US2COUNT, US3COUNT, USAT_S, UUSO;
    private long customizedMockitoSanityCheckTestsTime;
    private long modificationExecutionTime;
    private int insertIfStatementsCount;
    private int addedTestCount;
    private ComplexityMetric complexityMetricDifference, originTestComplexityMetric, experimentTestComplexityMetric, srcOriginalComplexityMetric, beforeModificationComplexityMetric, BMChangedFilesCpxMetrics, expChangedFilesCpxMetrics;
    private Metrics middleDiff, modifiedDiff, oriSrcCM, oriTestCM, BMTestCM, expTestCM, BMchangedFilesCM,expChangedFilesCM;
    private int changedMethodsCount, addedMethodsCount, changedFileCount, addedFileCount;

    public AnalysisStatistics(){
        this.totalAnalysisTime = -1;
        this.repoCloneTime = -1;
        this.testExecutionRepetitions = -1;
        this.originalTestsTime = -1;
        this.originalTestsPassedCount = -1;
        this.totalTestCount = -1;
        this.customizedMockitoTestsTime = -1;
        this.customizedMockitoTestsPassedCount = -1;
        this.originalMockitoTestsTime = -1;
        this.originalMockitoTestsPassedCount = -1;
        this.testsTimeAfterThenReturnToDoReturnChanges = -1;
        this.thenReturnToDoReturnChangesCount = -1;
        this.USDB = 0;
        this.USDA = 0;
        this.USOB = 0;
        this.USOA = 0;
        this.US1COUNT = 0;
        this.US2COUNT = 0;
        this.US3COUNT = 0;
        this.USAT_S = 0;
        this.UUSO = 0;
        this.changedFileCount = 0;
        this.addedMethodsCount = 0;
        this.changedMethodsCount = 0;
        this.addedFileCount = 0;
        this.customizedMockitoSanityCheckTestsTime = -1;
        this.modificationExecutionTime = -1;
        this.insertIfStatementsCount = -1;
        this.addedTestCount = 0;
        this.BMChangedFilesCpxMetrics = new ComplexityMetric();
        this.expChangedFilesCpxMetrics = new ComplexityMetric();
        this.complexityMetricDifference = new ComplexityMetric();
        this.originTestComplexityMetric = new ComplexityMetric();
        this.srcOriginalComplexityMetric = new ComplexityMetric();
        this.experimentTestComplexityMetric = new ComplexityMetric();
        this.beforeModificationComplexityMetric = new ComplexityMetric();
        this.middleDiff = new Metrics("","","", -1,-1,-1,-1);
        this.modifiedDiff = new Metrics("","","", -1,-1,-1,-1);
        this.oriSrcCM = new Metrics("","","", -1,-1,-1,-1);
        this.oriTestCM = new Metrics("","","", -1,-1,-1,-1);
        this.BMTestCM = new Metrics("","","", -1,-1,-1,-1);
        this.expTestCM = new Metrics("","","", -1,-1,-1,-1);
        this.BMchangedFilesCM = new Metrics("","","", -1,-1,-1,-1);
        this.expChangedFilesCM = new Metrics("","", "",-1,-1,-1,-1);
   }

    public JsonObject toJson(){
        JsonObject result = new JsonObject();
        result.addProperty("total_analysis_time", totalAnalysisTime);
        result.addProperty("repo_clone_time", repoCloneTime);
        result.addProperty("test_execution_repetitions", testExecutionRepetitions);
        result.addProperty("original_tests_time", originalTestsTime);
        result.addProperty("original_tests_passed_count", originalTestsPassedCount);
        result.addProperty("total_test_count", totalTestCount);
        result.addProperty("customized_mockito_tests_time", customizedMockitoTestsTime);
        result.addProperty("customized_mockito_tests_passed_count", customizedMockitoTestsPassedCount);
        result.addProperty("original_mockito_tests_time", originalMockitoTestsTime);
        result.addProperty("original_mockito_tests_passed_count", originalMockitoTestsPassedCount);
        result.addProperty("tests_time_after_then_return_to_doReturn_changes", testsTimeAfterThenReturnToDoReturnChanges);
        result.addProperty("then_return_to_do_return_changes_count", thenReturnToDoReturnChangesCount);
        result.addProperty("USDB", USDB);
        result.addProperty("USDA", USDA);
        result.addProperty("USOB", USOB);
        result.addProperty("USOA", USOA);
        result.addProperty("U_T", US1COUNT);
        result.addProperty("UUAT", US2COUNT);
        result.addProperty("UUWT", US3COUNT);
        result.addProperty("UUAT_S", USAT_S);
        result.addProperty("UUSO", UUSO);
        result.addProperty("Added_Files_Count", addedFileCount);
        result.addProperty("Added_methods_Count", addedMethodsCount);
        result.addProperty("Changed_Files_Count", changedFileCount);
        result.addProperty("Changed_Methods_Count", changedMethodsCount);
        result.addProperty("customized_mockito_sanity_check_tests_time", customizedMockitoSanityCheckTestsTime);
        result.addProperty("modification_execution_time", modificationExecutionTime);
        result.addProperty("insert_if_statement_count", insertIfStatementsCount);
        result.add("original_test_complexity_metrics", originTestComplexityMetric.toJson());
        result.add("experimental_test_complexity", experimentTestComplexityMetric.toJson());
        result.add("origin_src_complexity", srcOriginalComplexityMetric.toJson());
        result.add("before_modification_complexity", beforeModificationComplexityMetric.toJson());
        result.add("complexity_difference", complexityMetricDifference.toJson());
        result.add("before_test_Diff", middleDiff.toJson());
        result.add("after_test_diff", modifiedDiff.toJson());
        result.add("original_src_code_metrics", oriSrcCM.toJson());
        result.add("original_test_code_metrics", oriTestCM.toJson());
        result.add("before_modification_test_code_metrics", BMTestCM.toJson());
        result.add("experiment_test_code_metrics", expTestCM.toJson());
        result.add("before_modification_changed_files_code_metrics", BMchangedFilesCM.toJson());
        result.add("experiment_changed_files_code_metrics", expChangedFilesCM.toJson());
//        result.add("before_modification_changed_files_complexity", BMChangedFilesCpxMetrics.toJson());
//        result.add("experiment_changed_files_code_complexity", expChangedFilesCpxMetrics.toJson());

        return result;
    }

    public void setTotalAnalysisTime(long totalAnalysisTime) {
        this.totalAnalysisTime = totalAnalysisTime;
    }

    public void setRepoCloneTime(long repoCloneTime) {
        this.repoCloneTime = repoCloneTime;
    }

    public void setTestExecutionRepetitions(int testExecutionRepetitions) {
        this.testExecutionRepetitions = testExecutionRepetitions;
    }

    public void setMiddleDiff(Metrics middleDiff) {
        this.middleDiff = middleDiff;
    }


    public void setModifiedDiff(Metrics modifiedDiff) {
        this.modifiedDiff = modifiedDiff;
    }

    public void setOriginalTestsTime(long originalTestsTime) {
        this.originalTestsTime = originalTestsTime;
    }

    public void setOriginalTestsPassedCount(int originalTestsPassedCount) {
        this.originalTestsPassedCount = originalTestsPassedCount;
    }

    public void setTotalTestCount(int totalTestCount) {
        this.totalTestCount = totalTestCount;
    }

    public void setCustomizedMockitoTestsTime(long customizedMockitoTestsTime) {
        this.customizedMockitoTestsTime = customizedMockitoTestsTime;
    }

    public void setCustomizedMockitoTestsPassedCount(int customizedMockitoTestsPassedCount) {
        this.customizedMockitoTestsPassedCount = customizedMockitoTestsPassedCount;
    }

    public void setTestsTimeAfterThenReturnToDoReturnChanges(long testsTimeAfterThenReturnToDoReturnChanges) {
        this.testsTimeAfterThenReturnToDoReturnChanges = testsTimeAfterThenReturnToDoReturnChanges;
    }

    public void setThenReturnToDoReturnChangesCount(long thenReturnToDoReturnChangesCount) {
        this.thenReturnToDoReturnChangesCount = thenReturnToDoReturnChangesCount;
    }

    public void setCustomizedMockitoSanityCheckTestsTime(long customizedMockitoSanityCheckTestsTime) {
        this.customizedMockitoSanityCheckTestsTime = customizedMockitoSanityCheckTestsTime;
    }

    public void setModificationExecutionTime(long modificationExecutionTime){
        this.modificationExecutionTime = modificationExecutionTime;
    }

    public void setInsertIfStatementsCount(int insertIfStatementsCount) {
        this.insertIfStatementsCount = insertIfStatementsCount;
    }

    public void setComplexityMetricDifference(ComplexityMetric complexityMetricDifference) {
        this.complexityMetricDifference = complexityMetricDifference;
    }

    public void setExperimentTestComplexityMetric(ComplexityMetric experimentTestComplexityMetric) {
        this.experimentTestComplexityMetric = experimentTestComplexityMetric;
    }

    public void setOriginTestComplexityMetric(ComplexityMetric originTestComplexityMetric) {
        this.originTestComplexityMetric = originTestComplexityMetric;
    }

    public void setSrcOriginComplexity(ComplexityMetric srcOriginalComplexityMetric){
        this.srcOriginalComplexityMetric = srcOriginalComplexityMetric;
    }

    public void setOriSrcCM(Metrics oriSrcCM){
        this.oriSrcCM = oriSrcCM;
    }

    public void setOriTestCM(Metrics oriTestCM){
        this.oriTestCM = oriTestCM;
    }

    public void setExpTestCM(Metrics expTestCM){
        this.expTestCM = expTestCM;
    }

    public void setBMTestCM(Metrics BMTestCM){
        this.BMTestCM = BMTestCM;
    }
    public void setBeforeModificationComplexityMetric(ComplexityMetric beforeModificationComplexityMetric) {
        this.beforeModificationComplexityMetric = beforeModificationComplexityMetric;
    }

    public void setOriginalMockitoTestsTime(long originalMockitoTestsTime) {
        this.originalMockitoTestsTime = originalMockitoTestsTime;
    }

    public void setOriginalMockitoTestsPassedCount(int originalMockitoTestsPassedCount) {
        this.originalMockitoTestsPassedCount = originalMockitoTestsPassedCount;
    }

    public void setAddedTestCount(int addedTestCount) {
        this.addedTestCount = addedTestCount;
    }

    public int getAddedTestCount(){
        return this.addedTestCount;
    }

    public void setUSDB(int USDB) {
        this.USDB = USDB;
    }

    public void setUSDA(int USDA) {
        this.USDA = USDA;
    }

    public void setUS1COUNT(int US1COUNT) {
        this.US1COUNT = US1COUNT;
    }

    public void setUS2COUNT(int US2COUNT) {
        this.US2COUNT = US2COUNT;
    }

    public void setUS3COUNT(int US3COUNT) {
        this.US3COUNT = US3COUNT;
    }

    public void setUSOA(int USOA) {
        this.USOA = USOA;
    }

    public void setUSOB(int USOB) {
        this.USOB = USOB;
    }

    public void setAddedFileCount(int addedFileCount) {
        this.addedFileCount = addedFileCount;
    }

    public void setAddedMethodsCount(int addedMethodsCount) {
        this.addedMethodsCount = addedMethodsCount;
    }

    public void setChangedMethodsCount(int changedMethodsCount) {
        this.changedMethodsCount = changedMethodsCount;
    }

    public void setChangedFileCount(int changedFileCount) {
        this.changedFileCount = changedFileCount;
    }

    public void setBMchangedFilesCM(Metrics BMchangedFilesCM) {
        this.BMchangedFilesCM = BMchangedFilesCM;
    }

    public void setBMChangedFilesCpxMetrics(ComplexityMetric BMChangedFilesCpxMetrics) {
        this.BMChangedFilesCpxMetrics = BMChangedFilesCpxMetrics;
    }

    public void setExpChangedFilesCM(Metrics expChangedFilesCM) {
        this.expChangedFilesCM = expChangedFilesCM;
    }

    public void setExpChangedFilesCpxMetrics(ComplexityMetric expChangedFilesCpxMetrics) {
        this.expChangedFilesCpxMetrics = expChangedFilesCpxMetrics;
    }

    public void setUSAT_S(int USAT_S) {
        this.USAT_S = USAT_S;
    }

    public void setUUSO(int UUSO) {
        this.UUSO = UUSO;
    }

    public int getUSDA() {
        return this.USDA;
    }

    public int getUSDB() {
        return this.USDB;
    }

    public int getUSOA() {
        return this.USOA;
    }

    public int getUSOB() {
        return this.USOB;
    }
}