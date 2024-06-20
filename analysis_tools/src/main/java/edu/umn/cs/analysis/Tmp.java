package edu.umn.cs.analysis;

import edu.umn.cs.analysis.model.TestAnalysis;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Tmp {
    public static void main(String[] args)
    {
//        try{
//            foo();
//        }
//        catch (Exception e){
//            System.out.println("HERE");
//        }
        //read customized mockito trace
        List<TestAnalysis> testAnalyses = new ArrayList<TestAnalysis>();
        try {
            testAnalyses = CustomizedMockitoUtils.parseCustomizedMockitoTrace("/Users/mattia/Faculty/Research/2021_test_mocking_refactoring/repos_tmp/Multiverse_Multiverse-Inventories_experiment/mel.txt");
        }
        catch (Exception e){
            System.out.println("ERROR: "+e.getMessage());
        }
    }

    public static void foo() {
        try {
            System.out.println("Inside try block");

            // Throw an Arithmetic exception
            System.out.println(34 / 0);
        }

        // Can not accept Arithmetic type exception
        // Only accept Null Pointer type Exception
        catch (NullPointerException e) {
            throw e;
        }

        // Always execute
        finally {

            System.out.println(
                    "finally : i will execute always.");
        }
        // This will not execute
        System.out.println("i want to run");
    }
}
