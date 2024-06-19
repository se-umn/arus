package edu.xyz.cs.analysis.model;

import java.util.*;

public class FixUnusedStubsState {

    private boolean needAddPackage, isExtend, isOutside,isusInMultipleCase;
    private String filePath;
    private Set<String> addTestNameRuleList;
    private Map<String, Set<String>> relatedClassMap;
    private List<String> importFileName, importName;
    private List<Boolean> importIsStatic;
    public Map<Integer, List<String>> case3StubbedMethodMap;
    public Map<String,String> nameMap = new HashMap<>();
    public Map<String, Integer> indexMap = new HashMap<>();
    public int solutionOption;
    public Set<String> changedMethodNameList, addedFileList, changedFileNameList, addedMethodNameList;

    public FixUnusedStubsState(){
        this.needAddPackage = false;
        this.filePath = "";
        this.isExtend = false;
        this.isOutside = false;
        this.isusInMultipleCase = false;
        this.addTestNameRuleList = new HashSet<>();
        this.relatedClassMap = new HashMap<>();
        this.importFileName = new ArrayList<>();
        this.importIsStatic = new ArrayList<>();
        this.importName = new ArrayList<>();
        this.addedFileList = new HashSet<>();
        this.changedFileNameList = new HashSet<>();
        this.addedMethodNameList = new HashSet<>();
        this.changedMethodNameList = new HashSet<>();
        this.case3StubbedMethodMap = new HashMap<>();
        this.solutionOption = -1;
    }

    public boolean isNeedAddPackage() {
        return needAddPackage;
    }

    public void setNeedAddPackage(boolean needAddPackage) {
        this.needAddPackage = needAddPackage;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isExtend() {
        return isExtend;
    }

    public void setExtend(boolean extend) {
        isExtend = extend;
    }

    public void setusInMultipleCase(boolean usInMultipleCase) {
        isusInMultipleCase = usInMultipleCase;
    }

    public boolean isusInMultipleCase() {
        return isusInMultipleCase;
    }


    public boolean isOutside() {
        return isOutside;
    }

    public void setOutside(boolean outside) {
        isOutside = outside;
    }

    public Set<String> getAddTestNameRuleList() {
        return addTestNameRuleList;
    }

    public Map<String, Set<String>> getRelatedClassMap() {
        return relatedClassMap;
    }

    public List<String> getImportFileName() {
        return importFileName;
    }

    public List<String> getImportName(){
        return importName;
    }

    public List<Boolean> getImportIsStatic() {
        return importIsStatic;
    }


    public Set<String> getChangedFilesName() {
        return changedFileNameList;
    }

    public void setSolutionOption(int solutionOption) {
        this.solutionOption = solutionOption;
    }
}
