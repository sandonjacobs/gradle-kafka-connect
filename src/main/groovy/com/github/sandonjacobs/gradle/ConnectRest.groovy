package com.github.sandonjacobs.gradle

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import kong.unirest.HttpResponse
import kong.unirest.Unirest


@Slf4j
class ConnectRest {

    String restUrl

    def execCreateConnector(String payload, Map properties) {
        log.debug("creating connector\n$payload")
        def path = "$restUrl/connectors"

        HttpResponse<String> response = Unirest.post(path)
                .header("Content-Type", "application/json")
                .header("Cache-Control", "no-cache")
                .body(payload)
                .asJson()

        log.debug("unirest response : ${response.dump()}")

        def result = [
                status: response.status,
                statusText : response.statusText
        ]

        return result
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
