dependencies {
    api(project(":api"))
    compileOnly(libs.paper.api)
    compileOnly(libs.jetbrains.annotations)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
