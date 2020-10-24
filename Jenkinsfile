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
                    try {
                        sh "mvn test"
                    } finally {
                        junit '**/build/test-results/test/*.xml'
                        jacoco(
                            execPattern: '**/target/jacoco.exec',
                            classPattern: '**/coverage/**',
                            sourcePattern: '**/coverage/**',
                            inclusionPattern: '**/*.class'
                        )
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
    }
}
