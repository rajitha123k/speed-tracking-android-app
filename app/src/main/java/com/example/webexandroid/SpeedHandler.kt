package com.example.webexandroid

object SpeedHandler {
    var listener: SpeedUpdateListener? = null

    fun updateSpeed(exceedLimit: Boolean) {
        listener?.onSpeedLimitUpdate(exceedLimit)
    }

    fun isActivityRunning(): Boolean {
        return listener?.isActivityRunning() ?: false
    }

    interface SpeedUpdateListener {
        fun onSpeedLimitUpdate(exceedLimit: Boolean)
        fun isActivityRunning(): Boolean
    }
}