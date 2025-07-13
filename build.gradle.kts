plugins {
    java
    application
    alias(libs.plugins.shadow)
    alias(libs.plugins.lombok)
}

repositories {
    mavenLocal()
    maven("https://maven.glass-launcher.net/releases")
    maven("https://maven.glass-launcher.net/snapshots")
}

dependencies {
    implementation(libs.bundles.implementation)
}

group = "net.glass-launcher"
version = "0.5.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

application {
    mainClass = "net.glasslauncher.legacy.Main"
}
