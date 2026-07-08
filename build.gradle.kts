plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

val javaToolchains = extensions.getByType<JavaToolchainService>()
val java26Launcher = javaToolchains.launcherFor {
    languageVersion = JavaLanguageVersion.of(26)
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:2.1.0")
    }
}

dependencies {
    // Web (includes RestClient, MVC, Jackson 3)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JPA 3.2 / Hibernate 7, via Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Bean Validation 3.1
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Actuator - useful to see the modularized auto-config / observability additions
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // JSpecify annotations for @NullMarked / @Nullable compile-time null safety
    implementation("org.jspecify:jspecify:1.0.0")

    // Spring Modulith - @ApplicationModule annotation for the feature packages below;
    // structure verification itself runs from spring-modulith-starter-test (ModularityTests).
    implementation("org.springframework.modulith:spring-modulith-api")

    // In-memory DB for the sample - swap for Postgres/MySQL driver in real use
    runtimeOnly("com.h2database:h2")

    // H2 console auto-configuration - split into its own module in Boot 4's
    // modularized autoconfigure, so it needs to be declared explicitly.
    runtimeOnly("org.springframework.boot:spring-boot-h2console")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    javaLauncher.set(java26Launcher)
    jvmArgs("--enable-preview")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-parameters", "--enable-preview"))
}

tasks.withType<JavaExec> {
    javaLauncher.set(java26Launcher)
    jvmArgs("--enable-preview")
}
