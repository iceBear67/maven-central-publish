package net.mamoe.him188.maven.central.publish.gradle.preview

import net.mamoe.him188.maven.central.publish.gradle.MavenCentralPublishPlugin
import net.mamoe.him188.maven.central.publish.gradle.tasks.PreviewPublication
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

class JvmPreviewPublicationTest : AbstractPreviewPublicationTest() {
    @TestFactory
    fun `test Kotlin JVM`() = createTestsForKotlinVersions {
        val group = "group-id"
        val name = "project-name"
        val version = "1.0.0"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("${MavenCentralPublishPlugin.PLUGIN_ID}")
                kotlin("jvm") version "$publisherVersion"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                workingDir = File("${publisherDir.absolutePath.replace("\\", "/")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
        """.trimIndent()
        )

        assertGradleTaskSuccess(publisherDir, PreviewPublication.TASK_NAME) {
            val message = output.substringAfter("Publication Preview").substringBefore("Publication Preview End").trim()

            assertEquals(
                """
                Root module:
                  GroupId: group-id
                  ArtifactId: project-name
                  Version: 1.0.0

                Your project targets JVM platform only.
                Gradle users can add dependency by `implementation("group-id:project-name:1.0.0")`repository declared.
                Maven users can add dependency as follows:
                <dependency>
                    <groupId>group-id</groupId>
                    <artifactId>project-name</artifactId>
                    <version>1.0.0</version>
                </dependency>
            """.trimIndent(),
                message
            )
        }
    }

    @TestFactory
    fun `test Kotlin JVM with custom coordinates`() = createTestsForKotlinVersions {
        val group = "group-id"
        val name = "project-name"
        val version = "1.0.0"

        publisherDir.resolve("settings.gradle").writeText("""rootProject.name = "$name"""")
        publisherDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("${MavenCentralPublishPlugin.PLUGIN_ID}")
                kotlin("jvm") version "$publisherVersion"
            }
            repositories { mavenCentral() }
            description = "Test project desc."
            group = "$group"
            version = "$version"
            mavenCentralPublish {
                groupId = "custom-group-id"
                artifactId = "custom-artifact-id"
                version = "9.9.9"
                workingDir = File("${publisherDir.absolutePath.replace("\\", "/")}")
                singleDevGithubProject("Him188", "yamlkt")
                licenseFromGitHubProject("Apache-2.0", "master")
            }
        """.trimIndent()
        )

        assertGradleTaskSuccess(publisherDir, PreviewPublication.TASK_NAME) {
            val message = output.substringAfter("Publication Preview").substringBefore("Publication Preview End").trim()

            assertEquals(
                """
                Root module:
                  GroupId: custom-group-id
                  ArtifactId: custom-artifact-id
                  Version: 9.9.9

                Your project targets JVM platform only.
                Gradle users can add dependency by `implementation("custom-group-id:custom-artifact-id:9.9.9")`repository declared.
                Maven users can add dependency as follows:
                <dependency>
                    <groupId>custom-group-id</groupId>
                    <artifactId>custom-artifact-id</artifactId>
                    <version>9.9.9</version>
                </dependency>
            """.trimIndent(),
                message
            )
        }
    }
}