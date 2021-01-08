package com.github.sandonjacobs.gradle.tasks

import com.github.sandonjacobs.gradle.ConnectRest
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

@Slf4j
class SubmitConnectorsTask extends DefaultTask {

    ConnectRest rest

    @Input
    @Option(option = "connector-endpoint", description = "override the connector config endpoint here.")
    String connectorEndpoint

    @Input
    @Option(option = "print-only", description = "only prints the connector configs, if present do not post to endpoint")
    boolean dryRunFlag


    SubmitConnectorsTask() {
        group = project.extensions.kafkaConnect.taskGroup
        description = "Load Kafka Connector Configuration files."

        outputs.upToDateWhen { false }
    }

    @TaskAction
    def loadConnectors() {
        logger.debug("input param connect-endpoint => {}", connectorEndpoint)
        def endpoint = connectorEndpoint != null ? connectorEndpoint : project.extensions.kafkaConnect.connectEndpoint
        logger.debug("using {} as connector endpoint url", endpoint)
        rest = new ConnectRest()
        rest.setRestUrl(endpoint)

        def listing = new File(project.extensions.kafkaConnect.getConnectorsPath())
        listing.eachFile { f ->
            logger.debug(f.name)
            def jsonnetOutput = jsonnet(f).toString()
            def name = new JsonSlurper().parseText(jsonnetOutput).name
            processPayload(name, jsonnetOutput)
        }
    }

    def processPayload(String name, String payload) {
        println "Connector Name => $name"
        println "Connect Endpoint => ${rest.getRestUrl()}"
        if (dryRunFlag) {
            println "*** DRY RUN ***"
            println payload
        }
        else {
            println payload
            rest.execCreateConnector(payload, [:]) // todo: temp empty map
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
