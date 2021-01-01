import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//val kotlinVersion = "1.4.0"
val kotlinVersion = "1.4.21"
//val serializationVersion = "1.0.0-RC"
val serializationVersion = "1.0.1"
//val ktorVersion = "1.4.0"
val ktorVersion = "1.4.3"

val muirwikComponentVersion = "0.6.3"
//val kotlinJsVersion = "pre.129-kotlin-$kotlinVersion"
val kotlinJsVersion = "pre.133-kotlin-$kotlinVersion"


plugins {
//    kotlin("multiplatform") version "1.4.0"
    kotlin("multiplatform") version "1.4.20"
    application //to run JVM part
//    kotlin("plugin.serialization") version "1.4.0"
    kotlin("plugin.serialization") version "1.4.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/kotlin-js-wrappers/") // react, styled, ...
}

kotlin {
    jvm {
        withJava()
    }
    js(IR) {
        browser {
            binaries.executable()
        }

        useCommonJs()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("com.benasher44:uuid:0.2.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-serialization:$ktorVersion")
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:1.2.3")
                implementation("io.ktor:ktor-websockets:$ktorVersion")
//                implementation("org.litote.kmongo:kmongo:4.1.1")
//                implementation("org.litote.kmongo:kmongo-core:4.1.1")
                implementation("org.litote.kmongo:kmongo-coroutine-serialization:4.1.1")
                implementation("com.benasher44:uuid:0.2.3")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion") //include http&websockets

                //ktor client js json
                implementation("io.ktor:ktor-client-json-js:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-js:$ktorVersion")

//                implementation("org.jetbrains:kotlin-react:16.13.1-$kotlinJsVersion")
//                implementation("org.jetbrains:kotlin-react-dom:16.13.1-$kotlinJsVersion")
                implementation("org.jetbrains:kotlin-react:17.0.0-$kotlinJsVersion")
                implementation("org.jetbrains:kotlin-react-dom:17.0.0-$kotlinJsVersion")
                implementation("org.jetbrains:kotlin-react-router-dom:5.2.0-$kotlinJsVersion")
                implementation(npm("react", "17.0.0"))
                implementation(npm("react-dom", "17.0.0"))

                implementation("org.jetbrains:kotlin-redux:4.0.5-$kotlinJsVersion")

//                implementation("org.jetbrains:kotlin-styled:1.0.0-pre.110-kotlin-1.4.0")
//                implementation("org.jetbrains:kotlin-css-js:1.0.0-pre.110-kotlin-1.4.0")
                implementation("org.jetbrains:kotlin-styled:5.2.0-$kotlinJsVersion")
                implementation("org.jetbrains:kotlin-css-js:1.0.0-$kotlinJsVersion")
                implementation("com.ccfraser.muirwik:muirwik-components:$muirwikComponentVersion")
                implementation(npm("styled-components", "5.2.1"))
                implementation(npm("inline-style-prefixer", "6.0.0"))
                implementation(npm("@material-ui/core", "^4.9.14"))
                implementation(npm("react-hot-loader", "^4.12.20"))
            }
        }
    }
}

application {
    mainClassName = "ServerKt"
    applicationDefaultJvmArgs = listOf(
//        "--add-opens",
//        "java.base/jdk.internal.misc=ALL-UNNAMED",
//        "-Dio.netty.tryReflectionSetAccessible=true",
        "-Dio.netty.noUnsafe=true")
}

// include JS artifacts in any JAR we generate
tasks.getByName<Jar>("jvmJar") {
    val taskName = if (project.hasProperty("isProduction")) {
        "jsBrowserProductionWebpack"
    } else {
        "jsBrowserDevelopmentWebpack"
    }
    val webpackTask = tasks.getByName<KotlinWebpack>(taskName)
    dependsOn(webpackTask) // make sure JS gets compiled first
    from(File(webpackTask.destinationDirectory, webpackTask.outputFileName)) // bring output file along into the JAR
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

distributions {
    main {
        contents {
            from("$buildDir/libs") {
                rename("${rootProject.name}-jvm", rootProject.name)
                into("lib")
            }
        }
    }
}

// Alias "installDist" as "stage" (for cloud providers)
tasks.create("stage") {
    dependsOn(tasks.getByName("installDist"))
}

tasks.getByName<JavaExec>("run") {
    this.jvmArgs = listOf(
        "--add-opens",
        "java.base/jdk.internal.misc=ALL-UNNAMED",
        "-Dio.netty.tryReflectionSetAccessible=true"
//        "-Dio.netty.noUnsafe=true"
    )
    classpath(tasks.getByName<Jar>("jvmJar")) // so that the JS artifacts generated by `jvmJar` can be found and served
}

//val compileKotlin: KotlinCompile by tasks
//compileKotlin.kotlinOptions {
//    languageVersion = "1.4"
//}

//tasks.withType(KotlinCompile::class.java).all {
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
//}
