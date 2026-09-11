tasks.register("clean") {
    dependsOn(":app:clean")
}

tasks.register("assembleDebug") {
    dependsOn(":app:assembleDebug")
}

tasks.register("assembleRelease") {
    dependsOn(":app:assembleRelease")
}

tasks.register("assemble") {
    dependsOn(":app:assemble")
}

tasks.register("bundleRelease") {
    dependsOn(":app:bundleRelease")
}

tasks.register("bundleDebug") {
    dependsOn(":app:bundleDebug")
}

tasks.register("bundle") {
    dependsOn(":app:bundle")
}

tasks.register("check") {
    dependsOn(":app:check")
}

tasks.register("test") {
    dependsOn(":app:test")
}

tasks.register("lint") {
    dependsOn(":app:lint")
}

tasks.register("build") {
    dependsOn(":app:build")
}
