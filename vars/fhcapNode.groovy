#!/usr/bin/groovy

def call(Map parameters = [:], body) {

    node('ruby-fhcap') {

        def installLatest = parameters.get('installLatest', false)
        def credentialsId = parameters.get('credentialsId', 'jenkinsgithub')
        def configFile = parameters.get('configFile', "${WORKSPACE}/fhcap.json")
        def gitRepo = parameters.get('gitRepo', null)
        def gitRef = parameters.get('gitRef', 'master')

        step([$class: 'WsCleanup'])

        if(gitRepo) {
            dir('fhcap-cli') {
                checkoutGitRepo {
                    repoUrl = gitRepo
                    branch = gitRef
                }
                sh "rake install"
            }
        } else {
            if(installLatest) {
                sh "gem install fhcap-cli --no-ri --no-rdoc"
            }
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
