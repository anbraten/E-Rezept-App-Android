import de.gematik.ti.erp.app
import de.gematik.ti.erp.overriding
import org.owasp.dependencycheck.reporting.ReportGenerator.Format
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("io.realm.kotlin")
    id("kotlin-parcelize")
    id("org.owasp.dependencycheck")
    id("com.jaredsburrows.license")
    id("de.gematik.ti.erp.dependencies")
}

val USER_AGENT: String by overriding()
val VERSION_CODE: String by overriding()
val VERSION_NAME: String by overriding()
val DEBUG_TEST_IDS_ENABLED: String by overriding()
val VAU_OCSP_RESPONSE_MAX_AGE: String by overriding()

afterEvaluate {
    val taskRegEx = """assemble(Google|Huawei)(PuExternalDebug|PuExternalRelease)""".toRegex()
    tasks.forEach { task ->
        taskRegEx.matchEntire(task.name)?.let {
            val (_, version, flavor) = it.groupValues
            task.dependsOn(tasks.getByName("license${version}${flavor}Report"))
        }
    }
}

tasks.named("preBuild") {
    dependsOn(":ktlint", ":detekt")
}

licenseReport {
    generateCsvReport = false
    generateHtmlReport = false
    generateJsonReport = true
    copyJsonReportToAssets = true
}

android {
    // currently not working with an app suffix
    // namespace = "de.gematik.ti.erp.app"
    defaultConfig {
        applicationId = "de.gematik.ti.erp.app"
        versionCode = VERSION_CODE.toInt()
        versionName = VERSION_NAME

        testApplicationId = "de.gematik.ti.erp.app.test.test"
        testInstrumentationRunner = "de.gematik.ti.erp.app.test.test.MainTest"
        testInstrumentationRunnerArguments += "clearPackageData" to "true"
        testInstrumentationRunnerArguments += "useTestStorageService" to "true"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += "room.schemaLocation" to "$projectDir/schemas"
            }
        }
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    sourceSets {
        val test by getting
        test.apply {
            resources.srcDirs("src/test/res")
        }
        val androidTest by getting
        androidTest.apply {
            resources.srcDirs("src/test/res")
            assets.srcDirs("$projectDir/schemas")
        }
    }

    androidResources {
        noCompress("srt", "csv", "json")
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    dependencyCheck {
        analyzers.assemblyEnabled = false

        formats = listOf(Format.HTML, Format.XML)
        scanConfigurations = configurations.filter {
            it.name.startsWith("api") ||
                it.name.startsWith("implementation") ||
                it.name.startsWith("kapt")
        }.map {
            it.name
        }
    }

    val signingPropsFile = project.rootProject.file("signing.properties")
    if (signingPropsFile.canRead()) {
        println("Signing properties found: $signingPropsFile")
        val signingProps = Properties()
        signingProps.load(signingPropsFile.inputStream())
        signingConfigs {
            fun creatingRelease() = creating {
                val target = this.name // property name; e.g. googleRelease
                println("Create signing config for: $target")
                storeFile = signingProps["$target.storePath"]?.let { file(it) }
                println("\tstore: ${signingProps["$target.storePath"]}")
                keyAlias = signingProps["$target.keyAlias"] as? String
                println("\tkeyAlias: ${signingProps["$target.keyAlias"]}")
                storePassword = signingProps["$target.storePassword"] as? String
                keyPassword = signingProps["$target.keyPassword"] as? String
            }
            if (signingProps["googleRelease.storePath"] != null) {
                val googleRelease by creatingRelease()
            }
            if (signingProps["huaweiRelease.storePath"] != null) {
                val huaweiRelease by creatingRelease()
            }
        }
    } else {
        println("No signing properties found!")
    }

    buildTypes {
        val release by getting {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingPropsFile.canRead()) {
                signingConfig = signingConfigs.getByName("googleRelease")
            }
            resValue("string", "app_label", "E-Rezept")
        }
        val debug by getting {
            applicationIdSuffix = ".test"
            resValue("string", "app_label", "eRp-Test")
            versionNameSuffix = "-debug"
            signingConfigs {
                getByName("debug") {
                    storeFile = file("$rootDir/keystore/debug.keystore")
                    keyAlias = "androiddebugkey"
                    storePassword = "android"
                    keyPassword = "android"
                }
            }
        }
    }
    flavorDimensions += listOf("version")
    productFlavors {
        val flavor = project.findProperty("buildkonfig.flavor") as? String
        if (flavor?.startsWith("google") == true) {
            create(flavor) {
                dimension = "version"
                signingConfig = signingConfigs.findByName("googleRelease")
            }
        }
        if (flavor?.startsWith("huawei") == true) {
            create(flavor) {
                dimension = "version"
                applicationIdSuffix = ".huawei"
                versionNameSuffix = "-huawei"
                signingConfig = signingConfigs.findByName("huaweiRelease")
            }
        }
        if (flavor?.startsWith("konnektathonRu") == true) {
            create(flavor) {
                dimension = "version"
                applicationIdSuffix = ".konnektathon.ru"
                versionNameSuffix = "-konnektathon-RU"
                signingConfig = signingConfigs.findByName("googleRelease")
            }
        }
        if (flavor?.startsWith("konnektathonDevru") == true) {
            create(flavor) {
                dimension = "version"
                applicationIdSuffix = ".konnektathon.rudev"
                versionNameSuffix = "-konnektathon-RUDEV"
                signingConfig = signingConfigs.findByName("googleRelease")
            }
        }
    }

    packagingOptions {
        resources {
            excludes += "META-INF/**"
            // for JNA and JNA-platform
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
            // for byte-buddy
            excludes += "META-INF/licenses/ASM"
            pickFirsts += "win32-x86-64/attach_hotspot_windows.dll"
            pickFirsts += "win32-x86/attach_hotspot_windows.dll"
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0"
    }
}

compose.android.useAndroidX = true
compose.android.androidxVersion = app.composeVersion

dependencies {
    implementation(project(":common"))
    testImplementation(project(":common"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))

    app {
        dataMatrix {
            implementation(mlkitBarcodeScanner)
            implementation(zxing)
        }
        kotlinX {
            implementation(coroutines("core"))
            implementation(coroutines("android"))
        }
        android {
            coreLibraryDesugaring(desugaring)

            implementation(legacySupport)
            implementation(appcompat)
            implementation(coreKtx)
            implementation(datastorePreferences)
            implementation(security)
            implementation(biometric)
            implementation(webkit)
            implementation(lifecycle("viewmodel-compose"))
            implementation(lifecycle("process")) {
                // FIXME: remove if AGP > 7.2.0-alpha05 can handle cyclic dependencies (again)
                exclude(group = "androidx.lifecycle", module = "lifecycle-runtime")
            }

            implementation(composeNavigation)
            implementation(composeActivity)
            implementation(composePaging)

            implementation(camera("camera2"))
            implementation(camera("lifecycle"))
            implementation(camera("view", cameraViewVersion))
            implementation(imageCropper)

            debugImplementation(processPhoenix)
        }
        dependencyInjection {
            compileOnly(kodein("di-framework-compose"))
            androidTestImplementation(kodein("di-framework-compose"))
        }
        logging {
            implementation(napier)
        }
        lottie {
            implementation(lottie)
        }
        serialization {
            implementation(fhir)
            implementation(kotlinXJson)
        }
        crypto {
            implementation(jose4j)
            implementation(bouncyCastle("bcprov"))
            implementation(bouncyCastle("bcpkix"))
            testImplementation(bouncyCastle("bcprov", "jdk15on"))
            testImplementation(bouncyCastle("bcpkix", "jdk15on"))
        }
        network {
            implementation(retrofit2("retrofit"))
            implementation(retrofit2KotlinXSerialization)
            implementation(okhttp3("okhttp"))
            implementation(okhttp3("logging-interceptor"))
        }
        database {
            compileOnly(realm)
            testCompileOnly(realm)

            implementation(sqlCipher)
            implementation(room("runtime"))
            implementation(room("ktx"))
            ksp(room("compiler"))
        }
        compose {
            implementation(runtime)
            implementation(foundation)
            implementation(material)
            implementation(materialIconsExtended)
            implementation(animation)
            implementation(uiTooling)
            implementation(preview)
            implementation(accompanist("swiperefresh"))
            implementation(accompanist("flowlayout"))
            implementation(accompanist("pager"))
            implementation(accompanist("pager-indicators"))
            implementation(accompanist("systemuicontroller"))
        }
        passwordStrength {
            implementation(zxcvbn)
        }
        playServices {
            implementation(location)
            implementation(safetynet)
        }

        androidTest {
            testImplementation(archCore)
            androidTestImplementation(core)
            androidTestImplementation(rules)
            androidTestImplementation(junitExt)
            androidTestImplementation(runner)
            androidTestUtil(orchestrator)
            androidTestUtil(services)
            androidTestImplementation(navigation)
            androidTestImplementation(espresso)
        }
        kotlinXTest {
            testImplementation(coroutinesTest)
        }
        composeTest {
            androidTestImplementation(ui)
            androidTestImplementation(junit4)
        }
        networkTest {
            testImplementation(mockWebServer)
        }
        databaseTest {
            androidTestImplementation(roomTesting)
        }
        test {
            testImplementation(junit4)
            testImplementation(snakeyaml)
            testImplementation(json)
            testImplementation(mockk("mockk"))
            androidTestImplementation(mockk("mockk-android"))

            androidTestImplementation("io.cucumber:cucumber-android:4.9.0")
            androidTestImplementation("io.cucumber:cucumber-picocontainer:4.8.1")
        }
    }
}
