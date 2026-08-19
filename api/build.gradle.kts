dependencies {
    api(libs.adventure.api)
    api(libs.jetbrains.annotations)
    api(libs.kryo)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
