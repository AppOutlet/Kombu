import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.hotReload)
}

dependencies {
    implementation(project(":kombu"))
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "dev.appoutlet.kombu.Kombu"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.appoutlet.kombu"
            packageVersion = "1.0.0"
        }
    }
}
