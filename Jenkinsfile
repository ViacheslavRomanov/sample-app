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
                    withCredentials([string(credentialsId: 'jenkins_aws_access_key', variable: 'ACCESS_KEY'),
                                     string(credentialsId: 'jenkins_aws_secret_key', variable: 'SECRET_KEY')]){
                        sh "sudo packer validate \
                            -var 'aws_access_key=$ACCESS_KEY' \
                            -var 'aws_secret_key=$SECRET_KEY' \
                            -var 'aws_region=${AWS_REGION}' \
                            app_ebs.json"
                        sh "sudo packer build \
                            -var 'aws_access_key=$ACCESS_KEY' \
                            -var 'aws_secret_key=$SECRET_KEY' \
                            -var 'aws_region=${AWS_REGION}' \
                            app_ebs.json"
                    }
                }
            }
        }

    }
}