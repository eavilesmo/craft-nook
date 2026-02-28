// Root build.gradle.kts for Craft Nook
plugins {
    // Plugins applied to all subprojects
    id("com.android.application") version "8.3.2" apply false
    kotlin("android") version "1.9.22" apply false
    kotlin("kapt") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
