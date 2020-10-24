pipeline {
    agent any
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
                    sh "mvn test"
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
    }
}
