pipeline {
    agent any
    environment {
        AWS_ACCESS_KEY_ID = credentials('jenkins_aws_access_key')
        AWS_SECRET_ACCESS_KEY = credentials('jenkins_aws_secret_key')
    }
    stages {
        stage('Checkout AWSTerraform scenario repo') {
            steps{
                dir('aws'){
                    git branch: 'master', url: 'https://github.com/ViacheslavRomanov/AWSTerraform.git'
                }
            }
        }
        stage('Init update') {
            steps {
                dir ('aws/upd') {
                    sh 'terraform init'
                }
            }
        }
        stage('Plan update') {
            steps {
                withCredentials([
                        file(credentialsId: 'ec2_aws_pub', variable: 'KEY_PATH')
                ]) {
                    dir('aws/upd') {
                        sh "cp ${KEY_PATH} aws_pub.key"
                        sh "export TF_VAR_ec2_key_path='aws_pub.key' && terraform plan -input=false -out=tfplan"
                        sh "rm aws_pub.key"
                    }
                }
            }
        }
        stage('Retrieve env_vars') {
            steps {
                withCredentials([
                        file(credentialsId: 'ec2_aws_pub', variable: 'KEY_PATH')
                ]) {
                    dir ('aws/upd') {
                        sh "cp ${KEY_PATH} aws_pub.key"
                        sh "export TF_VAR_ec2_key_path='aws_pub.key' && terraform apply -input=false tfplan"
                        sh "rm aws_pub.key"
                    }
                }
            }
        }
        stage('Create app AMI') {
            steps {
                script {
                    AWS_REGION=sh(
                            script: 'source aws/upd/my_env && echo $TF_VAR_aws_region',
                            returnStdout: true
                    ).trim()
                }
                sh "echo ${AWS_REGION}"
                build job: 'create_app_image', parameters: [
                        string(name: 'AWS_REGION', value: "${AWS_REGION}")
                ]
            }
        }
        stage('Mark modified resource') {
            steps {
                withCredentials([
                        file(credentialsId: 'ec2_aws_pub', variable: 'KEY_PATH')
                ]) {
                    dir('aws/stage') {
                        sh "cp ${KEY_PATH} aws_pub.key"
                        sh "export TF_VAR_ec2_key_path='aws_pub.key' && source ../upd/my_env && terraform init &&  terraform taint -module=asg aws_autoscaling_group.asg"
                        sh "rm aws_pub.key"
                    }
                }
            }
        }
        stage('Apply modification') {
            steps {
                withCredentials([
                        file(credentialsId: 'ec2_aws_pub', variable: 'KEY_PATH')
                ]) {
                    dir ('aws/stage') {
                        sh "cp ${KEY_PATH} aws_pub.key"
                        sh "export TF_VAR_ec2_key_path='aws_pub.key'&& source ../upd/my_env && terraform plan -input=false -out=tfplan && terraform apply -input=false tfplan && rm ../upd/my_env"
                        sh "rm aws_pub.key"
                    }
                }
            }
        }
        stage('Clear tmp plan') {
            steps {
                withCredentials([
                        file(credentialsId: 'ec2_aws_pub', variable: 'KEY_PATH')
                ]) {
                    dir ('aws/upd') {
                        sh "cp ${KEY_PATH} aws_pub.key"
                        sh "export TF_VAR_ec2_key_path='aws_pub.key' && terraform destroy -auto-approve"
                        sh "rm aws_pub.key"
                    }
                }
            }
        }
    }
}