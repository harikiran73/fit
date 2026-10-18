package com.zamer.fitcoach;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;

/** Notifications (sound / vibration / both) and the extra direct vibration. */
final class Notifier {

    static final String CH_BOTH = "fc_both_v1";
    static final String CH_SOUND = "fc_sound_v1";
    static final String CH_VIB = "fc_vibrate_v1";
    static final long[] PATTERN = {0, 600, 250, 600, 250, 900};

    private Notifier() {}

    static void channels(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        AudioAttributes aa = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        NotificationChannel both = new NotificationChannel(CH_BOTH,
                "Reminders - sound + vibration", NotificationManager.IMPORTANCE_HIGH);
        both.setSound(sound, aa);
        both.enableVibration(true);
        both.setVibrationPattern(PATTERN);

        NotificationChannel snd = new NotificationChannel(CH_SOUND,
                "Reminders - sound only", NotificationManager.IMPORTANCE_HIGH);
        snd.setSound(sound, aa);
        snd.enableVibration(false);

        NotificationChannel vib = new NotificationChannel(CH_VIB,
                "Reminders - vibration only", NotificationManager.IMPORTANCE_HIGH);
        vib.setSound(null, null);
        vib.enableVibration(true);
        vib.setVibrationPattern(PATTERN);

        nm.createNotificationChannel(both);
        nm.createNotificationChannel(snd);
        nm.createNotificationChannel(vib);
    }

    static String channelFor(Context c) {
        String m = Store.mode(c);
        if ("sound".equals(m)) return CH_SOUND;
        if ("vibrate".equals(m)) return CH_VIB;
        return CH_BOTH;
    }

    private static PendingIntent openApp(Context c, int code) {
        Intent i = new Intent(c, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(c, code, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void remind(Context c, Plan.Task t, int rep, int minsLeft) {
        channels(c);
        String title;
        String text;
        if (rep == 0) {
            title = "⏰ " + t.title;
            text = "Time for it. Tap Done when finished.";
        } else if (rep < 3) {
            title = "Still pending: " + t.title;
            text = "Do it now and tap Done. " + minsLeft + " min left before it counts as missed.";
        } else {
            title = "⚠ Don't skip: " + t.title;
            text = "Only " + minsLeft + " min left - missing it adds a penalty!";
        }

        Intent doneIntent = new Intent(c, ActionReceiver.class)
                .setAction("com.zamer.fitcoach.DONE")
                .putExtra("id", t.id);
        PendingIntent donePi = PendingIntent.getBroadcast(c, 2000 + t.id, doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Action done = new Notification.Action.Builder(
                Icon.createWithResource(c, R.drawable.ic_stat), "Done", donePi).build();

        Notification n = new Notification.Builder(c, channelFor(c))
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text + "\n" + t.detail))
                .setCategory(Notification.CATEGORY_REMINDER)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(openApp(c, 1000 + t.id))
                .setAutoCancel(false)
                .setColor(0xFF0F766E)
                .addAction(done)
                .build();

        c.getSystemService(NotificationManager.class).notify(t.id, n);
        buzz(c);
    }

    static void missed(Context c, Plan.Task t) {
        channels(c);
        Notification n = new Notification.Builder(c, channelFor(c))
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle("❌ Missed: " + t.title)
                .setContentText("A penalty was added. Open FitCoach to see it.")
                .setCategory(Notification.CATEGORY_REMINDER)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(openApp(c, 1000 + t.id))
                .setAutoCancel(true)
                .setColor(0xFFB91C1C)
                .build();
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        nm.cancel(t.id);
        nm.notify(5000 + t.id, n);
        buzz(c);
    }

    static void test(Context c) {
        channels(c);
        String m = Store.mode(c);
        String label = "sound".equals(m) ? "sound only"
                : "vibrate".equals(m) ? "vibration only" : "sound + vibration";
        Notification n = new Notification.Builder(c, channelFor(c))
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle("FitCoach test alert")
                .setContentText("Alert mode: " + label)
                .setContentIntent(openApp(c, 999))
                .setAutoCancel(true)
                .setColor(0xFF0F766E)
                .build();
        c.getSystemService(NotificationManager.class).notify(9999, n);
        buzz(c);
    }

    /** Extra vibration with alarm priority so it still works when the phone is on silent. */
    @SuppressWarnings("deprecation")
    static void buzz(Context c) {
        if ("sound".equals(Store.mode(c))) return;
        Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null || !v.hasVibrator()) return;
        AudioAttributes aa = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        v.vibrate(VibrationEffect.createWaveform(PATTERN, -1), aa);
    }
}
