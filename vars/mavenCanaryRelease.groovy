#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def flow = new io.fabric8.Fabric8Commands()

    def skipTests = config.skipTests ?: false

    def profile
    if (flow.isOpenShift()) {
      profile = '-P openshift'
    } else {
      profile = '-P kubernetes'
    }

    sh "git checkout -b ${env.JOB_NAME}-${config.version}"

    sh "mvn org.codehaus.mojo:versions-maven-plugin:2.2:set -U -DnewVersion=${config.version}"
    sh "mvn clean -e -U deploy -Dmaven.test.skip=${skipTests} ${profile}"

    if (flow.hasService("bayesian")) {
        try {
          sh 'mvn io.github.stackinfo:stackinfo-maven-plugin:0.1:prepare'
          def response = bayesianAnalysis()
        } catch (err) {
          echo "Unable to run Bayesian analysis: ${err}"
        }
    }

    //try sonarQube
    sonarQubeScanner(body);


    def s2iMode = flow.isOpenShiftS2I()
    echo "s2i mode: ${s2iMode}"

    if (!s2iMode){
        if (flow.isSingleNode()){
            echo 'Running on a single node, skipping docker push as not needed'
            def m = readMavenPom file: 'pom.xml'
            def groupId = m.groupId.split( '\\.' )
            def user = groupId[groupId.size()-1].trim()
            def artifactId = m.artifactId
            sh "docker tag ${user}/${artifactId}:${config.version} ${env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST}:${env.FABRIC8_DOCKER_REGISTRY_SERVICE_PORT}/${user}/${artifactId}:${config.version}"

        }else{
            retry(3){
                sh "mvn fabric8:push -Ddocker.push.registry=${env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST}:${env.FABRIC8_DOCKER_REGISTRY_SERVICE_PORT}"
            }
        }
    }

    if (flow.hasService("content-repository")) {
      try {
        //sh 'mvn site site:deploy'
        echo 'mvn site disabled'
      } catch (err) {
        // lets carry on as maven site isn't critical
        echo 'unable to generate maven site'
      }
    } else {
      echo 'no content-repository service so not deploying the maven site report'
    }
  }
