plugins {
    id("java")
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.graalvm.buildtools.native") version "0.9.18"
//    id("io.freefair.lombok") version "6.6-rc1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {

//    implementation("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")
    implementation("org.postgresql:postgresql:42.5.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")

//    compileOnly("org.projectlombok:lombok:1.18.24")
//    annotationProcessor("org.projectlombok:lombok")

//    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
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
