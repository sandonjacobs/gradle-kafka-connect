package com.github.sandonjacobs.gradle.tasks

import com.github.sandonjacobs.gradle.ConnectRest
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

@Slf4j
class SubmitConnectorsTask extends DefaultTask {

    SubmitConnectorsTask() {
        group = project.extensions.kafkaConnect.taskGroup
        description = "Load Kafka Connector Configuration files."

        outputs.upToDateWhen { false }
    }


    @TaskAction
    def loadConnectors() {
        def endpoint = project.extensions.kafkaConnect.connectEndpoint
        def rest = new ConnectRest()

        def listing = new File(project.extensions.kafkaConnect.getConnectorsPath())
        listing.eachFile { f ->
            logger.debug(f.name)
            def jsonnetOutput = jsonnet(f).toString()

            def name = new JsonSlurper().parseText(jsonnetOutput).name

            println("Creating Connector (${name} to (${endpoint}):\n$jsonnetOutput")
            rest.execCreateConnector(jsonnetOutput, [:]) // todo: temp empty map
        }
    }

    def jsonnet(File f) {
        def sout = new StringBuilder(), serr = new StringBuilder()
        def proc = "jsonnet ${f.path}".execute()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(1000)
        logger.debug "out> $sout\nerr> $serr"

        if (serr.length()  > 0) {
            throw new GradleException("ERROR Calling JSONNET for file ${f.name}: $serr")
        }
        else {
            return sout
        }
    }
}
