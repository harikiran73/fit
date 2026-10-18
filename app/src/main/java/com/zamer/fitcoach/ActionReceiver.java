package com.zamer.fitcoach;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.time.LocalDate;

/** Handles the "Done" button on a notification. */
public class ActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        Plan.Task t = Plan.byId(id);
        if (t == null) return;

        Store.setDone(c, id, LocalDate.now().toString(), true);
        c.getSystemService(NotificationManager.class).cancel(id);
        Scheduler.schedule(c, t);
        Toast.makeText(c, "Done: " + t.title, Toast.LENGTH_SHORT).show();
    }
}
