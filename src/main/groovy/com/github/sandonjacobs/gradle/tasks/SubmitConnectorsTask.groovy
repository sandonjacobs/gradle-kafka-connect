package com.github.sandonjacobs.gradle.tasks

import com.github.sandonjacobs.gradle.ConnectRest
import com.github.sandonjacobs.gradle.utils.JsonnetUtils
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

@Slf4j
class SubmitConnectorsTask extends DefaultTask {

    @Input
    @Optional
    @Option(option = "connector-endpoint", description = "override the connector config endpoint here.")
    String connectorEndpoint = project.extensions.kafkaConnect.connectEndpoint

    @Input
    @Optional
    @Option(option = "print-only", description = "only prints the connector configs, if present do not post to endpoint")
    Boolean dryRunFlag

    @Input
    @Optional
    @Option(option = "connector-subdir", description = "Only project a given subdirectory of the `sourceBase/connectorSourceName` directory")
    String connectorSubDir

    @Input
    @Optional
    @Option(option = "jsonnet-tla-str-args", description = "Additional jsonnet args, maps to --tla-str <var>[=<val>].")
    String tlaStringArgs

    @Input
    @Optional
    @Option(option = "jsonnet-ext-code-file", description = "Maps to --ext-code-file in jsonnet.")
    String extCodeFile

    @Input
    @Optional
    @Option(option = "connector", description = "If set, only process this specified connector jsonnet file in the connectorSubDir.")
    String connectorName

    SubmitConnectorsTask() {
        group = project.extensions.kafkaConnect.taskGroup
        description = "Load Kafka Connector Configuration files."

        outputs.upToDateWhen { false }
    }

    @TaskAction
    def loadConnectors() {

        String pathBase = project.extensions.kafkaConnect.getConnectorsPath()
        logger.debug("connector sub dir  => {}", connectorSubDir)
        String connectorPath = connectorSubDir ? pathBase + '/' + connectorSubDir : pathBase

        logger.debug("input param connect-endpoint => {}", connectorEndpoint)
        ConnectRest rest = new ConnectRest()
        rest.setRestUrl(connectorEndpoint)

        logger.debug("******************************")
        logger.debug("pathBae            => {}", pathBase)
        logger.debug("connectorPath      => {}", connectorPath)
        logger.debug("connector sub dir  => {}", connectorSubDir)
        logger.debug("connector name     => {}", connectorName)
        logger.debug("******************************")
        
        if (connectorName) {
            logger.info("processing connector {} from directory {}", connectorName, connectorPath)
            def file = new File("${connectorPath}/${connectorName}.jsonnet")
            processFile(rest, connectorPath, file)
        } else {
            logger.info("processing connectors from directory {}", connectorPath)
            def listing = new File(connectorPath)
            listing.eachFile { f ->
                logger.debug(f.name)
                if (!f.isDirectory() && f.name.endsWith(".jsonnet")) {
                    logger.debug(f.name)
                    processFile(rest, connectorPath, f)
                }
            }
        }
    }

    def processFile(ConnectRest rest, String connectorPath, File f) {
        def jsonnetOutput = jsonnet(f, connectorPath).toString()
        def name = new JsonSlurper().parseText(jsonnetOutput).name
        processPayload(rest, name, jsonnetOutput)
    }

    def processPayload(ConnectRest rest, String name, String payload) {
        println "Connector Name => $name"
        println "Connect Endpoint => ${rest.getRestUrl()}"
        if (dryRunFlag) {
            println "*** DRY RUN ***"
            println payload
        }
        else {
            sleep(3000)
            logger.info(payload)
            rest.execCreateConnector(name, payload, [:]) // todo: temp empty map
        }
    }

    def jsonnet(File f, String connectorPath) {
        def sout = new StringBuilder(), serr = new StringBuilder()

        def command = "jsonnet ${f.path} ${JsonnetUtils.createExtCodeFileArg(connectorPath, extCodeFile)} " +
                "${JsonnetUtils.createTlaArgs(tlaStringArgs)}"
        print(command)

        def proc = command.execute()
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
