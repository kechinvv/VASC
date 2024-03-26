plugins {
    java
    antlr
    kotlin("jvm") version "1.9.23"
}

group = "com.vasc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    generateGrammarSource {
        val packageName = "${project.group}.antlr"
        outputDirectory = File(outputDirectory, packageName.replace('.', '/'))
        arguments = arguments + listOf("-visitor", "-package", packageName)
    }

    compileJava {
        dependsOn("generateGrammarSource")
    }

    compileTestJava {
        dependsOn("generateTestGrammarSource")
    }
}
