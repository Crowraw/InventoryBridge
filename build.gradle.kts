import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
}

group = "de.crowraw"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("http://erethon.de/repo")
    maven("https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client")
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly("de.erethon.dungeonsxl:dungeonsxl-api:0.18-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:1.17.1-R0.1-SNAPSHOT")
    implementation("de.crowraw.lib:CrowLIB:1.0"){
        exclude("org.spigotmc")
    }
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.4")
    testImplementation(kotlin("test-junit"))
}




tasks.test {
    useJUnit()
}
tasks.withType<Jar> {
    // Otherwise you'll get a "No main manifest attribute" error
    manifest {
        attributes["Main-Class"] = "com.example.MainKt"
    }

    // To add all of the dependencies
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "15"
}