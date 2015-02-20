package com.slist.apitests

import org.testng.TestNG

def main() {
    if (!args) {
        args = [System.getProperty("apitests.dir", ".") + "/testng.xml"] as String[]
    }

    args = args + ["-listener", "com.slist.apitests.core.ApitestsTestListener"] as String[]

    TestNG.main(args)
}

main()

