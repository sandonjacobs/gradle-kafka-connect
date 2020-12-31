package com.github.sandonjacobs.gradle.tasks

import com.github.sandonjacobs.gradle.ConnectRest
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

@Slf4j
class ListConnectorsTask extends DefaultTask {

    ListConnectorsTask() {
        group = project.extensions.kafkaConnect.taskGroup
        description = "List all kafka Connectors deployed to the cluster."
    }

    @TaskAction
    def listConnectors() {
        new ConnectRest().listConnectors().each {c ->
            println "$c"
        }
    }
}
