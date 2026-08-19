rootProject.name = "conditions"

include("api", "gson", "paper")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            name = "databagLocal"
            url = uri(rootDir.resolve("../databag/build/maven-repo"))
        }
        maven {
            name = "databagGitHub"
            url = uri("https://maven.pkg.github.com/mintychochip/databag")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
