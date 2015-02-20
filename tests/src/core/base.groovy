package com.slist.apitests.core

import org.testng.Reporter

class TestBase {
    def storesAPI = new StoresAPI()
    def categoriesAPI = new CategoriesAPI()
    def itemsAPI = new ItemsAPI()

    // Use this when TestNG's expectedException annotation can not be used.
    // One example is when you want to check for exceptions not on entire method
    // but only on a portion of the code. 
    def expectException(Class expectedException, Closure c) {
        try {
            c.call()
        } catch (Exception e) {
            // "e instanceof expectedException" doesn't work so I switched
            // to using "in" operator based on 
            // From http://groovy-programming.com/post/514058979?5c071848
            // To check for multiple exception types:
            //     if ([ class1, class2, class3 ].find { foo in it }) doIt()
            if (e in expectedException) {
                return e;
            } else {
                throw new Exception("Expected " + expectedException + " but got " + e + " instead").initCause(e)
            }
        }

        throw new Exception("Expected " + expectedException + " but none came")
    }

    def run_with_no_exceptions(Closure c) {
        try {
            c.call()
        } catch (Throwable e) {
            Reporter.log(getStackTraceAsString(e))
        }
    }

    def run_with_no_exceptions(boolean predicate, Closure c) {
        try {
            if (predicate) {
                c.call()
            }
        } catch (Throwable e) {
            Reporter.log(getStackTraceAsString(e))
        }
    }

    public static String getStackTraceAsString(Exception exception) {
        Writer wr = new StringWriter();
        PrintWriter pWriter = new PrintWriter(wr);
        exception.printStackTrace(pWriter);
        return wr.toString();
    }
}
