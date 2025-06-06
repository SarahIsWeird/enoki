plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
    `maven-publish`
}

version = project.extra["modVersion"] as String
group = project.extra["mavenGroup"] as String

base {
    val archivesBaseName: String by project
    archivesName = archivesBaseName
}

sourceSets {
    create("testmod") {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }
}

loom {
    runs {
        create("testmodClient") {
            client()
            ideConfigGenerated(project.rootProject == project)
            name = "Testmod Client"
            source(sourceSets["testmod"])
        }

        create("testmodServer") {
            server()
            ideConfigGenerated(project.rootProject == project)
            name = "Testmod Server"
            source(sourceSets["testmod"])
        }
    }
}

repositories {
    maven("https://maven.terraformersmc.com/") {
        name = "TerraformersMC"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.extra["minecraftVersion"]}")
    mappings("net.fabricmc:yarn:${project.extra["yarnMappings"]}:v2")

    modImplementation("net.fabricmc:fabric-loader:${project.extra["fabricLoaderVersion"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.extra["fabricApiVersion"]}")

    val commonmarkVersion: String by project
    include(api("org.commonmark:commonmark:$commonmarkVersion")) {}
    include(api("org.commonmark:commonmark-ext-gfm-strikethrough:$commonmarkVersion")) {}

    // Dev mods
    val modMenuVersion: String by project
    modLocalRuntime("com.terraformersmc:modmenu:$modMenuVersion")

    // This is cursed.
    "testmodImplementation"(sourceSets["main"].output)
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to inputs.properties["version"])
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.base.archivesName.get()
            from(components["java"])
        }
    }

    repositories {
    }
}
