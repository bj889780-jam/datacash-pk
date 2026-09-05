tasks.register("assembleDebug") {
    dependsOn(":app:assembleDebug")
}

tasks.register("build") {
    dependsOn(":app:assembleDebug")
}
