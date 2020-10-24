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
    }
}
