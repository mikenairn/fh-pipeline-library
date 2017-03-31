#!/usr/bin/groovy

def call(Map parameters = [:], body) {

    node('ruby-fhcap') {

        def installLatest = parameters.get('installLatest', false)
        def credentialsId = parameters.get('credentialsId', 'jenkinsgithub')
        def configFile = parameters.get('configFile', "${WORKSPACE}/fhcap.json")

        step([$class: 'WsCleanup'])

        if(installLatest) {
            sh "gem install fhcap-cli --no-ri --no-rdoc"
        }
        sh "fhcap --version"

        env.PATH = "${PATH}:/home/jenkins/bin"
        env.FHCAP_CFG_FILE = configFile

        sshagent([credentialsId]) {
            sh "fhcap setup --repos-dir ${WORKSPACE}"
        }

        body()
    }
}
