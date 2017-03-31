#!/usr/bin/groovy
import org.feedhenry.Utils

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def utils = new Utils()

    def name = config.name
    def type = config.type
    def credentials = config.credentials ?: [:]
    def providerConfig = config.providerConfig ?: [:]

    def credentialsStr = utils.mapToOptionsString(credentials)
    def providerConfigStr = utils.mapToOptionsString(providerConfig)

    sh "fhcap provider add --name ${name} --type ${type} --credentials ${credentialsStr} --provider-config ${providerConfigStr}"
}
