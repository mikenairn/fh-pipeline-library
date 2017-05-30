#!/usr/bin/groovy

import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic

def call(name, versionTxt) {
    def buildInfoFileName = 'build-info.json'
    def buildInfo = fileExists(buildInfoFileName) ? new JsonSlurperClassic().parseText(readFile(buildInfoFileName)) : [:]

    buildInfo['jenkinsUrl'] = env.JENKINS_URL
    buildInfo['buildUrl'] = env.BUILD_URL

    buildInfo[name] = [:]
    buildInfo[name]['version'] = versionTxt.split('-')[0]
    buildInfo[name]['build'] = env.BUILD_NUMBER

    writeFile file: buildInfoFileName, text: new JsonBuilder(buildInfo).toPrettyString()
}
