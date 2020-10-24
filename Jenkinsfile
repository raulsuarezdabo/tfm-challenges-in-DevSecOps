pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo 'Running build automation'
                sh './gradlew build'
            }
        }
        stage('Hello') {
            steps {
                echo 'Hello world'
            }
        }
    }
}
