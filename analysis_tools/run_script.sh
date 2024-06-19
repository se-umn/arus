#!/bin/bash

# Remove the ~/repos_tmp directory
rm -rf ~/repos_tmp
# Run the experiment redirect the output to a file
./gradlew clean -PmainClass=edu.xyz.cs.analysis.RunExperiment run --args="/home/mia/.sdkman/candidates/maven/current" > ~/RunSetupFlagDefault.txt
rm -rf ~/repos_tmp
# Run the experiment redirect the output to a file
./gradlew clean -PmainClass=edu.xyz.cs.analysis.RunExperiment run --args="/home/mia/.sdkman/candidates/maven/current true" > ~/RunSetupFlagTrue.txt
#rm -rf ~/repos_tmp
## Run the experiment redirect the output to a file
#./gradlew clean -PmainClass=edu.xyz.cs.analysis.RunExperiment run --args="/home/mia/.sdkman/candidates/maven/current" > ~/ReRunSetup3.txt
## Remove the ~/repos_tmp directory
#rm -rf ~/repos_tmp
## Run the experiment redirect the output to a file
#./gradlew clean -PmainClass=edu.xyz.cs.analysis.RunExperiment run --args="/home/mia/.sdkman/candidates/maven/current" > ~/ReRunSetup4.txt
#rm -rf ~/repos_tmp
## Run the experiment redirect the output to a file
#./gradlew clean -PmainClass=edu.xyz.cs.analysis.RunExperiment run --args="/home/mia/.sdkman/candidates/maven/current" > ~/ReRunSetup5.txt
#rm -rf ~/repos_tmp
## Run the experiment redirect the output to a file
#./gradlew clean -PmainClass=edu.xyz.cs.analysis.RunExperiment run --args="/home/mia/.sdkman/candidates/maven/current" > ~/ReRunSetup6.txt