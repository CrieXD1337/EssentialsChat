plugins {
    id("java")
    `maven-publish`
}

group = "me.criex"
version = "SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.opencollab.dev/maven-snapshots")
        name = "opencollab-repo-snapshot"
    }
    maven {
        url = uri("https://repo.luckperms.net/")
        name = "luckperms-repo"
    }
}

dependencies {
    compileOnly("cn.nukkit:nukkit:1.0-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    implementation(files("libs/Multipass-1.1.6.jar"))
}

tasks.withType<ProcessResources> {
    filesMatching("plugin.yml") {
        expand(project.properties)
    }
}