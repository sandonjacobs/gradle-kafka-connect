package com.github.sandonjacobs.gradle.tasks

import groovy.util.logging.Slf4j

@Slf4j
class PrintConnectorsTask extends BaseConfigLoadingTask {

    @Override
    def processPayload(String name, String payload) {
        println "Connector Name => $name"
        println payload
    }
}
