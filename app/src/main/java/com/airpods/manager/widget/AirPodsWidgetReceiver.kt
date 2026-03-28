package com.airpods.manager.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class AirPodsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = AirPodsWidget()
}
