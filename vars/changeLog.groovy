#!/usr/bin/groovy
import io.fabric8.ChangeLog

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.delegate = config
    body()
    def doChangeLog = config.changeLog ?: false

    if (doChangeLog) {
        def changelog = new ChangeLog();
        def changes = changeLog.getChangeLog(${env.JOB_NAME},pwd())

        echo" CHANGES !!!! = ${changes}"
    }

}
