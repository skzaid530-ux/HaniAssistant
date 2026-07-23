pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Picovoice (Porcupine) repository
        maven { url = uri("https://repo.picovoice.com/") }
    }
}
rootProject.name = "HaniAssistant"
include(":app")
