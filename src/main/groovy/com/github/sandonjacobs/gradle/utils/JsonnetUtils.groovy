package com.github.sandonjacobs.gradle.utils

import groovy.util.logging.Slf4j

@Slf4j
class JsonnetUtils {

    static def createTlaArgs(rawArgs) {
        def prefix = "--tla-str "
        if (rawArgs != null && rawArgs.length() > 0) {
            def args = rawArgs.tokenize('|')

            def result = prefix + args.inject { acc, val -> "$acc --tla-str $val" }
            log.debug("**** POST INJECT *******" + result)
            return result
        }
        else return ""
    }
}
