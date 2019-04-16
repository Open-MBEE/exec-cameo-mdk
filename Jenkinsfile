pipeline {
    agent { dockerfile true  }
    environment {
        BUILD_ACCESS = credentials('mdk-build-access')

        TESTRAIL_HOST = credentials('mdk-testrail-host')
        TESTRAIL_CREDENTIALS = credentials('mdk-testrail-credentials')
        TESTRAIL_SUITE_ID = credentials('mdk-testrail-suite-id')

        ARTIFACTORY_URL = credentials('mdk-artifactory-url')
        ARTIFACTORY_CREDENTIALS = credentials('mdk-artifactory-credentials')

        ADDITIONAL_TEST_ARGUMENTS = credentials('mdk-additional-test-arguments')
        DISPLAY = ':1'
    }
    stages {
        stage('Dependencies') {
            steps {
                sh '''
                    GIT_TAG=$(git describe --tags --exact-match `git rev-parse HEAD 2> /dev/null` 2> /dev/null) || true
                    export GRADLE_USER_HOME=$(pwd)/.gradle
                    xvfb-run ./gradlew -PbuildNumber=$BUILD_NUMBER -PbuildAccess=$BUILD_ACCESS -PbuildTag=$GIT_TAG -PartifactoryUrl=$ARTIFACTORY_URL -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW dependencies --gradle-user-home GRADLE_USER_HOME --info --stacktrace --refresh-dependencies
                '''
            }
        }
        stage('Compile') {
            steps {
                sh '''
                    GIT_TAG=$(git describe --tags --exact-match `git rev-parse HEAD 2> /dev/null` 2> /dev/null) || true
                    export GRADLE_USER_HOME=$(pwd)/.gradle
                    xvfb-run ./gradlew -PbuildNumber=$BUILD_NUMBER -PbuildAccess=$BUILD_ACCESS -PbuildTag=$GIT_TAG -PartifactoryUrl=$ARTIFACTORY_URL -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW --gradle-user-home GRADLE_USER_HOME --continue --info --stacktrace clean assemble
                '''
            }
        }
        stage('Test') {
            steps {
                sh '''
                    GIT_TAG=$(git describe --tags --exact-match `git rev-parse HEAD 2> /dev/null` 2> /dev/null) || true
                    export GRADLE_USER_HOME=$(pwd)/.gradle
                    Xvfb $DISPLAY &
                    xvfb-run ./gradlew -PbuildNumber=$BUILD_NUMBER -PbuildAccess=$BUILD_ACCESS -PbuildTag=$GIT_TAG -PartifactoryUrl=$ARTIFACTORY_URL -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW -PtestrailHost=$TESTRAIL_HOST -PtestrailUser=$TESTRAIL_CREDENTIALS_USR -PtestrailPassword=$TESTRAIL_CREDENTIALS_PSW -PtestrailSuiteId=$TESTRAIL_SUITE_ID -PadditionalTestArguments=$ADDITIONAL_TEST_ARGUMENTS --gradle-user-home GRADLE_USER_HOME --continue --info --stacktrace check testrailPublish
                '''
            }
            post {
                always {
                    junit 'build/test-results/**/*.xml'
                    archiveArtifacts 'build/reports/**'
                }
            }
        }
        stage('Publish') {
            steps {
                sh '''
                    GIT_TAG=$(git describe --tags --exact-match `git rev-parse HEAD 2> /dev/null` 2> /dev/null) || true
                    export GRADLE_USER_HOME=$(pwd)/.gradle
                    if [ -z $GIT_TAG ]; then ARTIFACTORY_REPOSITORY="maven-libs-snapshot-local"; else ARTIFACTORY_REPOSITORY="maven-libs-release-local"; fi
                    xvfb-run ./gradlew -PbuildNumber=$BUILD_NUMBER -PbuildAccess=$BUILD_ACCESS -PbuildTag=$GIT_TAG -PartifactoryUrl=$ARTIFACTORY_URL -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW -PartifactoryRepository=$ARTIFACTORY_REPOSITORY --gradle-user-home GRADLE_USER_HOME --continue --info --stacktrace artifactoryPublish
                '''
            }
        }
    }
}