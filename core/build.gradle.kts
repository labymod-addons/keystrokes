import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

dependencies {
    labyProcessor()
    api(project(":api"))

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}

tasks.test {
    useJUnitPlatform()
}

// The addon annotation processor only applies to the addon sources
tasks.compileTestJava {
    options.compilerArgs.add("-proc:none")
}