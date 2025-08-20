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

    // Twelvemonkeys 图像库以支持 WebP 格式
    implementation("com.twelvemonkeys.imageio:imageio-webp:3.10.1")
    implementation("com.twelvemonkeys.imageio:imageio-core:3.10.1")
    
    
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
            "-Dskiko.fallback.render.api=SOFTWARE"
        )
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