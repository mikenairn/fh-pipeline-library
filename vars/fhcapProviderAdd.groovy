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

    print config

    print "fhcapProviderAdd1"
    print config.credentials

    def credentials = config.credentials ?: [:]
    def providerConfig = config.providerConfig ?: [:]

    print "fhcapProviderAdd2"
    print credentials

    def credentialsStr = utils.mapToOptionsString(credentials)

    print "fhcapProviderAdd3"
    print credentials

    def providerConfigStr = utils.mapToOptionsString(providerConfig)

    def cmd = "fhcap provider add --name ${name} --type ${type} --credentials ${credentialsStr}"
    cmd += providerConfigStr.trim() ? " --provider-config ${providerConfigStr.trim()}" : ''
    sh cmd
}
