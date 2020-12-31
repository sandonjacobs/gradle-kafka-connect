package com.github.sandonjacobs.gradle

import com.github.sandonjacobs.gradle.tasks.ListConnectorsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class ConnectorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // apply Gradle built-in plugins
        project.apply plugin: 'base'
        project.apply plugin: 'com.redpillanalytics.gradle-properties'

        // apply the Gradle plugin extension and the context container
        applyExtension(project)

        project.afterEvaluate {

            // show ALL connectors
            project.task('listConnectors', type: ListConnectorsTask) {}
        }
    }

    /**
     * Apply the Gradle Plugin extension.
     */
    void applyExtension(Project project) {
        project.configure(project) {
            extensions.create('kafkaConnect', ConnectorExtension)
        }

    }

}
