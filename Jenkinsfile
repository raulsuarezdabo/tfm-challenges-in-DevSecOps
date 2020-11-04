pipeline {
    agent any
    environment {
        DOCKER_REPOSITORY = "raulsuarezdabo/tfm-devsecop-jenkins"
    }
    stages {
        stage('Dependencies') {
            steps {
                echo 'Downloading the dependencies..'
                script {
                    sh "mvn install -DskipTests=true"
                }
            }
        }
        stage('Testing') {
            steps {
                echo 'JUnit testing...'
                script {
                    try {
                        sh "mvn test"
                    } finally {
                        jacoco(execPattern: 'target/jacoco.exec')
                    }
                }
            }
        }
        stage('Sonarqube') {
            environment {
                scannerHome = tool 'SonarQubeScanner'
            }
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh "${scannerHome}/bin/sonar-scanner"
                }
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Dependencies Check') {
            steps {
                echo 'Dependencies Check...'
                script {
                    sh "/bin/dependency-check/bin/dependency-check.sh --out . --scan . --format XML"
                }
                dependencyCheckPublisher pattern: 'dependency-check-report.xml'
            }
        }
        stage('Integration Testing') {
            steps {
                echo 'Integration testing...'
                script {
                    sh "mvn test -Dtest=IntegrationTest"
                }
            }
        }
        stage('Publish Release Candidate') {
            when {
                branch 'develop'
            }
            steps{
                echo 'Publishing release candidate...'
                script {
                    dockerImage = docker.build(DOCKER_REPOSITORY)
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
                    docker.withRegistry("", "docker_hub_login") {
                        dockerImage.push("${env.BUILD_NUMBER}")
                    }
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
