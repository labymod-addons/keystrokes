plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
}

val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

group = "net.labymod.addons"
version = providers.environmentVariable("VERSION").getOrElse("0.0.1")


labyMod {
    defaultPackageName = "net.labymod.addons.keystrokes" //change this to your main package name (used by all modules)

    minecraft {
        registerVersion(versions.toTypedArray()) {

        }
    }

    addonInfo {
        namespace = "keystrokes"
        displayName = "Keystrokes"
        author = "LabyMedia GmbH"
        description = "Example Description"
        minecraftVersion = "*"
        version = rootProject.version.toString()
    }
}

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")

    repositories {
        maven("https://libraries.minecraft.net/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}