// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20" apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.24" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
}
buildscript {
    extra.apply {
        set("room_version", "2.6.0")
    }
}