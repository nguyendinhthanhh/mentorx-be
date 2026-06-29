def dockerTag = {
    def sha = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
    ["latest", sha]
}

pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
        timeout(time: 1, unit: 'HOURS')
    }

    environment {
        IMAGE               = 'thekhiem7/mentorx-api'
        REGISTRY_CREDENTIAL = 'thekhiem7-dockerhub-credentials'
        REGISTRY_URL        = 'https://index.docker.io/v1/'
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build & Push') {
            steps {
                script {
                    def tags = dockerTag()
                    def img = docker.build("${IMAGE}:${tags[0]}", ".")
                    docker.withRegistry(REGISTRY_URL, REGISTRY_CREDENTIAL) {
                        img.push(tags[0])
                        img.push(tags[1])
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([
                    string(credentialsId: 'portainer-mentorx-webhook', variable: 'PORTAINER_WEBHOOK')
                ]) {
                    sh 'curl -fsS -X POST "$PORTAINER_WEBHOOK"'
                }
            }
        }
    }

    post {
        always { cleanWs() }
    }
}
