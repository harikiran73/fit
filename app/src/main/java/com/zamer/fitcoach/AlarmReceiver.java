package com.zamer.fitcoach;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.time.LocalDate;

/** Fires when a task is due (or when a repeat is due). Keeps nagging until the task is done. */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        Plan.Task t = Plan.byId(id);
        if (t == null) return;

        String today = LocalDate.now().toString();
        String day = intent.getStringExtra("day");
        int rep = intent.getIntExtra("rep", 0);
        if (day == null || day.isEmpty()) day = today;

        // Alarm arrived late from an older day: count it as missed and move on.
        if (!day.equals(today)) {
            Store.markMissed(c, id, day);
            Scheduler.schedule(c, t);
            return;
        }

        // Already done: just plan the next occurrence.
        if (Store.isDone(c, id, day)) {
            Scheduler.schedule(c, t);
            return;
        }

        long due = Scheduler.dueMs(LocalDate.parse(day), Store.minutesOf(c, t));
        long expiry = due + t.windowMin * 60000L;
        long now = System.currentTimeMillis();

        // Window is over: missed -> penalty.
        if (now >= expiry) {
            if (Store.markMissed(c, id, day)) {
                Notifier.missed(c, t);
            }
            Scheduler.schedule(c, t);
            return;
        }

        int minsLeft = (int) Math.max(1, (expiry - now) / 60000L);
        Notifier.remind(c, t, rep, minsLeft);
        Scheduler.scheduleRepeat(c, t, day, rep + 1, expiry);
    }
}
