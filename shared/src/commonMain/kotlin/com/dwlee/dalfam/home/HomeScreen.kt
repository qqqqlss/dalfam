package com.dwlee.dalfam.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dwlee.dalfam.domain.cycle.model.CyclePhase
import com.dwlee.dalfam.domain.cycle.model.CycleStatus
import dalfam.shared.generated.resources.Res
import dalfam.shared.generated.resources.home_button_end
import dalfam.shared.generated.resources.home_button_start
import dalfam.shared.generated.resources.home_cycle_day
import dalfam.shared.generated.resources.home_days_late
import dalfam.shared.generated.resources.home_days_until_next_period
import dalfam.shared.generated.resources.home_empty
import dalfam.shared.generated.resources.message_already_ongoing
import dalfam.shared.generated.resources.message_before_start
import dalfam.shared.generated.resources.message_future_date
import dalfam.shared.generated.resources.message_no_ongoing_period
import dalfam.shared.generated.resources.message_overlaps
import dalfam.shared.generated.resources.message_period_ended
import dalfam.shared.generated.resources.message_period_started
import dalfam.shared.generated.resources.phase_follicular
import dalfam.shared.generated.resources.phase_luteal
import dalfam.shared.generated.resources.phase_menstrual
import dalfam.shared.generated.resources.phase_ovulation
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * ViewModel과 연결되는 화면. 상태를 꺼내서 [HomeContent]에 넘기기만 한다.
 *
 * `koinViewModel()`: Koin에 등록된 HomeViewModel을 꺼낸다.
 * 화면 회전처럼 다시 그려져도 같은 ViewModel 인스턴스를 돌려준다.
 */
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    // 화면이 보일 때만 수집한다(백그라운드로 가면 멈춘다).
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

/**
 * 상태를 받아 그리기만 하는 화면. ViewModel을 모르기 때문에 미리보기·테스트가 쉽다.
 * (stateless composable: 상태는 위에서 내려받고, 행동은 [onAction] 람다 하나로 위에 올린다)
 *
 * 버튼이 늘어도 파라미터는 `onAction` 하나 그대로다. 행동 종류는 [HomeAction]에만 추가한다.
 */
@Composable
fun HomeContent(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // message가 바뀔 때마다 한 번 실행된다. 보여 준 뒤 ViewModel에 "다 봤다"고 알린다.
    // LaunchedEffect 안은 Composable이 아니라서 stringResource 대신 suspend 함수 getString을 쓴다.
    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(getString(message.textRes()))
        onAction(HomeAction.MessageShown)
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                when {
                    state.isLoading -> CircularProgressIndicator()
                    state.status == null -> Text(stringResource(Res.string.home_empty))
                    else -> StatusCard(state.status)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onAction(HomeAction.StartPeriod) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.home_button_start))
                }
                OutlinedButton(onClick = { onAction(HomeAction.EndPeriod) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.home_button_end))
                }
            }
        }
    }
}

@Composable
private fun StatusCard(status: CycleStatus) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(Res.string.home_cycle_day, status.cycleDay),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(stringResource(status.phase.labelRes()), style = MaterialTheme.typography.titleMedium)
            Text(
                // plurals: 영어는 1 day / 2 days처럼 숫자에 따라 문구가 바뀐다.
                // 첫 번째 숫자는 단수·복수 판단용, 뒤의 숫자는 %1$d에 들어갈 값이다.
                if (status.isLate) {
                    pluralStringResource(Res.plurals.home_days_late, status.daysLate, status.daysLate)
                } else {
                    pluralStringResource(
                        Res.plurals.home_days_until_next_period,
                        status.daysUntilNextPeriod,
                        status.daysUntilNextPeriod,
                    )
                },
            )
        }
    }
}

private fun CyclePhase.labelRes(): StringResource = when (this) {
    CyclePhase.MENSTRUAL -> Res.string.phase_menstrual
    CyclePhase.FOLLICULAR -> Res.string.phase_follicular
    CyclePhase.OVULATION -> Res.string.phase_ovulation
    CyclePhase.LUTEAL -> Res.string.phase_luteal
}

private fun HomeMessage.textRes(): StringResource = when (this) {
    HomeMessage.PERIOD_STARTED -> Res.string.message_period_started
    HomeMessage.PERIOD_ENDED -> Res.string.message_period_ended
    HomeMessage.ALREADY_ONGOING -> Res.string.message_already_ongoing
    HomeMessage.NO_ONGOING_PERIOD -> Res.string.message_no_ongoing_period
    HomeMessage.OVERLAPS -> Res.string.message_overlaps
    HomeMessage.FUTURE_DATE -> Res.string.message_future_date
    HomeMessage.BEFORE_START -> Res.string.message_before_start
}

@Preview
@Composable
private fun HomeContentEmptyPreview() {
    MaterialTheme {
        HomeContent(
            state = HomeUiState(isLoading = false),
            onAction = {},
        )
    }
}
