import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization") version "2.1.10"
}

group = "me.knivram"
val appVersion = System.getenv("GITHUB_EVENT_INPUTS_VERSION") ?: "1.0.0"
version = appVersion

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.windows_x64)
    implementation(compose.desktop.linux_arm64)
    implementation(compose.desktop.linux_x64)
    implementation(compose.desktop.macos_arm64)
    implementation(compose.desktop.macos_x64)

    val voyagerVersion = "1.1.0-beta02"
    implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")

    val exposed_version = "0.60.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.h2database:h2:2.2.224")

    implementation("org.jetbrains.skiko:skiko-awt-runtime-windows-arm64:0.9.2")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.9.2")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-linux-arm64:0.9.2")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.9.2")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-macos-arm64:0.9.2")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-macos-x64:0.9.2")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            // WARN: when changing the package name, you need to update file paths in windows-release.yml
            packageName = "AthliTrack"
            packageVersion = appVersion

            // Add modules required for JDBC
            modules("java.sql", "java.naming")
        }
    }
}

//tasks.withType<Jar> {
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    manifest {
//        attributes["Main-Class"] = "MainKt"
//    }
//
//    val desktopMainSourceSet = kotlin.sourceSets["main"]
//
//    from(desktopMainSourceSet.resources.srcDirs)
//
//    from({
//        configurations["runtimeClasspath"].filter { it.exists() }.map { zipTree(it) }
//    })
//}
