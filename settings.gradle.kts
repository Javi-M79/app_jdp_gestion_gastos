pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT) //
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "app_jdp_gestion_gastos"
include(":app")