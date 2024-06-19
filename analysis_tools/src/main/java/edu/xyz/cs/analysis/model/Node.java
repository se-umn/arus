package edu.xyz.cs.analysis.model;

import java.util.Objects;

public class Node {
    private StackComponent value;
    private StackComponent previous;
    private boolean hasNext;

    public Node(StackComponent previous, StackComponent value, boolean hasNext)  {
        this.previous = previous;
        this.value = value;
        this.hasNext = hasNext;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node that = (Node) o;
        return Objects.equals(value, that.value)  && Objects.equals(hasNext, that.hasNext) && Objects.equals(previous, that.previous);
    }

    public StackComponent getValue() {
        return value;
    }

    public void setValue(StackComponent value) {
        this.value = value;
    }

    public StackComponent getPrevious() {
        return previous;
    }

    public void setPrevious(StackComponent previous) {
        this.previous = previous;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
};
