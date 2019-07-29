# HaveIBeenpwned_Java
General POC to launch HaveIBeenpwned.com and enter EMail Id's to verify if it is pawned

# Requirements(To Update Project):-

Java 1.8 Installed and Path Set "C:\Program Files\Java\jdk1.8.0_161\bin;"

Eclipse with Maven Plugin installed.

Microsoft Office for Excel installed.


# Requirements(To Run/Demo):

Java 1.8 Installed and Path Set "C:\Program Files\Java\jdk1.8.0_161\bin;"

Microsoft Office for Excel installed.


# How To Run :

1. Go to target\src\main\resources and open ReportPawn.xlsx and fill in all the email ID's that needs to be validated in first column accordingly.

2. Go to target folder and open command prompt and run
"java -jar HaveIBeenPawnd.jar conf.ini"

3. After Execution ends then simply launch ReportPawn.xlsx and verify respective outputs

(OR)

If simply single EMail needs to be checked then

2. Go to target folder and open command prompt and run

"java -jar HaveIBeenPawnd.jar conf.ini someothersemailid@emailtest.com"

This will simply print result on console if email is pawned or not.

