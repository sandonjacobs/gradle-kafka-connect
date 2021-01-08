package com.github.sandonjacobs.gradle.tasks

import com.github.sandonjacobs.gradle.ConnectRest
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

@Slf4j
class ListConnectorsTask extends DefaultTask {

    @Input
    @Optional
    @Option(option = "connector-endpoint", description = "override the connector config endpoint here.")
    String connectorEndpoint = project.extensions.kafkaConnect.connectEndpoint

    ListConnectorsTask() {
        group = project.extensions.kafkaConnect.taskGroup
        description = "List all kafka Connectors deployed to the cluster."

        outputs.upToDateWhen { false }
    }

    @TaskAction
    def listConnectors() {
        logger.debug("using {} as connector endpoint url", connectorEndpoint)
        ConnectRest rest = new ConnectRest()
        rest.setRestUrl(connectorEndpoint)

        def result = rest.listConnectors()

        if (result.status.toInteger() != 200) {
            throw new GradleException("List Connectors FAILED: ${result.toMapString()}")
        }

        result.body.toString().split(",").toList().forEach {
            b -> println(b)
        }
    }
}
