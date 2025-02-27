import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java-gradle-plugin")
    groovy
    `maven-publish`
    id("com.gradle.plugin-publish")
    id("com.github.johnrengelman.shadow")
}

sourceSets.main {
    java {
        srcDir(project(":maven-central-publish-protocol").projectDir.resolve("src/commonMain/kotlin"))
        // so that no need to publish :maven-central-publish-protocol to maven central.
    }
}

kotlin {
    sourceSets.all {
        languageSettings.progressiveMode = true
        languageSettings.optIn("kotlin.OptIn")
    }
}

/**
 * Because we use [compileOnly] for `kotlin-gradle-plugin`, it would be missing
 * in `plugin-under-test-metadata.properties`. Here we inject the jar into TestKit plugin
 * classpath via [PluginUnderTestMetadata] to avoid to avoid [NoClassDefFoundError].
 */
val kotlinVersionForIntegrationTest: Configuration by configurations.creating

dependencies {
    compileOnly(kotlin("gradle-plugin"))

    compileOnly(gradleApi())
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${rootProject.extra.get("serialization")}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${rootProject.extra.get("serialization")}")
    implementation("io.github.karlatemp:PublicationSign:1.3.12")

    testImplementation(gradleApi())
    testImplementation(localGroovy())
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:${rootProject.extra.get("junit")}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${rootProject.extra.get("junit")}")
    testImplementation(gradleTestKit())

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${rootProject.extra.get("junit")}")

    // Note: this version should be same as `KotlinTransitiveDependenciesIntegrationTest`
    kotlinVersionForIntegrationTest("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
}

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
    pluginClasspath.from(kotlinVersionForIntegrationTest)
}

tasks.withType(Test::class) {
    useJUnitPlatform()
}

tasks.getByName("shadowJar", ShadowJar::class) {
    archiveClassifier.set("")
}

tasks.getByName("publishPlugins").dependsOn("shadowJar")

pluginBundle {
    website = "https://github.com/Him188/maven-central-publish"
    vcsUrl = "https://github.com/Him188/maven-central-publish"
    tags = listOf("maven", "publishing", "tools")
}

gradlePlugin {
    testSourceSets(sourceSets.test.get())
    plugins {
        create("MavenCentralPublish") {
            id = "me.him188.maven-central-publish"
            displayName = "Maven Central Publish"
            description = project.description
            implementationClass = "me.him188.maven.central.publish.gradle.MavenCentralPublishPlugin"
        }
    }
}

kotlin.target.compilations.all {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        jvmTarget = "1.8"
    }
}