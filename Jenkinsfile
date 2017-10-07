pipeline {
  docker.withServer('tcp://trap-titania.uni-muenster.de:4243') {
    agent {
      dockerfile {
        filename 'dockerfile'
      }
    }
  }
  stages {
    stage('ASD') {
      steps {
        timestamps()
      }
    }
  }
}