package com.zamer.fitcoach;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Re-creates all alarms after a reboot, app update, or time/timezone change. */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent intent) {
        Notifier.channels(c);
        Scheduler.scheduleAll(c);
    }
}
