package com.zamer.fitcoach;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.LocalDate;
import java.time.ZoneId;

/** Sets exactly one pending alarm per task: either its next due time or the next repeat. */
final class Scheduler {

    static final String ACTION_ALARM = "com.zamer.fitcoach.ALARM";

    private Scheduler() {}

    static final class Next {
        final long at;
        final String day;
        final int rep;

        Next(long at, String day, int rep) {
            this.at = at;
            this.day = day;
            this.rep = rep;
        }
    }

    static long dueMs(LocalDate d, int minutes) {
        return d.atStartOfDay(ZoneId.systemDefault()).plusMinutes(minutes)
                .toInstant().toEpochMilli();
    }

    /** Works out when this task should ring next. */
    static Next next(Context c, Plan.Task t) {
        long now = System.currentTimeMillis();
        LocalDate today = LocalDate.now();
        int min = Store.minutesOf(c, t);
        for (int i = 0; i < 200; i++) {
            LocalDate d = today.plusDays(i);
            if (!Store.appliesOn(t, d)) continue;
            String day = d.toString();
            long due = dueMs(d, min);
            if (due > now) {
                if (i == 0 && Store.isDone(c, t.id, day)) continue;
                return new Next(due, day, 0);
            }
            if (i == 0) {
                // Due time already passed today: keep repeating while inside the window.
                long expiry = due + t.windowMin * 60000L;
                if (now < expiry && !Store.isDone(c, t.id, day) && !Store.isMissed(c, t.id, day)) {
                    long at = Math.min(now + Store.repeatMin(c) * 60000L, expiry + 1000L);
                    return new Next(at, day, 1);
                }
            }
        }
        return null;
    }

    static void schedule(Context c, Plan.Task t) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        Next n = next(c, t);
        if (n == null) {
            am.cancel(pending(c, t.id, "", 0));
            return;
        }
        setAlarm(am, n.at, pending(c, t.id, n.day, n.rep));
    }

    static void scheduleAll(Context c) {
        for (Plan.Task t : Plan.TASKS) schedule(c, t);
    }

    static void scheduleRepeat(Context c, Plan.Task t, String day, int rep, long expiry) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        long at = Math.min(System.currentTimeMillis() + Store.repeatMin(c) * 60000L, expiry + 1000L);
        setAlarm(am, at, pending(c, t.id, day, rep));
    }

    private static PendingIntent pending(Context c, int id, String day, int rep) {
        Intent i = new Intent(c, AlarmReceiver.class).setAction(ACTION_ALARM);
        i.putExtra("id", id);
        i.putExtra("day", day);
        i.putExtra("rep", rep);
        return PendingIntent.getBroadcast(c, id, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void setAlarm(AlarmManager am, long at, PendingIntent pi) {
        try {
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            }
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        }
    }
}
