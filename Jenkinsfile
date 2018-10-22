pipeline {
    agent {
        dockerfile {
            label 'CAE-Jenkins2-DH-Agents-Linux'
            // image 'openjdk:8-jdk-alpine'
        }
    }
    environment {
        BUILD_ACCESS = credentials('mdk-build-access')

        TESTRAIL_HOST = credentials('mdk-testrail-host')
        TESTRAIL_CREDENTIALS = credentials('mdk-testrail-credentials')
        TESTRAIL_SUITE_ID = credentials('mdk-testrail-suite-id')

        ARTIFACTORY_URL = credentials('mdk-artifactory-url')
        ARTIFACTORY_CREDENTIALS = credentials('mdk-artifactory-credentials')

        ADDITIONAL_TEST_ARGUMENTS = credentials('mdk-additional-test-arguments')
    }
    stages {
        stage('Dependencies') {
            steps {
                sh 'GIT_TAG=$(git describe --tags --exact-match `git rev-parse HEAD 2> /dev/null` 2> /dev/null) || true'
                sh './gradlew -PbuildNumber=$BUILD_NUMBER -PbuildAccess=$BUILD_ACCESS -PbuildTag=$GIT_TAG -PartifactoryUrl=$ARTIFACTORY_URL -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW dependencies --gradle-user-home ./.gradle --info --stacktrace --refresh-dependencies'
            }
        }
        stage('Compile') {
            steps {
                sh 'GIT_TAG=$(git describe --tags --exact-match `git rev-parse HEAD 2> /dev/null` 2> /dev/null) || true'
                sh './gradlew -PbuildNumber=$BUILD_NUMBER -PbuildAccess=$BUILD_ACCESS -PbuildTag=$GIT_TAG -PartifactoryUrl=$ARTIFACTORY_URL -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW --gradle-user-home ./.gradle --continue --info --stacktrace assemble'
            }
        }
        stage('Test') {
            steps {
                sh "export DISPLAY=:9001"
                sh "Xvfb $DISPLAY &"
                sh 'GIT_TAG=$(git describe --tags --exact-match `git rev-parse HEAD 2> /dev/null` 2> /dev/null) || true'
                sh './gradlew -PbuildNumber=$BUILD_NUMBER -PbuildAccess=$BUILD_ACCESS -PbuildTag=$GIT_TAG -PartifactoryUrl=$ARTIFACTORY_URL -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW -PtestrailHost=$TESTRAIL_HOST -PtestrailUser=$TESTRAIL_CREDENTIALS_USR -PtestrailPassword=$TESTRAIL_CREDENTIALS_PSW -PtestrailSuiteId=$TESTRAIL_SUITE_ID -PadditionalTestArguments=$ADDITIONAL_TEST_ARGUMENTS --gradle-user-home ./.gradle --continue --info --stacktrace check'
                sh "kill %1"
            }
        }
        stage('Publish') {
            steps {
                sh 'GIT_TAG=$(git describe --tags --exact-match `git rev-parse HEAD 2> /dev/null` 2> /dev/null) || true'
                sh 'if [ -z $BUILD_TAG ]; then ARTIFACTORY_REPOSITORY="maven-libs-snapshot-local"; else ARTIFACTORY_REPOSITORY="maven-libs-release-local"; fi'
                sh './gradlew -PbuildNumber=$BUILD_NUMBER -PbuildAccess=$BUILD_ACCESS -PbuildTag=$GIT_TAG -PartifactoryUrl=$ARTIFACTORY_URL -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW -PartifactoryRepository=$ARTIFACTORY_REPOSITORY --gradle-user-home ./.gradle --continue --info --stacktrace artifactoryPublish'
            }
        }
    }


    // post {
    //     always {
    //         archiveArtifacts 'build/reports/**'
    //         junit 'build/test-results/**/*.xml'
    //     }
    // }

}