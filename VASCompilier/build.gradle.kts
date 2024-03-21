plugins {
    id("java")
    antlr
}

group = "com.vas"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    antlr("org.antlr:antlr4:4.13.1")
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