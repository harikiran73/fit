package com.zamer.fitcoach;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** All saved data: done/missed flags, penalties, weights and settings. */
final class Store {

    // ---- Plan constants (change here if you want a different plan) ----
    static final String START_DATE = "2026-10-20";
    static final double START_WEIGHT = 110.0;
    static final double GOAL_WEIGHT = 90.0;
    static final int TOTAL_DAYS = 152;   // 20-10-2026 .. 20-03-2027 inclusive

    private Store() {}

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("fitcoach", Context.MODE_PRIVATE);
    }

    private static Set<String> set(Context c, String key) {
        return new HashSet<>(sp(c).getStringSet(key, new HashSet<String>()));
    }

    // ---- Dates ----
    static LocalDate startDate() {
        return LocalDate.parse(START_DATE);
    }

    static int dayIndex(LocalDate d) {
        return (int) ChronoUnit.DAYS.between(startDate(), d);
    }

    static LocalDate dateOf(int idx) {
        return startDate().plusDays(idx);
    }

    static double targetWeight(int idx) {
        int i = Math.max(0, Math.min(idx, TOTAL_DAYS - 1));
        return START_WEIGHT - (START_WEIGHT - GOAL_WEIGHT) * i / (TOTAL_DAYS - 1);
    }

    static boolean appliesOn(Plan.Task t, LocalDate d) {
        int idx = dayIndex(d);
        if (idx < 0 || idx >= TOTAL_DAYS) return false;
        if (t.weekly) return idx % 7 == 0 || idx == TOTAL_DAYS - 1;
        return true;
    }

    // ---- Task times ----
    static int minutesOf(Context c, Plan.Task t) {
        return sp(c).getInt("t_" + t.id, t.minutes);
    }

    static void setMinutes(Context c, int id, int minutes) {
        sp(c).edit().putInt("t_" + id, minutes).apply();
    }

    // ---- Done / missed ----
    static boolean isDone(Context c, int id, String day) {
        return set(c, "done_" + day).contains(String.valueOf(id));
    }

    static void setDone(Context c, int id, String day, boolean done) {
        Set<String> s = set(c, "done_" + day);
        if (done) s.add(String.valueOf(id)); else s.remove(String.valueOf(id));
        sp(c).edit().putStringSet("done_" + day, s).apply();
    }

    static boolean isMissed(Context c, int id, String day) {
        return set(c, "missed_" + day).contains(String.valueOf(id));
    }

    /** Marks a task as missed (once) and adds a penalty. Returns true if newly missed. */
    static boolean markMissed(Context c, int id, String day) {
        if (isDone(c, id, day) || isMissed(c, id, day)) return false;
        Set<String> s = set(c, "missed_" + day);
        s.add(String.valueOf(id));
        sp(c).edit().putStringSet("missed_" + day, s).apply();
        addPenalty(c);
        return true;
    }

    // ---- Penalties ----
    static List<Integer> penalties(Context c) {
        List<Integer> out = new ArrayList<>();
        String s = sp(c).getString("pen_list", "");
        if (!s.isEmpty()) {
            for (String p : s.split(",")) out.add(Integer.parseInt(p));
        }
        return out;
    }

    private static void savePenalties(Context c, List<Integer> l) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(l.get(i));
        }
        sp(c).edit().putString("pen_list", sb.toString()).apply();
    }

    private static void addPenalty(Context c) {
        List<Integer> l = penalties(c);
        int seq = sp(c).getInt("pen_seq", 0);
        l.add(seq);
        sp(c).edit().putInt("pen_seq", seq + 1).apply();
        savePenalties(c, l);
    }

    static void completePenalty(Context c, int seqId) {
        List<Integer> l = penalties(c);
        l.remove(Integer.valueOf(seqId));
        savePenalties(c, l);
    }

    // ---- Weights ----
    static void saveWeight(Context c, LocalDate d, double kg) {
        Set<String> s = set(c, "wlog");
        Iterator<String> it = s.iterator();
        while (it.hasNext()) {
            if (it.next().startsWith(d.toString() + "|")) it.remove();
        }
        s.add(d.toString() + "|" + kg);
        sp(c).edit().putStringSet("wlog", s).apply();
    }

    /** Weight logged exactly on this date, or null. */
    static Double weightOn(Context c, LocalDate d) {
        for (String e : set(c, "wlog")) {
            String[] p = e.split("\\|");
            if (p[0].equals(d.toString())) return Double.valueOf(p[1]);
        }
        return null;
    }

    /** Latest weight logged on or before this date (start weight if none). */
    static double weightAtOrBefore(Context c, LocalDate d) {
        LocalDate best = null;
        double bw = START_WEIGHT;
        for (String e : set(c, "wlog")) {
            String[] p = e.split("\\|");
            LocalDate ed = LocalDate.parse(p[0]);
            if (!ed.isAfter(d) && (best == null || ed.isAfter(best))) {
                best = ed;
                bw = Double.parseDouble(p[1]);
            }
        }
        return bw;
    }

    static double latestWeight(Context c) {
        return weightAtOrBefore(c, LocalDate.now());
    }

    // ---- Settings ----
    static String mode(Context c) {
        return sp(c).getString("mode", "both");   // both | sound | vibrate
    }

    static void setMode(Context c, String mode) {
        sp(c).edit().putString("mode", mode).apply();
    }

    static int repeatMin(Context c) {
        return sp(c).getInt("repeat_min", 5);
    }

    static void setRepeatMin(Context c, int minutes) {
        sp(c).edit().putInt("repeat_min", minutes).apply();
    }
}
