plugins { id("com.android.application") }
android {
    namespace = "se.adde.sleeplog"
    compileSdk = 37
    defaultConfig {
        applicationId = "se.adde.sleeplog"
        minSdk = 23
        targetSdk = 36
        versionCode = 4
        versionName = "0.1.3"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    bundle { language { enableSplit = false } }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.maxHeapSize = "1g"
            // Robolectric's Android 16 file descriptors require this on recent JDKs.
            it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED")
            it.systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
        }
    }
}
dependencies {
    implementation(project(":core"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.17")
}
