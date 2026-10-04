package com.floodsense.app

import android.app.Application
import com.floodsense.app.alerts.CitizenPushRegistrar
import com.floodsense.app.data.FieldLogCatalog
import com.floodsense.app.data.RiskSettings

class FloodSenseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FieldLogCatalog.load(this)
        RiskSettings.load(this)
        CitizenPushRegistrar.start(this)
    }
}
