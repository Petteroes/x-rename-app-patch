group = "app.xrename"

patches {
    about {
        name = "X Rename App Patches"
        description = "Custom Morphe patch for renaming cloned X/Twitter apps."
        source = "https://github.com/Petteroes/x-rename-app-patch"
        author = "Petteroes"
        contact = "https://github.com/Petteroes"
        website = "https://github.com/Petteroes/x-rename-app-patch"
        license = "GPLv3"
    }
}

val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"
        dependsOn(build)
        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    publish {
        dependsOn("generatePatchesList")
    }
}
