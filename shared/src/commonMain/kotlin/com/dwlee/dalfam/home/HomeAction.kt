package com.dwlee.dalfam.home

/**
 * 홈 화면에서 사용자가 할 수 있는 행동 전부. ViewModel은 [HomeViewModel.onAction] 하나로 받는다(ADR 0007).
 *
 * 행동이 한 타입에 모여 있어서 "이 화면에서 무슨 일이 일어날 수 있나"를 여기서 한눈에 본다.
 * `sealed`라서 `when`에 새 행동을 빠뜨리면 컴파일 에러가 난다.
 *
 * 값이 없는 행동은 `data object`로 둔다. 값이 필요해지면(예: 날짜를 골라 기록)
 * `data class StartPeriod(val date: LocalDate)`처럼 바꾼다.
 */
sealed interface HomeAction {
    /** "시작했어요" 버튼. 오늘을 생리 시작일로 기록한다. */
    data object StartPeriod : HomeAction

    /** "끝났어요" 버튼. 진행 중인 생리를 오늘로 끝낸다. */
    data object EndPeriod : HomeAction

    /** 스낵바를 보여 준 뒤. 지우지 않으면 화면 회전 때 같은 안내가 또 뜬다. */
    data object MessageShown : HomeAction
}
