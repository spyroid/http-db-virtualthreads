plugins {
    id("java")
//    kotlin("jvm") version "1.8.20-RC"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.graalvm.buildtools.native") version "0.10.6"
    id("com.github.ben-manes.versions") version "0.52.0"
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
//    implementation("org.postgresql:postgresql:42.6.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
}


tasks {
    jar {
        manifest {
            attributes("Main-Class" to "org.example.Main")
        }
    }
    shadowJar {
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
//            buildArgs.add("--enable-preview")
//            buildArgs.add("--initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils")
//            buildArgs.add("--initialize-at-run-time=io.netty.channel.DefaultFileRegion")
//            buildArgs.add("--initialize-at-run-time=io.netty.util.AbstractReferenceCounted")
        }
    }
}
