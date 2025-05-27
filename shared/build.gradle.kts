plugins {
    java
    alias(libs.plugins.internalConvention)
    alias(libs.plugins.testKonvence)
}

dependencies {
    compileOnly(projects.constants)
}
