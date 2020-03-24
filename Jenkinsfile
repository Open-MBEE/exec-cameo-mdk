pipeline {
    agent {
        docker{
            image 'circleci/openjdk@sha256:012843a8f91da1ea5e22e142bb5cb8c999a7457e03b8c79ab45d5fcb68043383'
        }
    }
    environment {
        GRADLE_USER_HOME = "$WORKSPACE/.gradle"
        ARTIFACTORY_CREDENTIALS = credentials('mdk-artifactory-credentials')
        TESTRAIL_CREDENTIALS = credentials('mdk-testrail-credentials')
        BUILD_ACCESS = credentials('mdk-build-access')
        TESTRAIL_HOST = credentials('mdk-testrail-host')
        TESTRAIL_SUITE_ID = credentials('mdk-testrail-suite-id')
        ARTIFACTORY_URL = credentials('mdk-artifactory-url')
        ADDITIONAL_TEST_ARGUMENTS = credentials('mdk-additional-test-args-19')
    }

    stages {
        stage('Dependencies') {
            steps {
                echo 'TAG_NAME = $TAG_NAME'
                sh './gradlew \
                    -PbuildNumber=$BUILD_NUMBER \
                    -PbuildAccess=$BUILD_ACCESS \
                    -PbuildTag=$TAG_NAME \
                    -PartifactoryUrl=$ARTIFACTORY_URL \
                    -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR \
                    -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW \
                    --gradle-user-home $GRADLE_USER_HOME \
                    --info --stacktrace --refresh-dependencies \
                    dependencies'
            }
        }
        stage('Compile') {
            steps {
                sh './gradlew \
                    -PbuildNumber=$BUILD_NUMBER \
                    -PbuildAccess=$BUILD_ACCESS \
                    -PbuildTag=$TAG_NAME \
                    -PartifactoryUrl=$ARTIFACTORY_URL \
                    -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR \
                    -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW \
                    --gradle-user-home $GRADLE_USER_HOME \
                    --continue --info --stacktrace \
                    clean assemble'
            }
        }
        stage('Test') {
            steps {
                sh 'xvfb-run ./gradlew \
                    -PbuildNumber=$BUILD_NUMBER \
                    -PbuildAccess=$BUILD_ACCESS \
                    -PbuildTag=$TAG_NAME \
                    -PartifactoryUrl=$ARTIFACTORY_URL \
                    -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR \
                    -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW \
                    -PtestrailHost=$TESTRAIL_HOST \
                    -PtestrailUser=$TESTRAIL_CREDENTIALS_USR \
                    -PtestrailPassword=$TESTRAIL_CREDENTIALS_PSW \
                    -PtestrailSuiteId=$TESTRAIL_SUITE_ID \
                    -PadditionalTestArguments=$ADDITIONAL_TEST_ARGUMENTS \
                    --gradle-user-home $GRADLE_USER_HOME \
                    --continue --info --stacktrace \
                    check testrailPublish'
            }
        }
        stage('Publish Snapshot') {
            when { anyOf { branch 'develop' ; branch 'release/**' } }
            steps {
                sh './gradlew \
                -PbuildNumber=$BUILD_NUMBER \
                -PbuildAccess=$BUILD_ACCESS \
                -PbuildTag=$TAG_NAME \
                -PartifactoryUrl=$ARTIFACTORY_URL \
                -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR \
                -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW \
                -PartifactoryRepository=maven-libs-snapshot-local \
                --gradle-user-home $GRADLE_USER_HOME \
                --continue --info --stacktrace \
                artifactoryPublish'
            }
        }

        stage('Publish Release') {
            when { tag "*" }
            steps {
                sh './gradlew \
                -PbuildNumber=$BUILD_NUMBER \
                -PbuildAccess=$BUILD_ACCESS \
                -PbuildTag=$TAG_NAME \
                -PartifactoryUrl=$ARTIFACTORY_URL \
                -PartifactoryUsername=$ARTIFACTORY_CREDENTIALS_USR \
                -PartifactoryPassword=$ARTIFACTORY_CREDENTIALS_PSW \
                -PartifactoryRepository=maven-libs-release-local \
                --gradle-user-home $GRADLE_USER_HOME \
                --continue --info --stacktrace \
                artifactoryPublish'
            }
        }
    }
}


