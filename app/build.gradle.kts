import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.assetControl"
    compileSdk = libs.versions.androidTargetSdk.get().toInt()

    // Manejo de versiones
    val versionPropsFile = file("version.properties")
    val versionProps = Properties()
    if (!versionPropsFile.exists()) {
        versionPropsFile.createNewFile()
        versionProps["VERSION_PATCH"] = "0"
        versionProps["VERSION_NUMBER"] = "0"
        versionProps["VERSION_BUILD"] = "-1"
        versionPropsFile.writer().use { versionProps.store(it, null) }
    }

    versionProps.load(FileInputStream(versionPropsFile))
    val value = if (gradle.startParameter.taskNames.any {
            it.contains("assembleRelease", ignoreCase = true) ||
                    it.contains("bundleRelease", ignoreCase = true)
        }) 1 else 0

    val versionMajor = 13
    val versionMinor = 0
    val versionPatch = versionProps["VERSION_PATCH"].toString().toInt() + value
    val versionBuild = versionProps["VERSION_BUILD"].toString().toInt() + 1
    val versionNumber = versionProps["VERSION_NUMBER"].toString().toInt() + value

    versionProps["VERSION_PATCH"] = versionPatch.toString()
    versionProps["VERSION_BUILD"] = versionBuild.toString()
    versionProps["VERSION_NUMBER"] = versionNumber.toString()
    versionPropsFile.writer().use { versionProps.store(it, null) }

    defaultConfig {
        applicationId = "com.example.assetControl13"
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
        versionCode = versionNumber
        versionName = "$versionMajor.$versionMinor.$versionPatch ($versionBuild)"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            proguardFile("proguard-rules.pro")
            proguardFile("proguard-ktor.pro")
        }
    }

    applicationVariants.all {
        sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmCompatibility.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmCompatibility.get())
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources.excludes.add("META-INF/*")
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/services/javax.annotation.processing.Processor")
        resources.excludes.add("META-INF/services/org.xmlpull.v1.XmlPullParserFactory")
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable.add("MissingTranslation")
    }

    testOptions {
        unitTests.all {
            it.extensions.configure<JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
        unitTests.isReturnDefaultValues = true
    }

    buildToolsVersion = libs.versions.buildToolsVer.get()

    sourceSets {
        getByName("main") {
            res.setSrcDirs(
                listOf(
                    "src/main/res/layouts/activities",
                    "src/main/res/layouts/fragments",
                    "src/main/res/layouts/adapters",
                    "src/main/res/layouts",
                    "src/main/res"
                )
            )
        }
    }

    ksp {
        arg("room.generateKotlin", "true")
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
// Desugaring
    coreLibraryDesugaring(libs.desugar)

    // AndroidX
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.google.material)
    implementation(libs.androidx.vectordrawable)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.percentlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.recyclerview)

    // Google
    implementation(libs.google.play.services.vision)
    implementation(libs.google.ksoap2)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Koin
    implementation(libs.koin.android)

    // Third-party
    implementation(libs.freereflection)
    implementation(libs.keyboardvisibility)
    implementation(libs.evalex)
    implementation(libs.async.http)
    implementation(libs.dotenv)
    implementation(libs.parceler.api)
    annotationProcessor("org.parceler:parceler:${libs.versions.parcelerVersion.get()}")
    implementation(libs.commons.net)

    // Debug tools
    debugImplementation(libs.leakcanary)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit)
    testImplementation(libs.mockito)
    implementation(libs.androidx.testext.junit)
    androidTestImplementation(libs.koin.test)

    // Dacosys
    implementation(libs.dacosys.imagecontrol)
    implementation(libs.dacosys.easyfloat)
    implementation(libs.dacosys.zxing)
    implementation(libs.dacosys.honeywell)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmCompatibility.get()))
        freeCompilerArgs.addAll(
            listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all",
                "-Xskip-prerelease-check"
            )
        )
    }
}