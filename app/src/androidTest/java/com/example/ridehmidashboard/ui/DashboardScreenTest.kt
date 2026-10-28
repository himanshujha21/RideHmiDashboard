package com.example.ridehmidashboard.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ridehmidashboard.data.RideScenario
import com.example.ridehmidashboard.data.TripInfo
import com.example.ridehmidashboard.ui.theme.RideHmiDashboardTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var pauseClicks = 0
    private var resumeClicks = 0
    private var restartClicks = 0

    private fun showDashboard(uiState: DashboardUiState) {
        composeRule.setContent {
            RideHmiDashboardTheme {
                DashboardContent(
                    uiState = uiState,
                    onPauseClick = { pauseClicks++ },
                    onResumeClick = { resumeClicks++ },
                    onRestartClick = { restartClicks++ }
                )
            }
        }
    }

    @Test
    fun ridingState_showsSpeedAndStatus() {
        showDashboard(
            DashboardUiState(
                scenario = RideScenario.RIDING,
                speedKmh = 25,
                motorStatus = MotorStatus.ON,
                killSwitchStatus = KillSwitchStatus.INACTIVE,
                batteryPercent = 76,
                rangeKm = 91
            )
        )

        composeRule.onNodeWithText("25").assertIsDisplayed()
        composeRule.onNodeWithText("RIDING").assertIsDisplayed()
        composeRule.onNodeWithText("ON").assertIsDisplayed()
        composeRule.onNodeWithText("Inactive").assertIsDisplayed()
        composeRule.onNodeWithText("76%").assertIsDisplayed()
        composeRule.onNodeWithText("91").assertIsDisplayed()
        composeRule.onNodeWithText("LIVE").assertIsDisplayed()
    }

    @Test
    fun killSwitchActive_showsActive() {
        showDashboard(
            DashboardUiState(
                scenario = RideScenario.MOTOR_DISABLED,
                speedKmh = 0,
                motorStatus = MotorStatus.OFF,
                killSwitchStatus = KillSwitchStatus.ACTIVE
            )
        )

        composeRule.onNodeWithText("Active").assertIsDisplayed()
        composeRule.onNodeWithText("OFF").assertIsDisplayed()
    }

    @Test
    fun staleState_showsNoDataWarning() {
        showDashboard(DashboardUiState(warning = DashboardWarning.NO_DATA, isStale = true))

        composeRule.onNodeWithText("NO DATA").assertIsDisplayed()
        composeRule.onNodeWithText("No vehicle data received").assertIsDisplayed()
    }

    @Test
    fun overSpeed_showsSpeedExceeded() {
        showDashboard(DashboardUiState(speedKmh = 50, motorStatus = MotorStatus.ON, isOverSpeed = true))

        composeRule.onNodeWithText("Exceeded").assertIsDisplayed()
    }

    @Test
    fun tripSlider_switchesBetweenValues() {
        showDashboard(DashboardUiState(trip = TripInfo(odometerKm = 1324.0, tripKm = 2.5, topSpeedKmh = 50)))

        composeRule.onNodeWithText("ODO").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Next").performClick()
        composeRule.onNodeWithText("TRIP").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Next").performClick()
        composeRule.onNodeWithText("50 km/h").assertIsDisplayed()
    }

    @Test
    fun pauseAndRestartButtons_callViewModel() {
        showDashboard(DashboardUiState())

        composeRule.onNodeWithContentDescription("Pause").performClick()
        composeRule.onNodeWithContentDescription("Restart").performClick()

        assertEquals(1, pauseClicks)
        assertEquals(1, restartClicks)
    }

    @Test
    fun pausedState_showsResumeButton() {
        showDashboard(DashboardUiState(isPaused = true))

        composeRule.onNodeWithText("PAUSED").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Resume").performClick()

        assertEquals(1, resumeClicks)
    }
}
