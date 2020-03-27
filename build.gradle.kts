plugins {
  idea
  java
  application
  kotlin("jvm")
  id("org.ajoberstar.reckon")
  id("com.github.ben-manes.versions")
  id("com.github.johnrengelman.shadow")
}

val mainClass: String by project
val mapDbVersion: String by project
val junit4Version: String by project
val assertjVersion: String by project
val coroutinesVersion: String by project
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

allprojects {
  apply<JavaLibraryPlugin>()

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
    implementation("org.mapdb:mapdb:$mapDbVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation(platform("org.junit:junit-bom:$junitJupiterVersion"))
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("junit:junit:$junit4Version")
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
      // useJUnit()
      // useJUnitPlatform()
      useJUnitPlatform {
        includeEngines("junit-jupiter", "junit-vintage")
      }
      testLogging {
        showCauses = true
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
        )
      }
    }
    named("clean") {
      doLast {
        delete(
            project.buildDir,
            "${project.projectDir}/out"
        )
      }
    }
  }
}

tasks {
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
    lateinit var ver: String
    doFirst {
      ver = project.version.toString()
      archiveFileName.set("$buildDir/sources-$ver.zip")
      println("creation $ver ${archiveFileName.get()}")
    }
  }
  withType(Wrapper::class.java) {
    gradleVersion = gradleWrapperVersion
    distributionType = Wrapper.DistributionType.ALL
    // distributionType = Wrapper.DistributionType.BIN
  }
}

// defaultTasks("clean", "sources", "fatJar", "installDist")
defaultTasks("clean", "test", "sources", "shadowJar", "installDist")

reckon {
  scopeFromProp()
  // stageFromProp()
  snapshotFromProp()
}

tasks {
  register("version") {
    println(project.version.toString())
  }
  register("status") {
    doLast {
      val status = grgit.status()
      status?.let { s ->
        println("workspace is clean: ${s.isClean}")
        if (!s.isClean) {
          if (s.unstaged.allChanges.isNotEmpty()) {
            println("""all unstaged changes: ${s.unstaged.allChanges.joinToString(separator = "") { i -> "\n - $i" }}""")
          }
        }
      }
    }
  }
}
