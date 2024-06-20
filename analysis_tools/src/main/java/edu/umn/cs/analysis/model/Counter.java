package edu.umn.cs.analysis.model;

public class Counter {

    private int count = 0;

    public Counter() {

    }

    public void increment(){
        count++;
    }

    public int getCount(){
        return count;
    }
}
