plugins {
    kotlin("jvm") version "2.1.21"
    id("org.jetbrains.compose") version "1.6.11"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
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
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    

    
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "dev.eatgrapes.soarlauncher.MainKt"
        
        jvmArgs += listOf(
            "-Xmx2g",
            "-Xms512m",
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

kotlin {
    jvmToolchain(21)
}