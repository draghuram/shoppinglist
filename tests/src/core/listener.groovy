package com.slist.apitests.core

import org.testng.ITestResult
import org.testng.TestListenerAdapter

public class ApitestsTestListener extends TestListenerAdapter {
    private int count = 1;
    private int lineLength = 0;
    
    @Override
    public void onTestFailure(ITestResult tr) {
        logProgress(count + "(F) ")
    }
    
    @Override
    public void onTestSkipped(ITestResult tr) {
        logProgress(count + "(S) ")
    }
    
    @Override
    public void onTestSuccess(ITestResult tr) {
        logProgress(count + " ")
    }
    
    private void logProgress(String msg) {
        ++count;
        System.out.print(msg);
        lineLength += msg.length();

        if (lineLength > 60) {
            System.out.println("");
            lineLength = 0;
        }
    }
} 
