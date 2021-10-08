import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.5.31"
val serializationVersion = "1.2.1"
val ktorVersion = "1.6.4"

val muirwikComponentVersion = "0.6.3"
val kotlinJsVersion = "pre.254-kotlin-$kotlinVersion"


plugins {
    kotlin("multiplatform") version "1.5.31"
    application //to run JVM part
    kotlin("plugin.serialization") version "1.5.31"
}

group = "org.f0cus"
version = "1.0.2-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
    }
    js(IR) {
        browser {
        }
        binaries.executable()

        useCommonJs()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

                implementation(kotlin("stdlib-common"))
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
//                compileOnly(kotlin("gradle-plugin", version = kotlinVersion))
//                compileOnly(kotlin("serialization", version = kotlinVersion))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.5.2")

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
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-$kotlinJsVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-$kotlinJsVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:5.2.0-$kotlinJsVersion")
//                implementation(npm("react", "17.0.2"))
//                implementation(npm("react-dom", "17.0.2"))

                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-redux:7.2.4-$kotlinJsVersion")

//                implementation("org.jetbrains:kotlin-styled:1.0.0-pre.110-kotlin-1.4.0")
//                implementation("org.jetbrains:kotlin-css-js:1.0.0-pre.110-kotlin-1.4.0")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.1-$kotlinJsVersion")
//                implementation("org.jetbrains:kotlin-css-js:1.0.0-$kotlinJsVersion")
//                implementation("com.ccfraser.muirwik:muirwik-components:$muirwikComponentVersion")
//                implementation(npm("styled-components", "5.2.1"))
//                implementation(npm("inline-style-prefixer", "6.0.0"))
//                implementation(npm("@material-ui/core", "^4.9.14"))
//                implementation(npm("react-hot-loader", "^4.12.20"))
            }
        }
    }
}

application {
    mainClassName = "ServerKt"
    applicationDefaultJvmArgs = listOf(
        "--add-opens",
        "java.base/jdk.internal.misc=ALL-UNNAMED",
        "-Dio.netty.tryReflectionSetAccessible=true"
    )
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
            jvmTarget = "11"
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
