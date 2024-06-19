package edu.xyz.cs.analysis.model;

import java.util.Objects;

public class ExecutionLocation {
    private ExecutionLocationType type;
    private String className;
    private String methodName;

    public ExecutionLocation(ExecutionLocationType type, String className, String methodName){
        this.type=type;
        this.className=className;
        this.methodName=methodName;
    }

    public boolean performSanityChecks() {
        boolean passed = true;
        if(className==null || className.equals("") || className.contains(" ")){
            passed = false;
            return passed;
        }
        if(methodName==null || methodName.equals("") || methodName.contains(" ")){
            passed = false;
            return passed;
        }
        return passed;
    }

    public ExecutionLocationType getType() {
        return type;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionLocation that = (ExecutionLocation) o;
        return type == that.type && Objects.equals(className, that.className) && Objects.equals(methodName, that.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, className, methodName);
    }
}
