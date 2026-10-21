// Declarative Jenkins Pipeline for SecureBank
// Comprehensive Test Strategy & DevSecOps Automation
pipeline {
  agent any
  environment {
    IMAGE = "YOUR_DOCKERHUB_USER/securebank"
    TAG   = "${env.BUILD_NUMBER}"
  }
  options {
    timestamps()
    timeout(time: 30, unit: 'MINUTES')
  }
  stages {
    stage('1. Checkout SCM') {
      steps {
        checkout scm
      }
    }

    stage('2. Comprehensive Automated Testing & Coverage') {
      steps {
        echo 'Executing Comprehensive Test Strategy: Functional, Security, Boundary & Logic tests...'
        sh 'node --test --experimental-test-coverage --test-reporter=spec test/bank.test.js'
      }
    }

    stage('3. SonarQube SAST Analysis') {
      steps {
        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
          sh 'sonar-scanner -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.token=$SONAR_TOKEN -Dsonar.projectName="SecureBank"'
        }
      }
    }

    stage('4. Trivy: Filesystem, Secrets & IaC Scan') {
      steps {
        sh 'mkdir -p reports'
        sh 'trivy fs --scanners vuln,secret,misconfig --severity HIGH,CRITICAL --format table -o reports/trivy-fs.txt .'
      }
    }

    stage('5. Multi-Stage Docker Build') {
      steps {
        sh 'docker build -t $IMAGE:$TAG -t $IMAGE:latest .'
      }
    }

    stage('6. Trivy: Container Image Scan (Quality Gate)') {
      steps {
        sh 'trivy image --severity HIGH,CRITICAL --format table -o reports/trivy-image.txt $IMAGE:$TAG'
        // Fails the pipeline if any CRITICAL severity CVE is detected
        sh 'trivy image --severity CRITICAL --exit-code 1 --no-progress $IMAGE:$TAG'
      }
    }

    stage('7. Push Docker Image to Registry') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'U', passwordVariable: 'P')]) {
          sh 'echo "$P" | docker login -u "$U" --password-stdin'
          sh 'docker push $IMAGE:$TAG && docker push $IMAGE:latest'
        }
      }
    }

    stage('8. Deploy to AWS EC2 over SSH') {
      steps {
        sshagent(credentials: ['ec2-ssh']) {
          sh '''ssh -o StrictHostKeyChecking=accept-new ec2-user@$DEPLOY_HOST "
            docker pull $IMAGE:$TAG &&
            (docker rm -f securebank || true) &&
            docker run -d --name securebank --restart unless-stopped -p 80:3000 $IMAGE:$TAG"'''
        }
      }
    }

    stage('9. Post-Deployment Smoke Test') {
      steps {
        sh 'sleep 5 && curl -fsS http://$DEPLOY_HOST/health'
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'reports/*', allowEmptyArchive: true
    }
    success {
      echo 'SecureBank DevSecOps Pipeline completed with 100% Quality Gate pass!'
    }
    failure {
      echo 'Pipeline failed! Security or Quality Gate criteria not met.'
    }
  }
}
