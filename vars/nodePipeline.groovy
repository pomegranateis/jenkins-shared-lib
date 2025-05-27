def call(Map config = [:]) {
    pipeline {
        agent any

        tools {
            nodejs 'NodeJS-20.x'
        }

        environment {
            IMAGE_NAME = config.imageName ?: 'pomegranatei/practical7'
        }

        stages {
            stage('Install') {
                steps {
                    sh 'npm install'
                }
            }

            stage('Test') {
                steps {
                    sh 'npm test || echo "No tests found"'
                }
            }

            stage('Build Docker') {
                steps {
                    script {
                        dockerImage = docker.build("${IMAGE_NAME}:${env.BUILD_NUMBER}")
                    }
                }
            }

            stage('Push Docker') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh '''
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                            docker push $IMAGE_NAME:$BUILD_NUMBER
                            docker logout
                        '''
                    }
                }
            }
        }
    }
}
