plugins {
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.internalConvention)
}

gradlePlugin {
    website = "https://github.com/varlanv/java-test-konvence"
    vcsUrl = "https://github.com/varlanv/java-test-konvence"
    plugins {
        create("testKonvenceGradlePlugin") {
            id = "com.varlanv.test-konvence"
            implementationClass = "com.varlanv.testkonvence.gradle.plugin.TestKonvencePlugin"
            displayName = "Test Konvence Plugin"
            description = "Plugin that provides a way to automatically change and enforce test naming"
            tags = listOf("test", "convention", "junit", "naming", "quality")

        }
    }
}

dependencies {
    compileOnly(projects.shared)
    compileOnly(projects.constants)
    testCompileOnly(projects.constants)
}

tasks.named<Jar>("jar") {
    dependsOn(":shared:jar")
    val rootDirPath = project.rootDir.toPath()
    from(
        rootDirPath
            .resolve("shared")
            .resolve("build")
            .resolve("classes")
            .resolve("java")
            .resolve("main")
    )
}