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
        }
    }
}