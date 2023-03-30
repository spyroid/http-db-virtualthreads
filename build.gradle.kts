plugins {
    id("java")
//    kotlin("jvm") version "1.8.20-RC"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.graalvm.buildtools.native") version "0.9.20"
}

group = "org.example"
version = "1.0"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.5.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

tasks {
    withType<Test>().all {
        allJvmArgs = listOf("--enable-preview")
        testLogging.showStandardStreams = true
        testLogging.showExceptions = true
        useJUnitPlatform {
        }
    }

    withType<JavaExec>().all {
        allJvmArgs = listOf("--enable-preview")
    }

    withType(JavaCompile::class.java).all {
        options.compilerArgs.addAll(listOf("--enable-preview", "-Xlint:preview"))
    }

    jar {
        manifest {
            attributes("Main-Class" to "org.example.Main")
        }
    }
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("shadow")
        mergeServiceFiles()
//        minimize()
        dependencies {
            exclude(dependency("org.jetbrains.kotlin:.*"))
            exclude(dependency("org.jetbrains:.*"))
        }
        manifest {
            attributes(mapOf("Main-Class" to "org.example.Main"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}

graalvmNative {
//    useFatJar.set(false) // required for older GraalVM releases
    binaries {
        named("main") {
            useFatJar.set(true)
            mainClass.set("org.example.Main")
            buildArgs.add("--enable-preview")
//            buildArgs.add("--initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils")
//            buildArgs.add("--initialize-at-run-time=io.netty.channel.DefaultFileRegion")
//            buildArgs.add("--initialize-at-run-time=io.netty.util.AbstractReferenceCounted")
        }
    }
}

tasks.register<JavaExec>("runOn19") {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(19))
    })

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.example.Main")
}

