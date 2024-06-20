package edu.umn.cs.analysis.model;

import java.util.Objects;

public class Edge {
    private StackComponent src;
    private StackComponent dest;
    private StackComponent pre;
    private boolean hasNext;

    public Edge(StackComponent pre, StackComponent src, StackComponent dest, boolean hasNext) {
        this.pre = pre;
        this.src = src;
        this.dest = dest;
        this.hasNext = hasNext;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge that = (Edge) o;
        return Objects.equals(pre, that.pre) && Objects.equals(src, that.src)  && Objects.equals(dest, that.dest) && Objects.equals(hasNext, that.hasNext);
    }

    public StackComponent getSrc() {
        return src;
    }

    public void setSrc(StackComponent src) {
        this.src = src;
    }

    public StackComponent getDest() {
        return dest;
    }

    public void setDest(StackComponent dest) {
        this.dest = dest;
    }

    public StackComponent getPre() {
        return pre;
    }

    public void setPre(StackComponent pre) {
        this.pre = pre;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
}