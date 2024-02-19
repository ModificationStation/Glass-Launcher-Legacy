import edu.sc.seis.launch4j.tasks.Launch4jLibraryTask
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository
import java.lang.Exception
import java.net.URI
import java.net.URL

plugins {
    java
    id("edu.sc.seis.launch4j") version "3.0.4"
    id("io.freefair.lombok") version "8.2.2"
    id("io.github.kota65535.dependency-report") version "2.0.1"
    id("com.google.osdetector") version "1.7.3"
}

repositories {
    maven(url="https://maven.fabricmc.net/")
    maven(url="https://maven.glass-launcher.net/babric")
    maven(url="https://maven.glass-launcher.net/snapshots")
    maven(url="https://jitpack.io")
    mavenCentral()
}

dependencies {
    // Some misc annotations to help with context hints and autocompletion.
    // https://www.jetbrains.com/help/idea/annotating-source-code.html
    compileOnly("org.jetbrains:annotations:24.0.1")

    // Used to handle JSON data objects.
    // https://github.com/falkreon/Jankson
    implementation("com.google.code.gson:gson:2.10.1")

    // Used to validate various URIs.
    // https://mvnrepository.com/artifact/commons-validator/commons-validator
    implementation("commons-validator:commons-validator:1.7")

    // Used for various things related to files
    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.11.0")

    // Used to verify Minecraft and program arguments.
    // https://mvnrepository.com/artifact/commons-cli/commons-cli
    implementation("commons-cli:commons-cli:1.5.0")

    // Used for parsing markdown various markdown styles. Has custom markdown support too.
    // https://github.com/commonmark/commonmark-java
    implementation("org.commonmark:commonmark:0.21.0")
    implementation("org.commonmark:commonmark-ext-autolink:0.21.0")

    // Used to handle accessing the mod repo.
    // https://glass-repo.net https://github.com/calmilamsy/Glass-Site-Repo-Java-API
    // No public documentation yet, though if you figure it out, you can use it for whatever.
    implementation("net.glasslauncher.repo:glass-site-java-api:0.7")

    // Various utility classes that help dramatically with handling files and URLs.
    // https://github.com/calmilamsy/glass-commons
    // Same thing, use if you want, though there are no docs.
    implementation("net.glasslauncher:commons:1.4")

    // Used to launch minecraft. Pretty powerful and mostly straight forward to use.
    // https://github.com/calmilamsy/glass-launch-wrapper
    implementation("com.github.calmilamsy:glass-launch-wrapper:f91c92f")

    // Used for windows to read the registry to find java versions.
    // https://github.com/java-native-access/jna
    implementation("net.java.dev.jna:jna:5.12.1")
    implementation("net.java.dev.jna:jna-platform:5.12.1")
}

group="glass-launcher"
version="0.5.0"

// Tells gradle to scream at you if you try to use any post Java 8 features.
java.sourceCompatibility = JavaVersion.VERSION_1_8

val depsJsonPath: String = "build/generated/configs/dependencies.gmc"
val launchWrapperDepsJsonPath: String = "build/generated/configs/launchwrapper_dependencies.gmc"

// Ensures encoding doesn't get screwed up when compiling on various OSes and language encodings.
// If you get an error when compiling, remove any special characters you added.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Debug task for when repos break.
tasks.register("listrepos") {
    doLast {
        println("Repositories:")
        project.repositories.forEach { println("Name: " + it.name) }
    }
}

// I don't fucking know at this point, I just know that trying to add two arraylists from in here and returning the result doesn't work. What the hell, gradle?
fun getDepChildren(baseList: List<ResolvedDependency>, dependencyList: ArrayList<ResolvedDependency>) {
    baseList.forEach {
        dependencyList.add(it)
        getDepChildren(it.children.toList(), dependencyList)
    }
}

fun checkRepository(dependency: ResolvedDependency): String {
    for (repository: ArtifactRepository in rootProject.repositories) {
        if (repository is DefaultMavenArtifactRepository) {
            val url: URI = repository.url
            var urlString = url.toString()
            if (!urlString.endsWith("/")) {
                urlString += "/"
            }

            val jarUrl = String.format(
                "%s%s/%s/%s/%s-%s.jar",
                urlString,
                dependency.moduleGroup.replace(".", "/"),
                dependency.moduleName,
                dependency.moduleVersion,
                dependency.moduleName,
                dependency.moduleVersion
            )

            try {
                val jarFile = URL(jarUrl)
                val inStreamJar = jarFile.openStream()
                if (inStreamJar != null) {
                    return repository.url.toString()
                }
            } catch (ignored: Exception) {
            }
        }
    }
    println(String.format("!! Can't find valid repo for \"%s\", are you using a local dependency? !!", dependency.name))
    return "null"
}

fun parseGMC(file: File): ArrayList<HashMap<String, String>> {
    val readValues: ArrayList<HashMap<String, String>> = arrayListOf()
    val text: String = file.readText()
    for (entry: String in text.split("||")) {
        val values: List<String> = entry.split("|")
        readValues.add(hashMapOf("name" to values[0], "url" to values[1]))
    }
    return readValues
}

fun dumpGMC(values: ArrayList<HashMap<String, String>>, file: File) {
    val entryList: ArrayList<String> = arrayListOf()
    values.forEach {
        entryList.add(it.values.joinToString("|"))
    }
    file.writeText(entryList.joinToString("||"))
}

fun generateDependencyConfig(fileToUse: String, rootDependencyList: ArrayList<ResolvedDependency>) {

    val jsonFile = File(fileToUse)

    val oldDeps: ArrayList<HashMap<String, String>>? = try {
        if (jsonFile.exists()) parseGMC(jsonFile) else null
    }
    catch (e: Exception) {
        e.printStackTrace()
        null
    }

    val depList: ArrayList<ResolvedDependency> = rootDependencyList
    getDepChildren(ArrayList(depList), depList)

    val newJson: ArrayList<HashMap<String, String>> = arrayListOf()

    depList.forEach { dependency: ResolvedDependency ->
        var resolvedDependency: HashMap<String, String>? =
            oldDeps?.firstOrNull { it["name"].equals(dependency.name) }

        if (resolvedDependency == null) {
            resolvedDependency = hashMapOf(
                "name" to dependency.name,
                "url" to checkRepository(dependency)
            )
            println(String.format("? Used repo scan for dependency \"%s\"", dependency.name))
        }
        oldDeps?.add(resolvedDependency)
        newJson.add(resolvedDependency)
    }
    if (!jsonFile.parentFile.exists()) {
        jsonFile.parentFile.mkdirs()
    }
    dumpGMC(newJson, jsonFile)
}

/**
 * This fancy task generates configs for glass-launcher's dependency downloader. Used in builds, unused in gradle runs.
 * Avoids polling repos if the dependency name and org matches the last generated config.
 */
tasks.register("generateDependencyConfigs") {
    doLast {
        generateDependencyConfig(depsJsonPath, ArrayList(project.configurations.runtimeClasspath.get().resolvedConfiguration.firstLevelModuleDependencies.toList()))
        generateDependencyConfig(launchWrapperDepsJsonPath, arrayListOf(project.configurations.runtimeClasspath.get().resolvedConfiguration.firstLevelModuleDependencies.first { it.moduleName.equals("glass-launch-wrapper") }))
    }
}

// Creates a fully functional exe for windows. If this doesn't work on your local system, remove the dependsOn line below.
tasks.withType<Launch4jLibraryTask> {
    mainClassName.set("net.glasslauncher.legacy.Main")
    icon.set("${projectDir}/glass.ico")
    outfile.set("${project.name}-${project.version}.exe")
    setJarTask(tasks.jar.get())
}

tasks.build {
    dependsOn("createExe")
}

tasks.withType<Jar> {
    dependsOn("generateDependencyConfigs")
    from(depsJsonPath)
    manifest {
        attributes["Main-Class"] = "net.glasslauncher.legacy.Main"
        attributes["Implementation-Title"] = "Glass Launcher Legacy"
        attributes["Implementation-Version"] = version
    }
}
