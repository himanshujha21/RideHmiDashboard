package com.example.ridehmidashboard.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridehmidashboard.R
import com.example.ridehmidashboard.ui.theme.AmberTint
import com.example.ridehmidashboard.ui.theme.CardSurface
import com.example.ridehmidashboard.ui.theme.GreenTint
import com.example.ridehmidashboard.ui.theme.RedTint
import com.example.ridehmidashboard.ui.theme.StatusAmber
import com.example.ridehmidashboard.ui.theme.StatusGreen
import com.example.ridehmidashboard.ui.theme.StatusGrey
import com.example.ridehmidashboard.ui.theme.StatusRed
import com.example.ridehmidashboard.ui.theme.TextPrimary
import com.example.ridehmidashboard.ui.theme.TextSecondary

@Composable
fun Pill(text: String, color: Color) {
    Row(
        modifier = Modifier
            .border(1.dp, color, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun RoundIconButton(@DrawableRes iconRes: Int, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(CardSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = TextPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun SmallIconButton(@DrawableRes iconRes: Int, contentDescription: String, onClick: () -> Unit) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = contentDescription,
        tint = TextSecondary,
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(4.dp)
    )
}

/** Icon plus a two-line label, used for the items in the right-hand column. */
@Composable
private fun AlertItem(
    @DrawableRes iconRes: Int,
    iconColor: Color,
    title: String,
    value: String,
    valueColor: Color,
    background: Color
) {
    Row(
        modifier = Modifier
            .background(background, RoundedCornerShape(50))
            .padding(start = 12.dp, end = 18.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, color = TextSecondary, fontSize = 12.sp)
            Text(text = value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun KillSwitchAlert(status: KillSwitchStatus) {
    val title = stringResource(R.string.kill_switch_title)
    when (status) {
        // Red background so it stands out from every other state
        KillSwitchStatus.ACTIVE -> AlertItem(
            iconRes = R.drawable.ic_power,
            iconColor = StatusRed,
            title = title,
            value = stringResource(R.string.kill_switch_active),
            valueColor = StatusRed,
            background = RedTint
        )
        KillSwitchStatus.INACTIVE -> AlertItem(
            iconRes = R.drawable.ic_power,
            iconColor = StatusGreen,
            title = title,
            value = stringResource(R.string.kill_switch_inactive),
            valueColor = StatusGreen,
            background = CardSurface
        )
        KillSwitchStatus.UNKNOWN -> AlertItem(
            iconRes = R.drawable.ic_power,
            iconColor = StatusAmber,
            title = title,
            value = stringResource(R.string.value_unavailable),
            valueColor = StatusAmber,
            background = CardSurface
        )
    }
}

@Composable
fun OverSpeedAlert(limitKmh: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(TextPrimary, CircleShape)
                .border(4.dp, StatusRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = limitKmh.toString(), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = stringResource(R.string.speed_exceeded_line1),
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.speed_exceeded_line2),
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WarningAlert(message: String) {
    Row(
        modifier = Modifier
            .background(AmberTint, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_warning),
            contentDescription = null,
            tint = StatusAmber,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = message, color = StatusAmber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

fun batteryColor(percent: Int?): Color = when {
    percent == null -> StatusGrey
    percent < 15 -> StatusRed
    percent < 30 -> StatusAmber
    else -> StatusGreen
}

@Composable
fun BatteryPill(batteryPercent: Int?) {
    val color = batteryColor(batteryPercent)
    Row(
        modifier = Modifier
            .background(CardSurface, RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BatteryIcon(percent = batteryPercent ?: 0, color = color)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = batteryPercent?.let { stringResource(R.string.battery_value, it) }
                ?: stringResource(R.string.value_unavailable),
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BatteryIcon(percent: Int, color: Color) {
    Canvas(modifier = Modifier.size(width = 28.dp, height = 14.dp)) {
        val strokeWidth = 1.5.dp.toPx()
        val nubWidth = 2.5.dp.toPx()
        val bodyWidth = size.width - nubWidth
        val corner = CornerRadius(3.dp.toPx())

        drawRoundRect(
            color = color,
            size = Size(bodyWidth, size.height),
            cornerRadius = corner,
            style = Stroke(width = strokeWidth)
        )
        drawRect(
            color = color,
            topLeft = Offset(bodyWidth, size.height * 0.3f),
            size = Size(nubWidth, size.height * 0.4f)
        )
        val inset = strokeWidth * 2
        drawRoundRect(
            color = color,
            topLeft = Offset(inset, inset),
            size = Size((bodyWidth - inset * 2) * percent / 100f, size.height - inset * 2),
            cornerRadius = CornerRadius(1.5.dp.toPx())
        )
    }
}

@Composable
fun MotorPill(status: MotorStatus) {
    val (text, color, background) = when (status) {
        MotorStatus.ON -> Triple(stringResource(R.string.motor_on), StatusGreen, GreenTint)
        MotorStatus.OFF -> Triple(stringResource(R.string.motor_off), StatusGrey, CardSurface)
        MotorStatus.UNKNOWN -> Triple(stringResource(R.string.value_unavailable), StatusAmber, CardSurface)
    }
    val description = stringResource(R.string.motor_description)

    Row(
        modifier = Modifier
            .background(background, RoundedCornerShape(50))
            .border(1.dp, color, RoundedCornerShape(50))
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_power),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
