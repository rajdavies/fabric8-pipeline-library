#!/usr/bin/groovy
import io.fabric8.GitUtils

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.delegate = config
    body()
    def doChangeLog = config.changeLog ?: false

    if (doChangeLog) {
        def gu = new GitUtils();
        def changes = gu.getChangeLog(env.JOB_NAME,pwd())

        echo" CHANGES !!!! = ${changes}"
    }

}
