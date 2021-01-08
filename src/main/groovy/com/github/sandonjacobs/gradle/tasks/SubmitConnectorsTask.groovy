package com.github.sandonjacobs.gradle.tasks

import groovy.util.logging.Slf4j

@Slf4j
class SubmitConnectorsTask extends BaseConfigLoadingTask {

    SubmitConnectorsTask() {
        group = project.extensions.kafkaConnect.taskGroup
        description = "Load Kafka Connector Configuration files."

        outputs.upToDateWhen { false }
    }

    @Override
    def processPayload(String name, String payload) {
        println "Connector Name => $name"
        println "Connect Endpoint => ${rest.getRestUrl()}"
        println payload
        rest.execCreateConnector(payload, [:]) // todo: temp empty map
    }
}
