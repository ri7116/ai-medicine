// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // ▼▼▼ 이 부분이 중요합니다. alias 대신 직접 버전을 명시하는 게 오류가 적습니다. ▼▼▼
    id("com.google.gms.google-services") version "4.4.2" apply false
}