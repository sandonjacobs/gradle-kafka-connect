package com.github.sandonjacobs.gradle.utils

class JsonnetUtilsTest extends GroovyTestCase {
    void testAddTlaArgs() {
        def args = "username=foo|password=foo|passwordToken=foo|consumerKey=foo|consumerSecret=foo"
        def result = JsonnetUtils.createTlaArgs(args)
        assertEquals("--tla-str username=foo --tla-str password=foo --tla-str passwordToken=foo --tla-str consumerKey=foo --tla-str consumerSecret=foo", result)
    }
}
