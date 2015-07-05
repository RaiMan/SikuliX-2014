
*** Variables ***
${USERNAME}               demo
${PASSWORD}               mode
${TESTSITE}               http://test.sikuli.de

*** Settings ***
Library           /Users/raimundhocke/SikuliX/SikuliX-2014/StuffContainer/testScripts/testRobot/testRun.sikuli.robot/LoginLibrary.py
Test Setup        start firefox and goto testsite    ${TESTSITE}
Test Teardown     stop firefox

*** Test Cases ***
User can log in with correct user and password
    Attempt to Login with Credentials    ${USERNAME}    ${PASSWORD}
    Status Should Be    Accepted

User cannot log in with invalid user or bad password
    Attempt to Login with Credentials    betty    wrong
    Status Should Be    Denied