node('CAE-Jenkins2-DH-Agents-Linux'){
    def mdk_builder

    stage('Preparation') {
        checkout scm
    }

    sh 'git branch --list'
    GIT_TAG = sh (
                script: '$(git describe --tags --exact-match `git rev-parse HEAD 2> /dev/null` 2> /dev/null) || true'
                returnStdout: true
                ).trim()
    echo "GIT TAG = $GIT_TAG"

    mdk_builder = docker.build("willard/mdk-builder", "-f Dockerfile .circleci")

withCredentials([
    usernamePassword(credentialsId: 'mdk-testrail-credentials',      usernameVariable: 'TESTRAIL_CREDENTIALS_USR',    passwordVariable: 'TESTRAIL_CREDENTIALS_PSW' ),
    usernamePassword(credentialsId: 'mdk-artifactory-credentials',   usernameVariable: 'ARTIFACTORY_CREDENTIALS_USR', passwordVariable: 'ARTIFACTORY_CREDENTIALS_PSW' ),
    string(          credentialsId: 'mdk-build-access',              variable: 'BUILD_ACCESS'),
    string(          credentialsId: 'mdk-testrail-host',             variable: 'TESTRAIL_HOST'),
    string(          credentialsId: 'mdk-testrail-suite-id',         variable: 'TESTRAIL_SUITE_ID'),
    string(          credentialsId: 'mdk-artifactory-url',           variable: 'ARTIFACTORY_URL'),
    string(          credentialsId: 'mdk-additional-test-arguments', variable: 'ADDITIONAL_TEST_ARGUMENTS')]) {

        stage('Dependencies') {
            mdk_builder.inside{
                sh '''
                export GRADLE_USER_HOME=$(pwd)/.gradle
                ./gradlew -PbuildNumber=$BUILD_NUMBER -PbuildAccess=$BUILD_ACCESS -PbuildTag=$GIT_TAG -PartifactoryUrl=$ARTIFACTORY_URL -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW dependencies --gradle-user-home GRADLE_USER_HOME --info --stacktrace --refresh-dependencies
                '''
            }
        }
        stage('Compile') {
            mdk_builder.inside{
                sh '''
                export GRADLE_USER_HOME=$(pwd)/.gradle
                ./gradlew -PbuildNumber=$BUILD_NUMBER -PbuildAccess=$BUILD_ACCESS -PbuildTag=$GIT_TAG -PartifactoryUrl=$ARTIFACTORY_URL -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW --gradle-user-home GRADLE_USER_HOME --continue --info --stacktrace clean assemble
                '''
            }
        }
        stage('Test') {
            mdk_builder.inside{
                sh '''
                export GRADLE_USER_HOME=$(pwd)/.gradle
                xvfb-run ./gradlew -PbuildNumber=$BUILD_NUMBER -PbuildAccess=$BUILD_ACCESS -PbuildTag=$GIT_TAG -PartifactoryUrl=$ARTIFACTORY_URL -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW -PtestrailHost=$TESTRAIL_HOST -PtestrailUser=$TESTRAIL_CREDENTIALS_USR -PtestrailPassword=$TESTRAIL_CREDENTIALS_PSW -PtestrailSuiteId=$TESTRAIL_SUITE_ID -PadditionalTestArguments=$ADDITIONAL_TEST_ARGUMENTS --gradle-user-home GRADLE_USER_HOME --continue --info --stacktrace check testrailPublish
                '''
            }
        }
        stage('Publish') {
            mdk_builder.inside{
                sh '''
                export GRADLE_USER_HOME=$(pwd)/.gradle
                if [ -z $GIT_TAG ]; then ARTIFACTORY_REPOSITORY="maven-libs-snapshot-local"; else ARTIFACTORY_REPOSITORY="maven-libs-release-local"; fi
                ./gradlew -PbuildNumber=$BUILD_NUMBER -PbuildAccess=$BUILD_ACCESS -PbuildTag=$GIT_TAG -PartifactoryUrl=$ARTIFACTORY_URL -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW -PartifactoryRepository=$ARTIFACTORY_REPOSITORY --gradle-user-home GRADLE_USER_HOME --continue --info --stacktrace artifactoryPublish
                '''
            }
        }
    }
}


