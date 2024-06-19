package edu.xyz.cs.analysis.model;

import com.github.javaparser.ast.CompilationUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DuplicateTestClassInfo {
    public String oldTCName;
    public String newTCName;
    public List<String> taList = new ArrayList<>();
    public String fileName;
    public String duplicatedTestClassName,newTestName,newFilePath;
    public List<String> stubbedNameList = new ArrayList<>();
    public List<String> testMethodList = new ArrayList<>();
    public List<Integer> lineNumList = new ArrayList<>();
    public CompilationUnit cu = null;

    public DuplicateTestClassInfo(String fileName, String oldTCName, String newTCName, List<String> taList){
        this.newTCName = newTCName;
        this.taList = taList;
        this.oldTCName = oldTCName;
        this.fileName = fileName;
    }
    public DuplicateTestClassInfo(CompilationUnit cu, String fileName, String duplicatedTestClassName, String newTestName, String newFilePath, List<Integer> lineNumList, List<String> stubbedNameList, List<String> testMethodList, List<String> taList){
        this.cu = cu;
        this.fileName = fileName;
        this.duplicatedTestClassName = duplicatedTestClassName;
        this.newTestName = newTestName;
        this.newFilePath = newFilePath;
        this.lineNumList = lineNumList;
        this.stubbedNameList = stubbedNameList;
        this.testMethodList = testMethodList;
        this.taList = taList;

    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DuplicateTestClassInfo that = (DuplicateTestClassInfo) o;
        return Objects.equals(fileName, that.fileName)  && Objects.equals(oldTCName, that.oldTCName);
    }
}