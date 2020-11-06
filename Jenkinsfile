pipeline {
    agent any
    environment {
        DOCKER_REPOSITORY = "raulsuarezdabo/tfm-devsecop-jenkins"
    }
    stages {
        stage('Dependencies') {
            steps {
                echo 'Dependency stage'
                script {
                    sh "echo 'Downloading dependencies...'"
                    sh "mvn install -DskipTests=true"
                    sh "echo 'Verifying dependencies...'"
                    sh "/bin/dependency-check/bin/dependency-check.sh --out . --scan . --format XML"
                    dependencyCheckPublisher pattern: 'dependency-check-report.xml'
                }
            }
        }
        stage('Testing') {
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
        stage('Sonarqube') {
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
        stage('Publish Release Candidate') {
            environment {
                FILE_OUTPUT_TYPE='json'
                FILE_OUTPUT_NAME='results.json'
                SEVERITY_BLOCK='CRITICAL'
            }
            when {
                branch 'devsecop'
            }
            steps{
                echo 'Publishing release candidate...'
                script {
                    dockerImage = docker.build(DOCKER_REPOSITORY)
                    echo 'Vulnerability Scanner for this container before to push.'
                    sh "trivy image --exit-code 1 --severity ${SEVERITY_BLOCK} -f ${FILE_OUTPUT_TYPE} -o ${FILE_OUTPUT_NAME} ${DOCKER_REPOSITORY}:latest"
                    docker.withRegistry("", "docker_hub_login") {
                        dockerImage.push("${env.BUILD_NUMBER}-RC")
                    }
                }
            }
        }
        stage('Publish Release') {
            when {
                branch 'main'
            }
            steps{
                echo 'Publishing release...'
                script {
                    dockerImage = docker.build(DOCKER_REPOSITORY)
                    echo 'Vulnerability Scanner for this container before to push.'
                    sh "trivy image ${DOCKER_REPOSITORY}:latest"
                    docker.withRegistry("", "docker_hub_login") {
                        dockerImage.push("${env.BUILD_NUMBER}")
                    }
                }
            }
        }
        stage('Vulnerability Scanner') {
            steps {
                echo 'Vulnerability Scanner for container...'
                sh "trivy image raulsuarezdabo/tfm-devsecop-jenkins:1"
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

