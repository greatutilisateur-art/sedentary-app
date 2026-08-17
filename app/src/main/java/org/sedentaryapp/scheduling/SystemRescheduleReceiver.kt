package org.sedentaryapp.scheduling

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SystemRescheduleReceiver :
    BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {

        val supported =
            intent.action in
                setOf(
                    Intent.ACTION_BOOT_COMPLETED,
                    Intent.ACTION_TIME_CHANGED,
                    Intent.ACTION_TIMEZONE_CHANGED,
                    Intent.ACTION_MY_PACKAGE_REPLACED,
                    AlarmManager
                        .ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
                )

        if (!supported) {
            return
        }

        val pendingResult =
            goAsync()

        CoroutineScope(
            SupervisorJob() +
                Dispatchers.IO
        ).launch {
            try {
                ProposalAlarmScheduler(
                    context.applicationContext
                ).reconcile()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
