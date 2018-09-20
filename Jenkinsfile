pipeline {
    agent any
    stages {
        stage('Checkout'){
            steps{
                checkout scm
            }
        }
        stage('Maven version') {
            steps {
                sh "mvn --version" // Runs a Bourne shell script, typically on a Unix node
            }
        }
        stage('Build project') {
            steps{
                sh "mvn clean install"
            }
        }
        stage('Create tar.gz') {
            steps{
                dir('target'){
                    sh 'tar -zvcf package.tar.gz *.jar *.properties *.sh lib/ scripts/'
                }
            }
        }
    }
}