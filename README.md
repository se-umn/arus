### Steps to Run the Experiment
1. Make sure that maven is installed on the local machine, the maven version used for running experiment is Apache Maven 3.8.4
2. Find the path to Maven's home by using `mvn -version`
3. Clone the project to be run and get its absolute path.
3. Change directory to `test_mocking_refactoring/analysis_tools`
4. Run the following command `./gradlew clean -PmainClass=edu.umn.cs.analysis.ARUS run --args="arg1 arg2 arg3"`.
`arg1` should be the absolute path to Maven. `arg2` should the absolute path to the project that needs to remove unnecessary stubbings from its test suite. `arg3` is the boolean indicates whether to remove UUS stubbings or not. If no `arg3` is passed, ARUS will set the boolean to `false` by default, which means UUS stubbings will be removed to the test suite.
5. After running, Unnecessary Stubbings numbers will be printed out to the terminal. Use `git diff .` to check changes have been made to test files.
  