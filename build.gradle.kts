plugins {
    id("java")
//    kotlin("jvm") version "1.8.20-RC"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.graalvm.buildtools.native") version "0.11.0"
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
    implementation("io.avaje:avaje-jsonb:3.6")
    annotationProcessor("io.avaje:avaje-jsonb-generator:3.6")
}


tasks {
    jar {
        manifest {
            attributes("Main-Class" to "org.example.Main")
        }
    }
    shadowJar {
        archiveBaseName.set("shadow")
        isZip64 = true
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
