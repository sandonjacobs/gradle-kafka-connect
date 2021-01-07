package com.github.sandonjacobs.gradle.tasks

import com.github.sandonjacobs.gradle.ConnectRest
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

@Slf4j
class ListConnectorsTask extends DefaultTask {

    ListConnectorsTask() {
        group = project.extensions.kafkaConnect.taskGroup
        description = "List all kafka Connectors deployed to the cluster."
    }

    @TaskAction
    def listConnectors() {
        def rest = new ConnectRest()
        rest.setRestUrl(project.extensions.kafkaConnect.connectEndpoint)
        def result = rest.listConnectors()

        if (result.status.toInteger() != 200) {
            throw new GradleException("List Connectors FAILED: ${result.toMapString()}")
        }

        result.body.toString().split(",").toList().forEach {
            b -> println(b)
        }
    }
}
