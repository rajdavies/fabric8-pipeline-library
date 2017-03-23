#!/usr/bin/groovy
import io.fabric8.ChangeLog

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.delegate = config
    body()
    def doChangeLog = config.changeLog ?: false

    if (doChangeLog) {
        def cl = new ChangeLog();
        def changes = cl.getChangeLog(env.JOB_NAME,pwd())

        echo" CHANGES !!!! = ${changes}"
    }

}
