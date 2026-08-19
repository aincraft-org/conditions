import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository

val releaseVersionPattern =
    Regex("""\d{2}\.([1-9]|1[0-2])\.([1-9]|[12]\d|3[01])\.[1-9]\d*""")

// CalVer YY.M.D.REVISION. Local builds default to SNAPSHOT; GitHub Packages
// and tagged releases require -PreleaseVersion= (CI uses UTC date + run number).
val requestedReleaseVersion = providers.gradleProperty("releaseVersion")
    .orNull
    ?.takeIf { it.isNotBlank() }
val conditionsVersion = requestedReleaseVersion ?: "0.0.0-SNAPSHOT"

gradle.taskGraph.whenReady {
    val githubPackagesPublicationRequested = allTasks.any { task ->
        task.name.contains("GitHubPackages")
    }
    val validReleaseVersion =
        requestedReleaseVersion?.matches(releaseVersionPattern) == true
    if (githubPackagesPublicationRequested && !validReleaseVersion) {
        throw GradleException(
            "GitHub Packages publication requires -PreleaseVersion=YY.M.D.REVISION "
                + "(for example, -PreleaseVersion=26.8.19.1).",
        )
    }
}

allprojects {
    group = "dev.conditions"
    version = conditionsVersion
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    val javaVersion = if (name == "paper") 25 else 21
    configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
        withSourcesJar()
        withJavadocJar()
    }
    tasks.withType<JavaCompile>().configureEach {
        options.release.set(javaVersion)
    }
    tasks.withType<Javadoc>().configureEach {
        isFailOnError = false
    }
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifactId = project.name
                pom {
                    name.set("conditions ${project.name}")
                    description.set(
                        when (project.name) {
                            "api" -> "Paper-free player condition graph for Minecraft-shaped predicates."
                            "gson" -> "Vanilla loot-condition JSON reader/writer for dev.conditions."
                            "paper" -> "Paper adapter that builds ConditionContext from a live player."
                            else -> "dev.conditions library."
                        },
                    )
                    url.set("https://github.com/aincraft-org/conditions")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("aincraft")
                            name.set("Aincraft")
                            url.set("https://github.com/aincraft-org")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/aincraft-org/conditions.git")
                        developerConnection.set("scm:git:ssh://git@github.com/aincraft-org/conditions.git")
                        url.set("https://github.com/aincraft-org/conditions")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "localBuildRepo"
                url = rootProject.layout.buildDirectory.dir("maven-repo").get().asFile.toURI()
            }
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/aincraft-org/conditions")
                credentials {
                    username = System.getenv("GITHUB_ACTOR") ?: ""
                    password = System.getenv("GITHUB_TOKEN") ?: ""
                }
            }
        }
    }

    tasks.withType<PublishToMavenRepository>().configureEach {
        if (name.contains("GitHubPackages")) {
            doFirst {
                val version = requestedReleaseVersion
                require(version != null && version.matches(releaseVersionPattern)) {
                    "Repository publication '$name' requires -PreleaseVersion=YY.M.D.REVISION"
                }
            }
        }
    }
}
