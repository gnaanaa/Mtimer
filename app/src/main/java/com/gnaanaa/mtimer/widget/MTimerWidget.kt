package com.gnaanaa.mtimer.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.gnaanaa.mtimer.data.sync.getWeeklyMindfulnessMinutes

class MTimerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val minutes = try {
            getWeeklyMindfulnessMinutes(context)
        } catch (e: Exception) {
            0
        }

        provideContent {
            MTimerWidgetContent(minutes)
        }
    }
}

@Composable
fun MTimerWidgetContent(minutes: Int) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MTimer",
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                fontSize = 12.sp
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "${minutes}m",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "this week",
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                fontSize = 10.sp
            )
        )
    }
}
