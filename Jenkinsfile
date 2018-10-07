pipeline {
    agent any
    environment {
        AWS_ACCESS_KEY_ID = credentials('jenkins_aws_access_key')
        AWS_SECRET_ACCESS_KEY = credentials('jenkins_aws_secret_key')
        AWS_REGION = "${AWS_REGION}"
    }
    stages {
        stage('Checkout'){
            steps{
                checkout scm
            }
        }
        stage('Maven version') {
            steps {
                sh "mvn --version"
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
        stage('Checkout packer scripts repo') {
            steps{
                dir('packer'){
                    git branch: 'master', url: 'https://github.com/ViacheslavRomanov/packer-scripts.git'
                    sh 'cp ../target/package.tar.gz app/package.tar.gz'
                }
            }
        }
        stage('Create app AMI') {
            steps{
                dir('packer/app'){
                    sh "sudo packer validate app_ebs.json"
                    sh "sudo packer build app_ebs.json"
                }
            }
        }

    }
}