node {
    checkout scm

    docker.withServer('tcp://trap-titania.uni-muenster.de:4243') {
        docker.image('mysql:5').withRun('-p 3306:3306') {
            /* do things */
        }
    }
}