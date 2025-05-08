// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version libs.versions.androidApplication.get() apply false
    id("org.jetbrains.kotlin.android") version libs.versions.kotlinAndroid.get() apply false
    alias(libs.plugins.kotlin.compose) apply false // Ak používaš Compose
    id("com.android.library") version libs.versions.androidLibrary.get() apply false // Ak máš knižničný modul
}
// build.gradle (Project: YourProjectName)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:...") // Skontrolujte vašu verziu
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:...") // Skontrolujte vašu verziu
    }
}

allprojects {
    repositories {
        // Tu by nemali byť žiadne riadky s google() alebo mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}