package com.github.sandonjacobs.gradle

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import org.gradle.api.GradleException

@Slf4j
class ConnectRest {

    String restUrl

    def execCreateConnector(String name, String payload, Map properties) {
        log.debug("creating connector => $name\n$payload")
        def path = "$restUrl/connectors/$name/config"

        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parseText(payload)

        def finalPayload =  json.config != null ? json.config : json
        log.info("connector config\n{}", JsonOutput.toJson(finalPayload))

        HttpResponse<String> response = Unirest.put(path)
                .header("Content-Type", "application/json")
                .header("Cache-Control", "no-cache")
                .body(JsonOutput.toJson(finalPayload))
                .asJson()

        def responseStatus = response.getStatus()
        log.debug(">>> response status {} <<<", responseStatus)
        if (responseStatus == 200 || responseStatus == 201) {
            def result = [
                    status: responseStatus,
                    statusText : response.statusText
            ]

            return result
        }
        else {
            log.error("ERROR: Returned a {} response code!!!", responseStatus)
            throw new GradleException("ERROR: Returned a ${ responseStatus} response code!!! ${response.statusText}")
        }
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
