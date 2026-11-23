package com.dwlee.dalfam.home

import com.dwlee.dalfam.domain.cycle.model.CycleStatus

/**
 * 홈 화면이 그리는 데 필요한 모든 것. 화면은 이 값 하나만 보고 그린다.
 *
 * 불변(data class + val)이라 바뀔 때는 copy로 새 객체를 만든다.
 * 그래서 Compose가 "이전 값과 다르다"를 쉽게 알아채고 다시 그린다.
 *
 * @property isLoading DB에서 첫 값을 받기 전.
 * @property status 오늘의 상태. 기록이 하나도 없으면 null.
 * @property message 한 번만 보여 줄 안내(스낵바). 보여 준 뒤 [HomeViewModel.onMessageShown]으로 지운다.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val status: CycleStatus? = null,
    val message: HomeMessage? = null,
)

/** 버튼을 눌렀을 때의 결과 안내. 문구는 화면이 정한다(ViewModel은 문자열을 모른다). */
enum class HomeMessage {
    PERIOD_STARTED,
    PERIOD_ENDED,
    ALREADY_ONGOING,
    NO_ONGOING_PERIOD,
    OVERLAPS,
    FUTURE_DATE,
    BEFORE_START,
}
