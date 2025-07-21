import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    java
    application
    id("io.freefair.lombok") version "8.13.1"
    id("com.gradleup.shadow") version "9.0.0-rc1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.3.2") {
        exclude(module = "opus-java") // audio library
    }
    implementation("com.zaxxer:HikariCP:6.3.0")
    runtimeOnly("org.xerial:sqlite-jdbc:3.49.1.0")

    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3")
}

application {
    mainClass = "me.thosea.autopoller.main.Bootstrap"
}

tasks.processResources {
    val properties = mapOf("version" to version)
    inputs.properties(properties)
    filesMatching("autopoller_version.txt") {
        expand(properties)
    }

    from("LICENSE")
    from("ThirdPartyLicenses")
    from("NOTICE")
}

java.toolchain {
    languageVersion = JavaLanguageVersion.of(22)
}

tasks.jar {
    archiveAppendix = "no-deps"
}

tasks.shadowJar {
    minimize {
        exclude(dependency("org.apache.logging.log4j:.*:.*"))
        exclude(dependency("org.xerial:sqlite-jdbc:.*"))
    }
    transform(Log4j2PluginsCacheFileTransformer())
    mergeServiceFiles()

    archiveClassifier = ""

    val jar = tasks.jar.map { archiveFile.get().asFile }
    doLast { jar.get().delete() }
}

tasks.distZip { enabled = false }
tasks.distTar { enabled = false }
tasks.shadowDistTar { enabled = false }
tasks.shadowDistZip { enabled = false }