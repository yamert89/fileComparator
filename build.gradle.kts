plugins {
    kotlin("jvm") version "1.4.20"
}

group = "roslesinforg.porokhin"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.apache.logging.log4j:log4j-api:2.14.0")
    implementation("org.apache.logging.log4j:log4j-core:2.14.0")
    testImplementation("junit:junit:4.13.2")
}
