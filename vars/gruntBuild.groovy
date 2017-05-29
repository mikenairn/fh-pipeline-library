#!/usr/bin/groovy

import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def name = config.name
    def distCmd = config.distCmd ?: 'fh:dist --only-bundle-deps'

    gruntCmd {
        cmd = distCmd
    }

    def versionTxt = readFile "output/**/VERSION.txt"

    def buildInfoFileName = 'build-info.json'
    def buildInfo = fileExists(buildInfoFileName) ? new JsonSlurperClassic().parseText(readFile(buildInfoFileName)) : [:]

    def (version,build) = versionTxt.trim().split('-')
    println version
    println build

    buildInfo[name] = [:]
    buildInfo[name]['version'] = versionTxt[0]
    buildInfo[name]['build'] = env.BUILD_NUMBER

    writeFile file: buildInfoFileName, text: new JsonBuilder(buildInfo).toPrettyString()

    archiveArtifacts "dist/${name}*.tar.gz, ${buildInfoFileName}"
}
