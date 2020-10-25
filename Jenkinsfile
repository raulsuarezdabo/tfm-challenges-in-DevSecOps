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
                branch 'jenkins'
            }
            environment {
                CLOUDSDK_CORE_DISABLE_PROMPTS=1
                CLUSTER_ZONE="europe-west1-b"
                CLUSTER_ID="cluster-tfm-devsecop-jenkins"
                PROJECT_ID="first-cluster-293016"
            }
            steps{
                echo 'Deploying...'
                script {
                    """
                        if [ ! -d $HOME/google-cloud-sdk/bin ]; then
                        rm -rf $HOME/google-cloud-sdk;
                        curl https://sdk.cloud.google.com | bash > /dev/null;
                        fi
                        source $HOME/google-cloud-sdk/path.bash.inc
                        gcloud components update kubectl
                        gcloud version
                    """
                }
                withCredentials([[$class: 'FileBinding', credentialsId: 'secret_file', variable: 'KEY_FILE']]) {
                  sh 'gcloud auth activate-service-account --key-file "$KEY_FILE"'
                }
            }
        }
    }
}
