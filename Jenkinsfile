// ---------- Helpers ----------
def utcCreated() {
    sh(script: "date -u +%Y-%m-%dT%H:%M:%SZ", returnStdout: true).trim()
}

def normalizeRepoUrl(String rawRepo) {
    def repoUrl = rawRepo ?: ''
    if (repoUrl.startsWith('git@github.com:')) {
        repoUrl = repoUrl.replace('git@github.com:', 'https://github.com/')
    }
    repoUrl.replaceAll(/\.git$/, '')
}

def commitUrl(String repoUrl, String commitSha) {
    (repoUrl && commitSha) ? "${repoUrl}/commit/${commitSha}" : (repoUrl ?: '')
}

def guessRefName() {
    env.CHANGE_ID ? "PR-${env.CHANGE_ID}" : (env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown')
}

@NonCPS
Map resolveDeployContext(String branchName, String changeId, String changeTarget) {
    final boolean isPr = (changeId != null)

    if (branchName == 'main') {
        return (isPr)
            ? [stage: 'staging',     environment: 'preview']
            : [stage: 'production',  environment: 'production']
    }
    return [stage: 'development', environment: 'development']
}

def ociLabelArgs(String tag) {
    def created = utcCreated()
    def repoUrl = normalizeRepoUrl(env.GIT_URL ?: '')
    def url     = commitUrl(repoUrl, env.GIT_COMMIT ?: '')
    def refName = guessRefName()
    def ctx     = resolveDeployContext(env.BRANCH_NAME, env.CHANGE_ID, env.CHANGE_TARGET)

    def baseImage = "eclipse-temurin:25-jre-alpine"
    def docsUrl   = "https://github.com/nguyendinhthanhh/mentorx-be"

    def labels = [
        "org.opencontainers.image.source=${repoUrl}",
        "org.opencontainers.image.revision=${env.GIT_COMMIT ?: ''}",
        "org.opencontainers.image.url=${url}",
        "org.opencontainers.image.created=${created}",
        "org.opencontainers.image.version=${tag}",
        "org.opencontainers.image.ref.name=${refName}",

        "org.opencontainers.image.title=mentorx-api",
        "org.opencontainers.image.description=Mentor X Backend API - Spring Boot 3.2",
        "org.opencontainers.image.vendor=MentorX",
        "org.opencontainers.image.documentation=${docsUrl}",
        "org.opencontainers.image.authors=nguyendinhthanhh",

        "org.opencontainers.image.base.name=${baseImage}",
        "org.opencontainers.image.build.source=jenkins",
        "org.opencontainers.image.build.version=${env.JENKINS_VERSION ?: 'unknown'}",

        "org.opencontainers.image.stage=${ctx.stage}",
        "org.opencontainers.image.environment=${ctx.environment}",
    ].collect { "--label \"${it}\"" }.join(' ')

    return "-f Dockerfile ${labels}"
}

// ---------- Maven wrapper ----------
def mvnCompile() {
    sh './mvnw -q -DskipTests compile'
}

def mvnTest() {
    sh './mvnw -q test'
}

def mvnPackage() {
    sh './mvnw -q -DskipTests package'
}

// ---------- Docker wrapper (AUTO CLEANUP) ----------
def withDockerImage(String tag, Closure body) {
    try {
        body()
    } finally {
        sh "docker rmi ${env.IMAGE}:${tag} --force || true"
    }
}

def dockerBuildAndPush(String tag) {
    withDockerImage(tag) {
        def img = docker.build("${env.IMAGE}:${tag}","${ociLabelArgs(tag)} .")
        docker.withRegistry(env.REGISTRY_URL, env.REGISTRY_CREDENTIAL) {
            img.push(tag)
        }
    }
}

// ---------- Pipeline (single-branch: main only) ----------
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
        NOTIFY_TO           = 'team@mentorx.local'
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        // ── PR Gating: compile + test in parallel on every PR to main ──
        stage('PR Validation') {
            when { changeRequest target: 'main' }
            parallel {
                stage('Compile Check') {
                    steps { script { mvnCompile() } }
                }
                stage('Test Check') {
                    steps {
                        script { mvnTest() }
                        junit 'target/surefire-reports/**/*.xml'
                    }
                }
            }
        }

        // ── Deploy Pipeline: runs on merge to main ──
        stage('Package') {
            when {
                allOf {
                    branch 'main'
                    not { changeRequest() }
                }
            }
            steps {
                script {
                    def sha = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                    env.SHORT_SHA = sha
                    mvnPackage()
                }
            }
        }

        stage('Publish Images') {
            when {
                allOf {
                    branch 'main'
                    not { changeRequest() }
                }
            }
            steps {
                script {
                    dockerBuildAndPush('latest')
                    dockerBuildAndPush("${env.SHORT_SHA}")
                }
            }
        }

        stage('Deploy') {
            when {
                allOf {
                    branch 'main'
                    not { changeRequest() }
                }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'portainer-mentorx-webhook', variable: 'PORTAINER_WEBHOOK')
                ]) {
                    sh 'curl -fsS -X POST "$PORTAINER_WEBHOOK"'
                }
            }
        }

        stage('Cleanup') {
            steps {
                script {
                    sh "docker images --filter \"dangling=true\" --filter \"reference=${IMAGE}\" -q | xargs -r docker rmi || true"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            mail to: "${env.NOTIFY_TO}",
                 subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Branch: ${env.BRANCH_NAME}\nCommit: ${env.GIT_COMMIT}\nURL: ${env.BUILD_URL}"
        }
        fixed {
            mail to: "${env.NOTIFY_TO}",
                 subject: "FIXED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Branch: ${env.BRANCH_NAME}\nCommit: ${env.GIT_COMMIT}\nURL: ${env.BUILD_URL}"
        }
    }
}
