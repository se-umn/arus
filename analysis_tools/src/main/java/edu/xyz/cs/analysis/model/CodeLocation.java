package edu.xyz.cs.analysis.model;

import ppg.code.Code;

import java.util.Objects;

public class CodeLocation {
    public static final String NATIVE_LOCATION = "NATIVE_LOCATION";

    private String fileName;
    private String className;
    private String methodName;
    private int lineNum;

    public CodeLocation(String fileName, String className, String methodName, int lineNum){
        this.fileName=fileName;
        this.className=className;
        this.methodName=methodName;
        this.lineNum=lineNum;
    }

    public boolean performSanityChecks() {
        boolean passed = true;
        if (fileName == null || fileName.equals("")) {
            passed = false;
            return passed;
        }
        if (fileName.equals(CodeLocation.NATIVE_LOCATION)) {
            if (className == null || className.equals("") || className.contains(" ")) {
                passed = false;
                return passed;
            }
            if (methodName == null || methodName.equals("") || methodName.contains(" ")) {
                passed = false;
                return passed;
            }
            if (lineNum != 0) {
                passed = false;
                return passed;
            }
        }
        else{
            if (!fileName.endsWith(".java")) {
                passed = false;
                return passed;
            }
            if (className == null || className.equals("") || className.contains(" ")) {
                passed = false;
                return passed;
            }
            if (methodName == null || methodName.equals("") || methodName.contains(" ")) {
                passed = false;
                return passed;
            }
            if (lineNum < 1) {
                passed = false;
                return passed;
            }
        }
        return passed;
    }

    public String getFileName() {
        return fileName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getLineNum() {
        return lineNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeLocation that = (CodeLocation) o;
        return Objects.equals(fileName, that.fileName) && Objects.equals(className, that.className)  && Objects.equals(methodName, that.methodName) && lineNum == that.lineNum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, className, methodName, lineNum);
    }

}
