package com.github.sandonjacobs.gradle

import groovy.util.logging.Slf4j

@Slf4j
class ConnectorExtension {

    /**
     * The group name to use for all tasks. Default: 'kafkaConnect'.
     */
    String taskGroup = 'kafkaConnect'

    /**
     * RESTful endpoint for the Connect Server. Default: 'http://localhost:8083'.
     */
    String connectEndpoint = 'http://localhost:8083'

    /**
     * Base source directory for the Confluent plugin. Default: 'src/main'.
     */
    String sourceBase = 'src/main'

    /**
     * Name of the connectors source directory that resides in the {@link #sourceBase} directory. Default: 'connectors'.
     */
    String connectorSourceName = 'connectors'

    /**
     * Full path of the connector config source directory. When set, this overrides the values of {@link #sourceBase} and {@link #connectorSourceName}. Default: null.
     */
    String connectorSourcePath

    /**
     * Provides the path for Pipeline source files.
     *
     * @return The full path of the Pipeline source files. Uses {@link #connectorSourcePath} first if it exists, and if it doesn't (the default), then it uses {@link #sourceBase} and {@link #connectorSourceName}.
     */
    String getConnectorsPath() {

        return (connectorSourcePath ?: "${sourceBase}/${connectorSourceName}")
    }
}
