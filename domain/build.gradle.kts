plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

// 순수 Kotlin 모듈(ADR 0006). Android 플러그인을 쓰지 않으므로 android.* 를 import할 수 없다.
// Android 쪽(shared, data)은 jvm 타깃을 소비하고, jvm 타깃은 나중에 서버에서도 쓸 수 있다.
kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // 도메인 API(Flow, LocalDate)에 드러나는 타입이라 api로 노출한다.
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
