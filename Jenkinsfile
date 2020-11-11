// Initialize a LinkedHashMap / object to share between stages
def pipelineContext = [:]

pipeline {
    agent any
    environment {
        DOCKER_REPOSITORY = "raulsuarezdabo/tfm-devsecop-jenkins"
        RC_BRANCH = 'devsecop'
        RELEASE_BRANCH = 'main'
    }
    stages {
        // Static Application Security Testing (SAST) start...
        stage('Dependencies') {
            agent {
                docker {
                    image 'maven:3-alpine'
                    args '-v /root/.m2:/root/.m2'
                }
            }
            steps {
                echo 'Dependency stage'
                script {
                    sh "echo 'Downloading dependencies...'"
                    sh "mvn install -DskipTests=true"
                    sh "echo 'Verifying dependencies...'"
                    sh "mvn dependency-check:check -Dformat=xml"
                    dependencyCheckPublisher pattern: 'dependency-check-report.xml'
                }
            }
        }
        stage('Testing') {
            agent {
                docker {
                    image 'maven:3-alpine'
                    args '-v /root/.m2:/root/.m2'
                }
            }
            steps {
                echo 'Test stage'
                script {
                    sh "echo 'JUnit testing...'"
                    sh "mvn test"
                    sh "echo 'Integration testing...'"
                    sh "mvn test -Dtest=IntegrationTest"
                    jacoco(execPattern: 'target/jacoco.exec')
                }
            }
        }
        stage('Static Analysis (SAST)') {
            environment {
                scannerHome = tool 'SonarQubeScanner'
            }
            steps {
                script {
                    withSonarQubeEnv('sonarqube') {
                        sh "${scannerHome}/bin/sonar-scanner"
                    }
                    sleep(10)
                    qualitygate = waitForQualityGate()
                    if (qualitygate.status != "OK") {
                        error "Pipeline aborted due to quality gate coverage failure: ${qualitygate.status}"
                    }
                }
            }
        }
        // ... ends of SAST
        // Dynamic Application Security Testing (DAST) stages starts...
        stage('Test the image (Pen Testing)') {
            environment {
                NETWORK_NAME="ci-cd_cicd"
                APP_NETWORK_ALIAS="app"
                APP_PORT="8081"
                ZAP_CONTAINER_NAME="ci-cd_zap_1"
            }
            when {
                anyOf {
                    branch RC_BRANCH
                    branch RELEASE_BRANCH
                }
            }
            steps {
                script {
                    level = ""
                    if (env.BRANCH_NAME == RC_BRANCH) {
                        level = "High"
                    } else if (env.BRANCH_NAME == RELEASE_BRANCH) {
                        level = "Medium"
                    } else {
                        error "Pipeline error, impossible to Pen Testing an image on branch ${env.BRANCH_NAME}"
                    }
                    try {
                        pipelineContext.appImage = docker.build(DOCKER_REPOSITORY, ".")
                        pipelineContext.appContainer = pipelineContext.appImage.run("--network=${NETWORK_NAME} --network-alias=${APP_NETWORK_ALIAS}")
                        sh "docker exec ${ZAP_CONTAINER_NAME}  zap-cli --verbose quick-scan http://${APP_NETWORK_ALIAS}:${APP_PORT} -l ${level}"
                    } catch (Exception e) {
                        error "Pipeline aborted due to quality policy, ZAP report has more information"
                    } finally {
                        pipelineContext.appContainer.stop()
                    }
                }
            }
        }
        stage('Publish Release') {
            environment {
                FILE_OUTPUT_TYPE='json'
                FILE_OUTPUT_NAME='results.json'
            }
            when {
                anyOf {
                    branch RC_BRANCH
                    branch RELEASE_BRANCH
                }
            }
            steps{
                script {
                    if (env.BRANCH_NAME == RC_BRANCH) {
                        MSG_CASE = 'Publishing Release Candidate'
                        SEVERITY_BLOCK = 'CRITICAL'   // criteria for RC, just blocks only if finds CRITICAL
                        CONTAINER_VERSION = env.BUILD_NUMBER+'-RC'
                    } else if (env.BRANCH_NAME == RELEASE_BRANCH) {
                        MSG_CASE = 'Publishing Release'
                        SEVERITY_BLOCK = 'CRITICAL,HIGH'   // criteria for release, blocks if finds CRITICAL or HIGH
                        CONTAINER_VERSION = env.BUILD_NUMBER
                    } else {
                        error "Pipeline error, impossible to create an image on branch ${env.BRANCH_NAME}"
                    }
                    echo MSG_CASE
                    dockerImage = docker.build(DOCKER_REPOSITORY)
                    echo 'Cleaning vulnerability scanner...'
                    sh "docker run -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy --clear-cache"
                    echo 'Vulnerability Scanner for this container before push.'
                    sh "docker run -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy --exit-code 1 --severity ${SEVERITY_BLOCK} -f ${FILE_OUTPUT_TYPE} -o ${FILE_OUTPUT_NAME} ${DOCKER_REPOSITORY}:latest"
                    docker.withRegistry("", "docker_hub_login") {
                        dockerImage.push("${CONTAINER_VERSION}")
                    }
                    echo "Container ${DOCKER_REPOSITORY}:${CONTAINER_VERSION} pushed: OK"
                }
            }
        }
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying...not implemented yet'
                
            }
        }
    }
}

