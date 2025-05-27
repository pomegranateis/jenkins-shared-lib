def call(Map config = [:]) {
    def imageName = config.imageName ?: 'pomegranatei/practical7'

    pipeline {
        agent any

        tools {
            nodejs 'NodeJS-20.x'
        }

        environment {
            IMAGE_NAME = "${imageName}"  // <- MAKE SURE it's in double quotes!
        }

        stages {
            stage('Install') {
                steps {
                    sh 'npm install'
                }
            }

            stage('Build') {
                steps {
                    sh 'npm run build'
                }
            }

            stage('Test') {
                steps {
                    sh 'npm test'
                }
            }

            stage('Docker Build') {
                steps {
                    sh 'docker build -t $IMAGE_NAME:$BUILD_NUMBER .'
                }
            }

            stage('Docker Push') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh '''
                            echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                            docker push $IMAGE_NAME:$BUILD_NUMBER
                            docker logout
                        '''
                    }
                }
            }
        }
    }
}
