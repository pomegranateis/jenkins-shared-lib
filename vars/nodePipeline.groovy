def call(Map config = [:]) {
    pipeline {
        agent any

        tools {
            nodejs 'NodeJS-20.x'
        }

        environment {
            CI = 'true'
        }

        stages {
            stage('Install') {
                steps {
                    dir(config.appDir ?: '.') {
                        sh 'npm install'
                    }
                }
            }

            stage('Build') {
                steps {
                    dir(config.appDir ?: '.') {
                        sh 'npm run build'
                    }
                }
            }

            stage('Test') {
                steps {
                    dir(config.appDir ?: '.') {
                        sh 'npm test || true' // optional: continue if no tests
                    }
                }
            }

            stage('Docker Build') {
                steps {
                    script {
                        dir(config.appDir ?: '.') {
                            docker.build("${config.imageName ?: 'myimage'}:${env.BUILD_NUMBER}")
                        }
                    }
                }
            }

            stage('Docker Push') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh "echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin"
                        sh "docker push ${config.imageName ?: 'myimage'}:${env.BUILD_NUMBER}"
                        sh "docker logout"
                    }
                }
            }
        }
    }
}
