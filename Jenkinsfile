pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo 'Downloading the dependencies..'
                script {
                    sh "mvn install -DskipTests=true"
                }
            }
        }
        stage('Hello') {
            steps {
                echo 'hello..'
                script {
                    sh "echo 'world'"
                }
            }
        }
    }
}
