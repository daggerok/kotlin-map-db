pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
  val kotlinVersion: String by extra
  val shadowGradlePluginVersion: String by extra
  val versionsGradlePluginVersion: String by extra
  plugins {
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("com.github.ben-manes.versions") version versionsGradlePluginVersion
    id("com.github.johnrengelman.shadow") version shadowGradlePluginVersion
  }
}

val name: String by extra
rootProject.name = name
