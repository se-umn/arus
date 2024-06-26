### Steps to Run ARUS

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
### Experimental Results
| ID     | Name      | Commit ID | LOC$_{sc}$ | LOC$_{tc}$ | Tests | USD$_{b}$ | USD$_{a}$ | USO$_{b}$ | USO$_{a}$ | TU | UUH | UUS |
|--------|-----------|-----------|------------|------------|-------|-----------|-----------|-----------|-----------|----|-----|-----|
| P01 | [Allure](https://github.com/allure-framework/allure-bamboo) | 85a9408c | 1086 | 135 | 15 | 2 | 0 | 5 | 0 | 1 | 0 | 1 |
| P02 | [Amazon-ecs](https://github.com/jenkinsci/amazon-ecs-plugin) | 44817eda | 2403 | 231 | 27 | 3 | 0 | 3 | 0 | 3 | 0 | 0 |
| P03 | [Amazon-sqs](https://github.com/awslabs/amazon-sqs-java-extended-client-lib) | 450d5221 | 481 | 417 | 35 | 1 | 0 | 14 | 0 | 0 | 0 | 1 |
| P04 | [Appcenter](https://github.com/jenkinsci/appcenter-plugin) | 986ec689 | 1810 | 1301 | 146 | 7 | 0 | 26 | 0 | 0 | 0 | 7 |
| P05 | [Aws-codepipeline](https://github.com/awslabs/aws-codepipeline-custom-job-worker) | 33952495 | 762 | 525 | 45 | 2 | 0 | 16 | 0 | 0 | 0 | 2 |
| P06 | [Bftsmart](https://github.com/blockchain-jd-com/bftsmart) | 44c1cb2e | 16053 | 3379 | 12 | 8 | 1 | 42 | 2 | 7 | 0 | 0 |
| P07 | [CAS](https://github.com/apereo/cas-server-security-filter) | a84c946c | 552 | 357 | 38 | 1 | 0 | 1 | 0 | 1 | 0 | 0 |
| P08 | [Chucknorris](https://github.com/jenkinsci/chucknorris-plugin) | 2a9dc4b0 | 159 | 208 | 26 | 4 | 0 | 12 | 0 | 0 | 0 | 4 |
| P09 | [Conduit](https://github.com/RentTheRunway/conduit) | c6f82f67 | 1400 | 1415 | 79 | 24 | 0 | 112 | 0 | 17 | 0 | 7 |
| P10 | [Datadog](https://github.com/jenkinsci/datadog-plugin) | 875c82b9 | 7670 | 4127 | 161 | 17 | 0 | 34 | 0 | 14 | 1 | 2 |
| P11 | [Easytable](https://github.com/vandeseer/easytable) | b3c278a9 | 1661 | 1082 | 57 | 4 | 0 | 8 | 0 | 1 | 3 | 0 |
| P12 | [Github-branch](https://github.com/jenkinsci/github-branch-source-plugin) | e708675a | 7056 | 5924 | 522 | 2 | 0 | 3 | 0 | 2 | 0 | 0 |
| P13 | [GivWenZen](https://github.com/weswilliams/GivWenZen) | 455a03aa | 1757 | 1089 | 57 | 2 | 0 | 2 | 0 | 1 | 0 | 1 |
| P14 | [Google-compute](https://github.com/jenkinsci/google-compute-engine-plugin) | 08e2f706 | 2166 | 1279 | 50 | 19 | 0 | 76 | 0 | 7 | 1 | 11 |
| P15 | [Google-kubernetes](https://github.com/jenkinsci/google-kubernetes-engine-plugin) | ef890e4a | 999 | 1089 | 109 | 8 | 0 | 36 | 0 | 0 | 7 | 1 |
| P16 | [Google-oauth](https://github.com/jenkinsci/google-oauth-plugin) | 3e03b2cb | 1021 | 1303 | 89 | 4 | 0 | 4 | 0 | 4 | 0 | 0 |
| P17 | [HAP](https://github.com/hap-java/HAP-Java) | f4a9872d | 6066 | 98 | 12 | 2 | 0 | 7 | 0 | 0 | 0 | 2 |
| P18 | [Hashicorp](https://github.com/jenkinsci/hashicorp-vault-plugin) | 182c0fba | 2477 | 2028 | 110 | 15 | 0 | 144 | 0 | 3 | 10 | 2 |
| P19 | [Instant-messaging](https://github.com/jenkinsci/instant-messaging-plugin) | 51f23def | 2992 | 674 | 41 | 16 | 2 | 521 | 503 | 10 | 4 | 0 |
| P20 | [KittehIRCClientLib](https://github.com/KittehOrg/KittehIRCClientLib) | 46b57952 | 9938 | 2168 | 198 | 17 | 0 | 77 | 0 | 9 | 1 | 7 |
| P21 | [LDBC](https://github.com/ldbc/ldbc_snb_datagen) | 0c019a46 | 4736 | 488 | 3 | 1 | 0 | 1 | 0 | 1 | 0 | 0 |
| P22 | [Matomo](https://github.com/matomo-org/matomo-java-tracker) | 751823e6 | 1285 | 1503 | 184 | 8 | 0 | 8 | 0 | 8 | 0 | 0 |
| P23 | [MobArena](https://github.com/garbagemule/MobArena) | 9164b125 | 13906 | 2572 | 293 | 4 | 0 | 8 | 0 | 1 | 2 | 1 |
| P24 | [MutabilityDetector](https://github.com/MutabilityDetector/MutabilityDetector) | ac1bc226 | 6710 | 3421 | 371 | 5 | 0 | 5 | 0 | 5 | 0 | 0 |
| P25 | [Netconf](https://github.com/Juniper/netconf-java) | c0fbedac | 1570 | 545 | 51 | 2 | 0 | 14 | 0 | 0 | 0 | 2 |
| P26 | [Oauth-filter](https://github.com/curityio/oauth-filter-for-java) | eb27b214 | 979 | 241 | 11 | 3 | 1 | 5 | 3 | 2 | 0 | 0 |
| P27 | [Offheap](https://github.com/Terracotta-OSS/offheap-store) | 05cc59ec | 11180 | 7192 | 125 | 1 | 0 | 1 | 0 | 1 | 0 | 0 |
| P28 | [OPML](https://github.com/mdewilde/opml-parser) | ae6a03d9 | 904 | 935 | 58 | 1 | 0 | 1 | 0 | 1 | 0 | 0 |
| P29 | [Pgadapter](https://github.com/cloudspannerecosystem/pgadapter) | e64d3f0d | 2759 | 1205 | 86 | 12 | 0 | 10 | 0 | 7 | 0 | 5 |
| P30 | [Photon](https://github.com/komoot/photon) | 4343b9f3 | 2201 | 1513 | 112 | 5 | 0 | 22 | 0 | 3 | 2 | 0 |
| P31 | [Projog](https://github.com/s-webber/projog) | 70fea568 | 9761 | 9623 | 1100 | 1 | 0 | 53 | 0 | 0 | 0 | 1 |
| P32 | [Repository-connector](https://github.com/jenkinsci/repository-connector-plugin) | 34fef47d | 1418 | 577 | 23 | 1 | 0 | 3 | 0 | 0 | 0 | 1 |
| P33 | [Serenity](https://github.com/Invictum/serenity-reportportal-integration) | 4c5476f3  | 861 | 523 | 80 | 2 | 0 | 3 | 0 | 0 | 0 | 2 |
| P34 | [Sling](https://github.com/apache/sling-org-apache-sling-commons-threads) | ff2418ae  | 1209  | 189 | 9 | 1 | 0 | 1 | 0 | 1 | 0 | 0 |
| P35 | [Sonar-auth](https://github.com/vaulttec/sonar-auth-oidc) | 99d86044  | 395 | 727 | 65 | 25 | 0 | 73 | 0 | 15 | 6 | 4 |
| P36 | [Sonar-scm](https://github.com/perforce/sonar-scm-perforce) | 115cc273  | 357 | 104 | 6 | 5 | 0 | 5 | 0 | 5 | 0 | 0 |
| P37 | [Subversion](https://github.com/jenkinsci/subversion-plugin) | dd1693c1  | 6463 | 2517 | 293 | 2 | 0 | 13 | 0 | 1 | 1 | 0 |
| P38 | [SwornAPI](https://github.com/dmulloy2/SwornAPI) | 0e33d2a1  | 4182 | 102 | 7 | 4 | 0 | 20 | 0 | 2 | 2 | 0 |
| P39 | [Token](https://github.com/jenkinsci/token-macro-plugin) | 871c6edc  | 2214 | 1884 | 176 | 37 | 0 | 135 | 0 | 27 | 10 | 0 |
| P40 | [Xunit](https://github.com/jenkinsci/xunit-plugin) | bf2a9c19  | 2078 | 1246 | 191 | 2 | 0 | 5 | 0 | 0 | 0 | 2 |
| **Total** |           |           | **143677** | **65936**  | **5073** | **280**   | **4**     | **1529**  | **508**   | **160** | **50** | **66** |

- **ID**: Project identifier
- **Name**: Project name with link to GitHub repository
- **Commit ID**: Version analyzed
- **LOC$_{sc}$**: Number of lines of code in the source code
- **LOC$_{tc}$**: Number of lines of code in the test code
- **Tests**: Number of tests
- **USD$_{b}$**: Number of stubbing definitions leading to unnecessary stubbings before running ARUS
- **USD$_{a}$**: Number of stubbing definitions leading to unnecessary stubbings after running ARUS
- **USO$_{b}$**: Number of unnecessary stubbing occurrences before running ARUS
- **USO$_{a}$**: Number of unnecessary stubbing occurrences after running ARUS
- **TU**: Number of totally-unnecessary stubbings
- **UUH**: Number of used-unnecessary-helper stubbings
- **UUS**: Number of used-unnecessary-setup stubbings
