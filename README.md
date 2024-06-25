### Updated Steps to Run the Experiment

1. Make sure that Maven is installed on the local machine. The Maven version used for running the experiment is Apache Maven 3.8.4.
2. Find the path to Maven's home by using `mvn -version`.
3. Clone the project to be run and get its absolute path.
4. Ensure the `gradle` folder, which contains the Gradle wrapper, is included in the project directory to avoid the error: `Could not find or load main class org.gradle.wrapper.GradleWrapperMain`.
5. Change the directory to `test_mocking_refactoring/analysis_tools`.
6. Run the following command:
    ```sh
    ./gradlew clean -PmainClass=edu.umn.cs.analysis.ARUS run --args="arg1 arg2 arg3"
    ```
    - `arg1` should be the absolute path to Maven.
    - `arg2` should be the absolute path to the project needing to remove unnecessary stubbings from its test suite.
    - `arg3` is a boolean that indicates whether to remove UUS stubbings. If no `arg3` is passed, ARUS will set the boolean to `true` by default, which means UUS stubbings will be removed from the test suite.
7. Unnecessary Stubbings numbers will be printed out to the terminal when ARUS finishes its process.
8. ARUS will create a copy of the project within the same directory, appending `_experiment` to the original project name. To review the changes made to the test files, navigate to the `_experiment` folder and use `git diff`.
