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
                step{
                    echo 'JUnit testing...'
                    script {
                        try {
                            sh "mvn test"
                        } finally {
                            junit '**/build/test-results/test/*.xml'
                        }
                    }
                }
                step {
                    - $class: 'JacocoPublisher'
                    - execPattern: '**/build/jacoco/*.exec'
                    - classPattern: '**/build/classes'
                    - sourcePattern: 'src/main/java'
                    - exclusionPattern: 'src/test*'
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
