package org.practice.grp.stepDefination;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class Sample_StepDef {

    @Given("^I have performed test for (.+)$")
    public void test(String testCaseId){
        System.out.println("Welocme");
    }

    @And("I have user type (.+) with user data (.+)$")
    public void iHaveUserTypeUSERTYPEWithUserDataUSERDATA(String user,String type) {
        System.out.println("Welocme");
    }

    @Then("account type is (.+)$")
    public void accountTypeIsACCOUNTTYPE(String parm) {
        System.out.println("Welocme");
    }
}
