package edu.umn.cs.analysis.model;

import java.util.Objects;

public class StackComponent {

    private String fileName;
    private String className;
    private String methodName;
    private int lineInvokedInMethod;

    public StackComponent(String fileName, String className, String methodName, int lineInvokedInMethod){
        this.fileName=fileName;
        this.className=className;
        this.methodName=methodName;
        this.lineInvokedInMethod=lineInvokedInMethod;
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

    public int getLineInvokedInMethod() {
        return lineInvokedInMethod;
    }

    public String getInfo(){
        return className+" "+methodName+" "+lineInvokedInMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackComponent that = (StackComponent) o;
        return Objects.equals(fileName, that.fileName) && Objects.equals(className, that.className)  && Objects.equals(methodName, that.methodName) && lineInvokedInMethod == that.lineInvokedInMethod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, className, methodName, lineInvokedInMethod);
    }

}
