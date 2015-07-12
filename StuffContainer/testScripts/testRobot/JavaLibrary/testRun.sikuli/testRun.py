workDir = getParentPath()
libJar = "RobotLibrary.jar"
RUNTIME.addToClasspath(os.path.join(workDir, libJar))

runScript("""
robot

*** Variables ***
| ${result} | nothing

*** Settings ***
| Documentation | Show we can Java libraries
| Library | robotlibrary.RobotLibrary
    
*** Test Cases ***
| Yes we can Java Keyword Libraries
| | [Documentation] | show it
| | Set Log Level | TRACE
| | ${result}= | this is a keyword
| | Set Global Variable | ${result} | ${result} | 

| Check the result
| | [Documentation] | test it
| | Should Be Equal | ${result} | I am a keyword
    
""")
