plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.0"
}

group = "de.idiotischer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "craftsblockReleases"
        url = uri("https://repo.craftsblock.de/experimental")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(platform("de.craftsblock.craftscore:bom:3.8.13-pre8"))
    implementation("de.craftsblock.craftscore:event")
    implementation("com.google.code.gson:gson:2.13.2")
}

tasks.shadowJar {
    // Only embed shared classes
    configurations = listOf(project.configurations.getByName("runtimeClasspath"))
    dependencies {
        include(project(":shared"))
    }
    archiveClassifier.set("")
    archiveBaseName.set("BOB-server")
}

tasks.jar {
    enabled = false
}
