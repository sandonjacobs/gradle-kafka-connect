package com.github.sandonjacobs.gradle.tasks

import com.github.sandonjacobs.gradle.ConnectRest
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

abstract class BaseConfigLoadingTask extends DefaultTask {

    ConnectRest rest

    BaseConfigLoadingTask() {
        def endpoint = project.extensions.kafkaConnect.connectEndpoint
        rest = new ConnectRest()
        rest.setRestUrl(endpoint)
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

    @TaskAction
    def loadConnectors() {
        def endpoint = project.extensions.kafkaConnect.connectEndpoint

        def listing = new File(project.extensions.kafkaConnect.getConnectorsPath())
        listing.eachFile { f ->
            logger.debug(f.name)
            def jsonnetOutput = jsonnet(f).toString()
            def name = new JsonSlurper().parseText(jsonnetOutput).name
            processPayload(name, jsonnetOutput)
        }
    }

    abstract def processPayload(String name, String payload)
}
