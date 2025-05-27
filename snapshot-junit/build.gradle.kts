plugins {
    java
    alias(libs.plugins.internalConvention)
    alias(libs.plugins.testKonvence)
}

dependencies {
    compileOnly(projects.constants)
    compileOnly(projects.shared)
    testCompileOnly(projects.constants)
    testImplementation(projects.shared)
    implementation(libs.junit.jupiter.api)
}
