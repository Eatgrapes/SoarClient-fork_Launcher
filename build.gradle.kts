plugins {
    kotlin("jvm") version "2.1.21"
    id("org.jetbrains.compose") version "1.6.11"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.eatgrapes.soarlauncher"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("org.apache.commons:commons-compress:1.26.0")
    implementation("net.coobird:thumbnailator:0.4.17")

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // webp support
    implementation("com.twelvemonkeys.imageio:imageio-webp:3.10.1")
    implementation("com.twelvemonkeys.imageio:imageio-core:3.10.1")
    implementation("com.github.oshi:oshi-core:6.4.11")

    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.21")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.1.21")
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "dev.eatgrapes.soarlauncher.MainKt"
        
        jvmArgs += listOf(
            "-Xmx1g",
            "-Xms256m",
            "-XX:+UseG1GC",
            "-XX:+UseStringDeduplication",
            "-XX:+OptimizeStringConcat",
            "-XX:+UseCompressedOops",
            "-XX:MaxGCPauseMillis=100",
            "-Dskiko.render.api=OPENGL",
            "-Dskiko.fallback.render.api=SOFTWARE",
            "--add-exports=java.base/sun.security.action=ALL-UNNAMED",
            "--add-exports=java.management/sun.management=ALL-UNNAMED",
            "--add-exports=jdk.management/com.sun.management.internal=ALL-UNNAMED",
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED"
        )

        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)

            windows {
                packageName = "Soar-Fork Client Launcher"
                packageVersion = "1.0.0"
                menuGroup = "SoarClient-Fork Team"
                upgradeUuid = "9178b83d-8de5-458c-8395-febfc72c0121"
                shortcut = true
                menu = true
                iconFile.set(project.file("src/main/resources/soar/logo.ico"))
            }
        }
    }
}

tasks.shadowJar {
    archiveBaseName.set("SoarClient-fork_Launcher")
    archiveClassifier.set("all")
    archiveVersion.set("1.0-SNAPSHOT")
    manifest {
        attributes["Main-Class"] = "dev.eatgrapes.soarlauncher.MainKt"
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.eatgrapes.soarlauncher.MainKt"
    }
}

kotlin {
    jvmToolchain(21)
}
