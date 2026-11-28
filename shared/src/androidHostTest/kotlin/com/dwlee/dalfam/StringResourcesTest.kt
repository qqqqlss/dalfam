package com.dwlee.dalfam

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 영어(기본)와 한국어 문자열 파일의 키가 똑같은지 검사한다.
 *
 * 한쪽에만 키를 추가하면, 빠진 언어에서는 조용히 영어(기본값)가 나오거나
 * 기본값에 없으면 앱이 크래시한다. 그런 실수를 커밋 전에 잡기 위한 테스트.
 *
 * 호스트 테스트는 모듈 폴더(shared/)에서 실행되므로 상대 경로로 파일을 읽는다.
 */
class StringResourcesTest {

    private val resourcesDir = File("src/commonMain/composeResources")

    @Test
    fun 한국어와_영어의_문자열_키가_같다() {
        val english = keysOf(File(resourcesDir, "values/strings.xml"))
        val korean = keysOf(File(resourcesDir, "values-ko/strings.xml"))

        assertTrue(english.isNotEmpty(), "영어 문자열을 하나도 읽지 못했습니다. 경로를 확인하세요.")
        assertEquals(
            english,
            korean,
            "한국어에만 없는 키: ${english - korean}, 영어에만 없는 키: ${korean - english}",
        )
    }

    private fun keysOf(file: File): Set<String> {
        assertTrue(file.exists(), "${file.path} 파일이 없습니다.")
        return Regex("""<(string|plurals) name="([^"]+)"""")
            .findAll(file.readText())
            .map { "${it.groupValues[1]}:${it.groupValues[2]}" }
            .toSet()
    }
}
