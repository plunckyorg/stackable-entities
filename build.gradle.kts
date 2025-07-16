import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
}

group = "com.pluncky"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")

    compileOnly(fileTree("D:\\Minecraft\\Local Minecraft Server\\1.21.7\\plugins") { include("bukkit-utils.jar") })
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    archiveFileName.set("${project.name}.jar")

    destinationDirectory.set(file("D:\\Minecraft\\Local Minecraft Server\\1.21.7\\plugins"))
}

paper {
    name = project.name
    prefix = "Stackable Entities"
    apiVersion = "1.21.7"
    version = "${project.version}"
    main = "com.pluncky.stackableentities.StackableEntitiesPlugin"
    description = "Stacks living entities and item drops."
    author = "davipccunha"

    serverDependencies {
        register("bukkit-utils") {
            load = PaperPluginDescription.RelativeLoadOrder.AFTER
            required = true
        }
    }
}