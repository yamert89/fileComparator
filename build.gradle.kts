plugins {
    kotlin("jvm") version "1.4.20"
}

group = "roslesingorg.porokhin"
version = "0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("junit:junit:4.13.2")
}
