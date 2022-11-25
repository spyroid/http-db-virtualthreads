plugins {
    id("java")
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.graalvm.buildtools.native") version "0.9.18"
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
    implementation("org.postgresql:postgresql:42.5.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
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
