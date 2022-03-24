Feature: Test

	
	Scenario Outline: Sample test case 1
		Given I have performed test for <TestCaseId>
		And I have user type <USERTYPE> with user data <USERDATA>
		Then account type is <ACCOUNTTYPE>

		Examples: 
			|TestCaseId|USERTYPE|USERDATA|ACCOUNTTYPE|
			|UB123|test|1234:3445:4555|Current|
			|UB123|test|345:5667:900|Random|
			|test|test|test|test|

	
	Scenario Outline: Sample test case 2
		Given I have performed test for <TestCaseId>
		And I have user type <USERTYPE> with user data <USERDATA>
		Then account type is <ACCOUNTTYPE>

		Examples: 
			|TestCaseId|USERTYPE|USERDATA|ACCOUNTTYPE|
			|UB123|test|1234:3445:4555|Current|
