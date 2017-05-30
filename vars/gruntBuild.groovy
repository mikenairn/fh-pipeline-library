#!/usr/bin/groovy

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

    sh "cp output/**/VERSION.txt ."
    def versionTxt = readFile("VERSION.txt").trim()

    writeBuildInfo(name, versionTxt)

    archiveArtifacts "dist/${name}*.tar.gz, ${buildInfoFileName}"
}
