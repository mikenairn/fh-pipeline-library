#!/usr/bin/groovy
import org.feedhenry.Utils

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def utils = new Utils()

    def repos = config.repos ?: [:]

    for (def repo in utils.mapToList(repos)) {
        def repoName = repo[0]
        def repoUrl = null
        def repoClustersDir = null
        if(repo[1] instanceof Map) {
            repoUrl = repo[1]['url']
            repoClustersDir = repo[1]['clustersDir']
        } else {
            repoUrl = repo[1]
            repoClustersDir = 'clusters'
        }
        fhcapRepoAdd {
            name = repoName
            url = repoUrl
            clustersDir = repoClustersDir
        }
    }

}
