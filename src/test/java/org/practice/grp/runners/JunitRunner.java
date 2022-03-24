package org.practice.grp.runners;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/resources/FinalFeatures/UXP_Banking_365" },
        // features = {"src/test/java/com/boi/grp/initialfeatures"},
        plugin = { "pretty", "json:target/cucumber-parallel/cucumber.json",
                "junit:target/cucumber-parallel/cucumber.xml", "html:target/cucumber-parallel",
                "rerun:target/cucumber-parallel/rerun.txt" }, dryRun = false, tags = { "@CHEQUESEARCH" }, glue = { "com.boi" })
public class JunitRunner {
}