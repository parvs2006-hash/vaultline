// Declarative pipeline. Jenkins prerequisites:
//  - Node 20 + Docker + Trivy + sonar-scanner on the agent
//  - Credentials: 'sonar-token' (Secret text), 'dockerhub-creds' (user/pass), 'ec2-ssh' (SSH key)
//  - Jenkins "Environment variables": SONAR_HOST_URL, DEPLOY_HOST
pipeline {
  agent any
  environment {
    IMAGE = "YOUR_DOCKERHUB_USER/vaultline"
    TAG   = "${env.BUILD_NUMBER}"
  }
  options { timestamps(); timeout(time: 30, unit: 'MINUTES') }
  stages {
    stage('Checkout')      { steps { checkout scm } }
    stage('Install & Test') {
      steps {
        sh 'npm install --no-audit --no-fund'
        sh 'mkdir -p coverage && node --test --experimental-test-coverage --test-reporter=spec --test-reporter=lcov --test-reporter-destination=stdout --test-reporter-destination=coverage/lcov.info test/*.test.js'
      }
    }
    stage('SonarQube Analysis') {
      steps {
        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
          sh 'sonar-scanner -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.token=$SONAR_TOKEN'
        }
      }
    }
    stage('Trivy: dependencies & IaC') {
      steps {
        sh 'mkdir -p reports'
        sh 'trivy fs --scanners vuln,secret,misconfig --severity HIGH,CRITICAL --format table -o reports/trivy-fs.txt .'
      }
    }
    stage('Docker Build') { steps { sh 'docker build -t $IMAGE:$TAG -t $IMAGE:latest .' } }
    stage('Trivy: image') {
      steps {
        sh 'trivy image --severity HIGH,CRITICAL --format table -o reports/trivy-image.txt $IMAGE:$TAG'
        // Fail the build on CRITICAL findings (quality gate)
        sh 'trivy image --severity CRITICAL --exit-code 1 --no-progress $IMAGE:$TAG'
      }
    }
    stage('Push Image') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'U', passwordVariable: 'P')]) {
          sh 'echo "$P" | docker login -u "$U" --password-stdin'
          sh 'docker push $IMAGE:$TAG && docker push $IMAGE:latest'
        }
      }
    }
    stage('Deploy to AWS EC2') {
      steps {
        sshagent(credentials: ['ec2-ssh']) {
          sh '''ssh -o StrictHostKeyChecking=accept-new ec2-user@$DEPLOY_HOST "
            docker pull $IMAGE:$TAG &&
            (docker rm -f vaultline || true) &&
            docker run -d --name vaultline --restart unless-stopped -p 80:3000 $IMAGE:$TAG"'''
        }
      }
    }
    stage('Smoke Test') { steps { sh 'sleep 5 && curl -fsS http://$DEPLOY_HOST/health' } }
  }
  post {
    always { archiveArtifacts artifacts: 'reports/*', allowEmptyArchive: true }
  }
}
