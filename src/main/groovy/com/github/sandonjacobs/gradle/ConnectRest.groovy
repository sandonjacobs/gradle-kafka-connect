package com.github.sandonjacobs.gradle

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import kong.unirest.HttpResponse
import kong.unirest.Unirest


@Slf4j
class ConnectRest {

    /**
     * The base REST endpoint of kafka connect API. Defaults to 'http://localhost:8083'.
     */
    String restUrl = 'http://localhost:8083'

    def execCreateConnector(String payload, Map properties) {

    }

    def deleteConnector(String name) {

    }

    def connectorExists(String name) {

    }

    def listConnectors() {
        def path = "$restUrl/connectors"

        HttpResponse<String> response = Unirest.get(path)
                .header("Content-Type", "application/json")
                .header("Cache-Control", "no-cache")
        .asString()

        log.debug("unirest response : ${response.dump()}")
        def body = new JsonSlurper().parseText(response.body)

        def result = [
                status: response.status,
                statusText : response.statusText,
                body: body
        ]

        return result
    }
}
