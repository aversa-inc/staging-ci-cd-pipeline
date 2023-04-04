import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
}

group = "ca.aversa"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation:2.6.7")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.retry:spring-retry:1.3.3")
    implementation("org.springframework:spring-aspects:5.3.20")
    implementation("com.amazonaws:aws-java-sdk-dynamodb:1.12.201")
    implementation("com.amazonaws:aws-java-sdk-sns:1.12.201")
    implementation("com.amazonaws:aws-java-sdk-secretsmanager:1.12.201")
    implementation("com.amazonaws:aws-java-sdk-ses:1.12.234")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.20")
    implementation("dev.turingcomplete:kotlin-onetimepassword:2.2.0")

    implementation("org.junit.jupiter:junit-jupiter:5.4.2")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.google.code.gson:gson:2.9.0")

    implementation("com.auth0:auth0:1.42.0")
    implementation("com.auth0:jwks-rsa:0.21.1")
    implementation("com.auth0:java-jwt:3.19.2")

    compileOnly("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.12.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<Jar>("jar") {
    enabled = false
}
