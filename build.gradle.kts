// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Google services Gradle plugin (đọc google-services.json để cấu hình Firebase)
    id("com.google.gms.google-services") version "4.5.0" apply false
}