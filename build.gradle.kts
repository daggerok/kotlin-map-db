plugins {
  idea
  java
  application
  kotlin("jvm")
  kotlin("plugin.spring")
  id("com.github.ben-manes.versions")
  id("com.github.johnrengelman.shadow")
}

val mainClass: String by project
val junit4Version: String by project
val assertjVersion: String by project
val javaVersion = JavaVersion.VERSION_1_8
val junitJupiterVersion: String by project
val gradleWrapperVersion: String by project

idea {
  module.iml {
    beforeMerged(Action<org.gradle.plugins.ide.idea.model.Module> {
      dependencies.clear()
    })
  }
}

repositories {
  mavenCentral()
}

java {
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(kotlin("reflect"))
  implementation("org.mapdb:mapdb:3.0.8")
  testImplementation("junit:junit:$junit4Version")
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
  testImplementation(platform("org.junit:junit-bom:$junitJupiterVersion"))
  testImplementation("org.assertj:assertj-core:$assertjVersion")
}

sourceSets {
  main {
    java.srcDir("src/main/kotlin")
  }
  test {
    java.srcDir("src/test/kotlin")
  }
}

application {
  mainClassName = mainClass
}

tasks {
  // register("fatJar", Jar::class.java) {
  //   //archiveAppendix.set("all")
  //   archiveClassifier.set("all")
  //   duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  //   manifest {
  //     attributes("Main-Class" to mainClass)
  //   }
  //   from(configurations.runtimeClasspath.get()
  //       .onEach { println("add from dependencies: ${it.name}") }
  //       .map { if (it.isDirectory) it else zipTree(it) })
  //   val sourcesMain = sourceSets.main.get()
  //   sourcesMain.allSource.forEach { println("add from sources: ${it.name}") }
  //   from(sourcesMain.output)
  // }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
      freeCompilerArgs += "-Xjsr305=strict"
      jvmTarget = "$javaVersion"
    }
  }
  withType<Test> {
    useJUnitPlatform()
    testLogging {
      showExceptions = true
      showStandardStreams = true
      events(
          org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
          org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
          org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
      )
    }
  }
  create<Zip>("sources") {
    dependsOn("clean")
    shouldRunAfter("clean", "assemble")
    description = "Archives sources in a zip file"
    group = "Archive"
    from("src") {
      into("src")
    }
    from(".github") {
      into(".github")
    }
    from("gradle") {
      into("gradle")
    }
    from(".gitattributes")
    from(".gitignore")
    from("gradlew")
    from("gradlew.bat")
    from("gradle.properties")
    from("build.gradle.kts")
    from("README.md")
    from("settings.gradle.kts")
    archiveFileName.set("${project.buildDir}/sources-${project.version}.zip")
  }
  named("clean") {
    doLast {
      delete(
          project.buildDir,
          "${project.projectDir}/out"
      )
    }
  }
  withType(Wrapper::class.java) {
    gradleVersion = gradleWrapperVersion
    distributionType = Wrapper.DistributionType.ALL
    // distributionType = Wrapper.DistributionType.BIN
  }
}

// defaultTasks("clean", "sources", "fatJar", "installDist")
defaultTasks("clean", "sources", "shadowJar", "installDist")
