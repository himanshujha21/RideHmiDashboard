package com.example.ridehmidashboard.ui

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ridehmidashboard.R
import com.example.ridehmidashboard.data.RideScenario
import com.example.ridehmidashboard.data.TripInfo
import com.example.ridehmidashboard.ui.theme.AccentOrange
import com.example.ridehmidashboard.ui.theme.DashboardBackground
import com.example.ridehmidashboard.ui.theme.GaugeTrack
import com.example.ridehmidashboard.ui.theme.PanelSurface
import com.example.ridehmidashboard.ui.theme.PlatformSurface
import com.example.ridehmidashboard.ui.theme.RideHmiDashboardTheme
import com.example.ridehmidashboard.ui.theme.StatusAmber
import com.example.ridehmidashboard.ui.theme.StatusGreen
import com.example.ridehmidashboard.ui.theme.StatusGrey
import com.example.ridehmidashboard.ui.theme.StatusRed
import com.example.ridehmidashboard.ui.theme.TextPrimary
import com.example.ridehmidashboard.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val SPEED_BAR_MAX_KMH = 120

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(
        uiState = uiState,
        onPauseClick = viewModel::pause,
        onResumeClick = viewModel::resume,
        onRestartClick = viewModel::restart,
        modifier = modifier
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DashboardBackground)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        StatusBar(isStale = uiState.isStale, isPaused = uiState.isPaused)

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            RideView(
                uiState = uiState,
                modifier = Modifier
                    .weight(2.2f)
                    .fillMaxHeight()
            )
            AlertList(
                uiState = uiState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        BottomBar(
            batteryPercent = uiState.batteryPercent,
            motorStatus = uiState.motorStatus,
            isPaused = uiState.isPaused,
            onPauseClick = onPauseClick,
            onResumeClick = onResumeClick,
            onRestartClick = onRestartClick
        )
    }
}

@Composable
private fun StatusBar(isStale: Boolean, isPaused: Boolean) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.align(Alignment.CenterStart)) {
            when {
                isPaused -> Pill(text = stringResource(R.string.status_paused), color = StatusGrey)
                isStale -> Pill(text = stringResource(R.string.status_no_data), color = StatusAmber)
                else -> Pill(text = stringResource(R.string.status_live), color = StatusGreen)
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(30.dp)
                .background(AccentOrange, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo_mark),
                contentDescription = stringResource(R.string.logo_description),
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = rememberCurrentTime(),
            color = TextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun rememberCurrentTime(): String {
    val formatter = remember { SimpleDateFormat("EEE d MMM   h:mm a", Locale.getDefault()) }
    val time by produceState(initialValue = formatter.format(Date())) {
        while (true) {
            value = formatter.format(Date())
            delay(1_000L)
        }
    }
    return time
}

/** Centre of the dashboard: ride panel, speed, speed bar and the trip slider. */
@Composable
private fun RideView(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        RidePanel(
            scenario = uiState.scenario,
            rangeKm = uiState.rangeKm,
            batteryPercent = uiState.batteryPercent
        )
        SpeedReadout(speedKmh = uiState.speedKmh)
        SpeedBar(speedKmh = uiState.speedKmh, isOverSpeed = uiState.isOverSpeed)
        TripSlider(trip = uiState.trip)
    }
}

// Trapezoid panel, narrower at the bottom, like the top panel on the reference dashboard
private val PanelShape = GenericShape { size, _ ->
    val inset = size.width * 0.06f
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width - inset, size.height)
    lineTo(inset, size.height)
    close()
}

@Composable
private fun RidePanel(scenario: RideScenario?, rangeKm: Int?, batteryPercent: Int?) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .background(PanelSurface, PanelShape)
    ) {
        // Battery level shown as a bar along the top edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(DashboardBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((batteryPercent ?: 0) / 100f)
                    .fillMaxHeight()
                    .background(batteryColor(batteryPercent))
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(scenarioLabel(scenario)).uppercase(),
                color = AccentOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                maxLines = 1,
                modifier = Modifier
                    .background(DashboardBackground, RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = rangeKm?.toString() ?: stringResource(R.string.value_unavailable),
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = stringResource(R.string.range_unit), color = TextPrimary, fontSize = 11.sp)
                Text(text = stringResource(R.string.range_label), color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun SpeedReadout(speedKmh: Int?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = speedKmh?.toString() ?: stringResource(R.string.value_unavailable),
            color = if (speedKmh == null) TextSecondary else TextPrimary,
            fontSize = 88.sp,
            lineHeight = 88.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.speed_unit),
            color = TextSecondary,
            fontSize = 13.sp,
            letterSpacing = 3.sp
        )
    }
}

/** Speed shown as a red bar that grows out from the centre of a platform. */
@Composable
private fun SpeedBar(speedKmh: Int?, isOverSpeed: Boolean) {
    val targetFraction = (speedKmh ?: 0).coerceIn(0, SPEED_BAR_MAX_KMH) / SPEED_BAR_MAX_KMH.toFloat()
    val fraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 800),
        label = "speedBar"
    )
    val barColors = if (isOverSpeed) listOf(StatusRed, AccentOrange, StatusRed) else listOf(StatusRed, StatusRed)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        val platform = Path().apply {
            moveTo(size.width * 0.18f, 0f)
            lineTo(size.width * 0.82f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(platform, color = PlatformSurface)

        val trackLeft = size.width * 0.08f
        val trackWidth = size.width * 0.84f
        val trackTop = size.height * 0.55f
        val trackHeight = size.height * 0.3f
        val corner = CornerRadius(trackHeight / 2)
        drawRoundRect(
            color = GaugeTrack,
            topLeft = Offset(trackLeft, trackTop),
            size = Size(trackWidth, trackHeight),
            cornerRadius = corner
        )

        val fillWidth = trackWidth * fraction
        if (fillWidth > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(barColors),
                topLeft = Offset(trackLeft + (trackWidth - fillWidth) / 2, trackTop),
                size = Size(fillWidth, trackHeight),
                cornerRadius = corner
            )
        }
    }
}

/** ODO, trip and top speed, one at a time, switched with the arrows. */
@Composable
private fun TripSlider(trip: TripInfo) {
    val items = listOf(
        stringResource(R.string.odometer_label) to stringResource(R.string.odometer_value, trip.odometerKm),
        stringResource(R.string.trip_label) to stringResource(R.string.trip_value, trip.tripKm),
        stringResource(R.string.top_speed_label) to stringResource(R.string.top_speed_value, trip.topSpeedKmh)
    )
    var index by rememberSaveable { mutableIntStateOf(0) }
    val (label, value) = items[index]

    Row(verticalAlignment = Alignment.CenterVertically) {
        SmallIconButton(
            iconRes = R.drawable.ic_chevron_left,
            contentDescription = stringResource(R.string.previous_item),
            onClick = { index = (index - 1 + items.size) % items.size }
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 16.dp, end = 10.dp)
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = 16.dp)
        )
        SmallIconButton(
            iconRes = R.drawable.ic_chevron_right,
            contentDescription = stringResource(R.string.next_item),
            onClick = { index = (index + 1) % items.size }
        )
    }
}

/** Right-hand column with the kill switch and any active alerts. */
@Composable
private fun AlertList(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        KillSwitchAlert(status = uiState.killSwitchStatus)
        if (uiState.isOverSpeed) {
            OverSpeedAlert(limitKmh = DashboardViewModel.SPEED_LIMIT_KMH)
        }
        uiState.warning?.let { warning ->
            WarningAlert(message = stringResource(warningMessage(warning)))
        }
    }
}

@Composable
private fun BottomBar(
    batteryPercent: Int?,
    motorStatus: MotorStatus,
    isPaused: Boolean,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isPaused) {
            RoundIconButton(
                iconRes = R.drawable.ic_play,
                contentDescription = stringResource(R.string.button_resume),
                onClick = onResumeClick
            )
        } else {
            RoundIconButton(
                iconRes = R.drawable.ic_pause,
                contentDescription = stringResource(R.string.button_pause),
                onClick = onPauseClick
            )
        }
        RoundIconButton(
            iconRes = R.drawable.ic_restart,
            contentDescription = stringResource(R.string.button_restart),
            onClick = onRestartClick
        )
        Spacer(modifier = Modifier.weight(1f))
        BatteryPill(batteryPercent = batteryPercent)
        MotorPill(status = motorStatus)
    }
}

@StringRes
private fun scenarioLabel(scenario: RideScenario?): Int = when (scenario) {
    null -> R.string.scenario_connecting
    RideScenario.PARKED -> R.string.scenario_parked
    RideScenario.READY -> R.string.scenario_ready
    RideScenario.RIDING -> R.string.scenario_riding
    RideScenario.ACCELERATING -> R.string.scenario_accelerating
    RideScenario.MOTOR_DISABLED -> R.string.scenario_motor_disabled
    RideScenario.SENSOR_FAULT -> R.string.scenario_sensor_fault
    RideScenario.SIGNAL_LOST -> R.string.scenario_signal_lost
}

@StringRes
private fun warningMessage(warning: DashboardWarning): Int = when (warning) {
    DashboardWarning.INVALID_SPEED -> R.string.warning_invalid_speed
    DashboardWarning.MISSING_SIGNAL -> R.string.warning_missing_signal
    DashboardWarning.NO_DATA -> R.string.warning_no_data
    DashboardWarning.SOURCE_ERROR -> R.string.warning_source_error
}

@Composable
private fun PreviewContent(uiState: DashboardUiState) {
    DashboardContent(uiState = uiState, onPauseClick = {}, onResumeClick = {}, onRestartClick = {})
}

@Preview(widthDp = 860, heightDp = 400)
@Composable
private fun RidingPreview() {
    RideHmiDashboardTheme {
        PreviewContent(
            uiState = DashboardUiState(
                scenario = RideScenario.RIDING,
                speedKmh = 25,
                motorStatus = MotorStatus.ON,
                killSwitchStatus = KillSwitchStatus.INACTIVE,
                batteryPercent = 76,
                rangeKm = 91,
                trip = TripInfo(odometerKm = 1324.5, tripKm = 3.42, topSpeedKmh = 50)
            )
        )
    }
}

@Preview(widthDp = 860, heightDp = 400)
@Composable
private fun OverSpeedPreview() {
    RideHmiDashboardTheme {
        PreviewContent(
            uiState = DashboardUiState(
                scenario = RideScenario.ACCELERATING,
                speedKmh = 50,
                motorStatus = MotorStatus.ON,
                killSwitchStatus = KillSwitchStatus.INACTIVE,
                isOverSpeed = true,
                batteryPercent = 70,
                rangeKm = 84
            )
        )
    }
}

@Preview(widthDp = 860, heightDp = 400)
@Composable
private fun KillSwitchActivePreview() {
    RideHmiDashboardTheme {
        PreviewContent(
            uiState = DashboardUiState(
                scenario = RideScenario.MOTOR_DISABLED,
                speedKmh = 0,
                motorStatus = MotorStatus.OFF,
                killSwitchStatus = KillSwitchStatus.ACTIVE,
                batteryPercent = 12,
                rangeKm = 14
            )
        )
    }
}

@Preview(widthDp = 860, heightDp = 400)
@Composable
private fun NoDataPreview() {
    RideHmiDashboardTheme {
        PreviewContent(
            uiState = DashboardUiState(
                scenario = RideScenario.SIGNAL_LOST,
                warning = DashboardWarning.NO_DATA,
                isStale = true
            )
        )
    }
}
