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
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.0.0")
    implementation("org.apache.poi:poi-ooxml:4.1.2")
    testImplementation("junit:junit:4.13.2")

    /*compile "org.apache.logging.log4j:log4j-api-kotlin:1.0.0"
    compile "org.apache.logging.log4j:log4j-api:2.11.1"
    compile "org.apache.logging.log4j:log4j-core:2.11.1"*/
}
