import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room3)
    // @Module·@Singleton 같은 어노테이션을 읽어 Koin 등록 코드를 컴파일할 때 만들어 준다.
    // 정의(definition)가 있는 Gradle 모듈마다 붙여야 한다.
    alias(libs.plugins.koinCompiler)
}

kotlin {
    compilerOptions {
        // Room이 생성하는 DalfamDatabaseConstructor가 expect/actual object라 Beta 경고를 끈다.
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    android {
        namespace = "com.dwlee.dalfam.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    // DB 테스트용. Android 호스트 테스트에서는 sqlite-bundled의 Android용 네이티브 라이브러리를
    // 불러올 수 없어서, 데스크톱용 네이티브가 들어 있는 jvm 타깃에서 실제 SQLite로 검증한다.
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(libs.room3.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
        }
        androidMain.dependencies {
            // androidContext()로 Application Context를 꺼내 쓰기 위해
            implementation(libs.koin.android)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}
dependencies {
    listOf("kspAndroid", "kspJvm", "kspIosArm64", "kspIosSimulatorArm64").forEach {
        add(it, libs.room3.compiler)
    }
}
