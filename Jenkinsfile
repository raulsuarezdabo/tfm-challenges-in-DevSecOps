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
                post {
                    junit "**/build/test-results/*.xml"
                    step([
                         $class           : 'JacocoPublisher',
                         execPattern      : 'build/jacoco/jacoco.exec',
                         classPattern     : 'build/classes/main',
                         sourcePattern    : 'src/main/java',
                         exclusionPattern : '**/*Test.class'
                    ])
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
