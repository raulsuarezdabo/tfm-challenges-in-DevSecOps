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
                ZAP_FILE_REPORT="zap-owasp-report.html"
            }
            when {
                anyOf {
                    branch RC_BRANCH
                    branch RELEASE_BRANCH
                }
            }
            steps {
                script {
                    try {
                        pipelineContext.appImage = docker.build(DOCKER_REPOSITORY, ".")
                        pipelineContext.appContainer = pipelineContext.appImage.run("--network=${NETWORK_NAME} --network-alias=${APP_NETWORK_ALIAS}")
                        sh "docker exec zap --network=${NETWORK_NAME} zap-cli --verbose quick-scan http://${APP_NETWORK_ALIAS}:${APP_PORT} -l Medium" 
                        //sh "docker exec zap zap-cli --verbose alerts --alert-level Medium -f json | jq length"
                        pipelineContext.currentStage.result = 'SUCCESS'
                    } finally {
                        pipelineContext.appContainer.stop()
                    }
                    sh "docker exec zap zap-cli --verbose report -o /zap/reports/owasp-quick-scan-report.html --output-format html"
                    publishHTML target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: false,
                        keepAll: true,
                        reportDir: '/opt/dast/reports',
                        reportFiles: 'owasp-quick-scan-report.html',
                        reportName: 'Analisis DAST'
                      ]        
                    if pipelineContext.currentStage.result != 'SUCCESS' {
                        error ("Pipeline aborted due to quality policy, ZAP report has more information")
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
                        error "Pipeline error, impossible to create an image on this branch ${env.BRANCH_NAME}"
                    }
                    echo MSG_CASE
                    dockerImage = docker.build(DOCKER_REPOSITORY)
                    echo 'Cleaning vulnerability scanner...'
                    sh "trivy image --clear-cache"
                    echo 'Vulnerability Scanner for this container before push.'
                    sh "trivy image --exit-code 1 --severity ${SEVERITY_BLOCK} -f ${FILE_OUTPUT_TYPE} -o ${FILE_OUTPUT_NAME} ${DOCKER_REPOSITORY}:latest"
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
            environment {
                CLOUDSDK_CORE_DISABLE_PROMPTS=1
                CLUSTER_ZONE="europe-west1-b"
                CLUSTER_ID="cluster-tfm-devsecop-jenkins"
                PROJECT_ID="first-cluster-293016"
            }
            steps {
                echo 'Deploying...'
                withCredentials([[$class: 'FileBinding', credentialsId: 'secret_file', variable: 'KEY_FILE']]) {
                    sh """
                        if [ ! -d $HOME/google-cloud-sdk/bin ]; then
                        rm -rf $HOME/google-cloud-sdk;
                        curl https://sdk.cloud.google.com | bash > /dev/null;
                        fi
                        source $HOME/google-cloud-sdk/path.bash.inc
                        gcloud components update kubectl
                        gcloud version
                        gcloud auth activate-service-account --key-file $KEY_FILE
                        gcloud container clusters get-credentials $CLUSTER_ID --zone $CLUSTER_ZONE --project $PROJECT_ID
                        kubectl apply -f kube.yml
                    """
                }
            }
        }
    }
}

