package com.zamer.fitcoach;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int TEAL = 0xFF0F766E;
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final String[] TABS = {"Today", "Guide", "Progress", "Settings"};
    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
    private static final DateTimeFormatter LONG = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);

    private ScrollView scroll;
    private LinearLayout content;
    private final TextView[] tabViews = new TextView[TABS.length];
    private int currentTab = 0;

    // ------------------------------------------------------------------ lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Notifier.channels(this);
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        buildShell();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Scheduler.scheduleAll(this);
        show(currentTab);
    }

    // ------------------------------------------------------------------ shell

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF4F7F6);

        TextView header = tv("FitCoach   |   110 kg -> 90 kg", 18, true, Color.WHITE);
        header.setBackgroundColor(TEAL);
        header.setPadding(dp(16), dp(14), dp(16), dp(14));
        root.addView(header, new LinearLayout.LayoutParams(MATCH, WRAP));

        scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(12), dp(12), dp(24));
        scroll.addView(content, new ViewGroup.LayoutParams(MATCH, WRAP));
        root.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1f));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(Color.WHITE);
        for (int i = 0; i < TABS.length; i++) {
            final int tabIndex = i;
            TextView tab = tv(TABS[i], 14, false, 0xFF777777);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(0, dp(14), 0, dp(14));
            tab.setOnClickListener(v -> show(tabIndex));
            nav.addView(tab, new LinearLayout.LayoutParams(0, WRAP, 1f));
            tabViews[i] = tab;
        }
        root.addView(nav, new LinearLayout.LayoutParams(MATCH, WRAP));
        setContentView(root);
    }

    private void show(int tab) {
        currentTab = tab;
        content.removeAllViews();
        switch (tab) {
            case 0: showToday(); break;
            case 1: showGuide(); break;
            case 2: showProgress(); break;
            default: showSettings(); break;
        }
        for (int i = 0; i < tabViews.length; i++) {
            boolean on = i == tab;
            tabViews[i].setTextColor(on ? TEAL : 0xFF777777);
            tabViews[i].setTypeface(on ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
    }

    /** Rebuilds the current tab but keeps the scroll position. */
    private void refresh() {
        final int y = scroll.getScrollY();
        show(currentTab);
        scroll.post(() -> scroll.scrollTo(0, y));
    }

    // ------------------------------------------------------------------ small helpers

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView tv(String text, int sp, boolean bold, int color) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private TextView bodyText(String text) {
        TextView t = tv(text, 14, false, 0xFF374151);
        t.setPadding(0, dp(6), 0, 0);
        t.setLineSpacing(0f, 1.15f);
        return t;
    }

    private GradientDrawable rounded(int color) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(12));
        return g;
    }

    /** Adds a rounded card to the content area and returns it. */
    private LinearLayout card(int color) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(14), dp(12), dp(14), dp(12));
        l.setBackground(rounded(color));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP);
        lp.bottomMargin = dp(10);
        content.addView(l, lp);
        return l;
    }

    private void infoCard(String title, String body) {
        LinearLayout c = card(Color.WHITE);
        c.addView(tv(title, 17, true, TEAL));
        c.addView(bodyText(body));
    }

    private Button button(String label, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setOnClickListener(listener);
        return b;
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void safeStart(Intent i) {
        try {
            startActivity(i);
        } catch (Exception e) {
            toast("Please open your phone Settings > Apps > FitCoach");
        }
    }

    private static String fmt(int min) {
        int h = min / 60;
        int m = min % 60;
        int h12 = h % 12 == 0 ? 12 : h % 12;
        return String.format(Locale.US, "%d:%02d %s", h12, m, h >= 12 ? "PM" : "AM");
    }

    private static String lines(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append('\n');
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ TODAY

    private void showToday() {
        final LocalDate today = LocalDate.now();
        final int idx = Store.dayIndex(today);
        final String day = today.toString();

        LinearLayout head = card(Color.WHITE);
        if (idx < 0) {
            head.addView(tv("Plan starts on 20-10-2026", 18, true, TEAL));
            head.addView(bodyText("In " + (-idx) + " day(s). Reminders begin that morning at 5:30 AM. "
                    + "This is your daily schedule - long-press a row to change its time."));
        } else if (idx >= Store.TOTAL_DAYS) {
            head.addView(tv("Your 5-month plan is complete!", 18, true, TEAL));
            head.addView(bodyText("Open the Progress tab to see your final result."));
        } else {
            head.addView(tv("Day " + (idx + 1) + " of " + Store.TOTAL_DAYS, 18, true, TEAL));
            head.addView(bodyText(String.format(Locale.US,
                    "Target weight today: %.1f kg   |   Goal: %.0f kg by 20-03-2027",
                    Store.targetWeight(idx), Store.GOAL_WEIGHT)));
        }

        final List<Plan.Task> list = new ArrayList<>();
        for (Plan.Task t : Plan.TASKS) {
            boolean show = idx < 0 ? !t.weekly : Store.appliesOn(t, today);
            if (show) list.add(t);
        }
        Collections.sort(list, (a, b) ->
                Integer.compare(Store.minutesOf(this, a), Store.minutesOf(this, b)));

        int done = 0;
        for (Plan.Task t : list) {
            if (Store.isDone(this, t.id, day)) done++;
        }
        if (!list.isEmpty()) {
            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(list.size());
            pb.setProgress(done);
            LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(MATCH, WRAP);
            plp.topMargin = dp(8);
            head.addView(pb, plp);
            head.addView(tv(done + " of " + list.size() + " done today", 13, true, 0xFF444444));
        }

        // Pending penalties
        List<Integer> pens = Store.penalties(this);
        if (!pens.isEmpty()) {
            LinearLayout pc = card(0xFFFBE4E4);
            pc.addView(tv("Penalties to complete (" + pens.size() + ")", 17, true, 0xFFB91C1C));
            for (final int p : pens) {
                LinearLayout pr = new LinearLayout(this);
                pr.setOrientation(LinearLayout.HORIZONTAL);
                pr.setGravity(Gravity.CENTER_VERTICAL);
                TextView txt = tv(Plan.PENALTIES[p % Plan.PENALTIES.length], 14, false, 0xFF1F2937);
                pr.addView(txt, new LinearLayout.LayoutParams(0, WRAP, 1f));
                pr.addView(button("Done", v -> {
                    Store.completePenalty(this, p);
                    refresh();
                }));
                pc.addView(pr);
            }
            pc.addView(bodyText("Penalties come from missed tasks. Do them safely - stop if you feel pain or dizziness."));
        }

        // Task rows
        long nowMs = System.currentTimeMillis();
        for (final Plan.Task t : list) {
            final int min = Store.minutesOf(this, t);
            boolean isDone = Store.isDone(this, t.id, day);
            boolean isMissed = Store.isMissed(this, t.id, day);
            boolean overdue = idx >= 0 && Scheduler.dueMs(today, min) < nowMs;
            int color = isDone ? 0xFFE3F4E8 : isMissed ? 0xFFFBE4E4 : overdue ? 0xFFFFF1D6 : Color.WHITE;

            LinearLayout row = card(color);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView time = tv(fmt(min), 14, true, TEAL);
            row.addView(time, new LinearLayout.LayoutParams(dp(78), WRAP));

            LinearLayout mid = new LinearLayout(this);
            mid.setOrientation(LinearLayout.VERTICAL);
            TextView title = tv(t.title, 15, true, 0xFF1F2937);
            TextView detail = tv(t.detail, 12, false, 0xFF6B7280);
            detail.setPadding(0, dp(2), 0, 0);
            mid.addView(title);
            mid.addView(detail);
            row.addView(mid, new LinearLayout.LayoutParams(0, WRAP, 1f));

            CheckBox cb = new CheckBox(this);
            cb.setChecked(isDone);
            cb.setClickable(false);
            cb.setFocusable(false);
            row.addView(cb);

            row.setOnClickListener(v -> {
                if (idx < 0 || idx >= Store.TOTAL_DAYS) {
                    toast("Your plan runs from 20-10-2026 to 20-03-2027");
                    return;
                }
                Store.setDone(this, t.id, day, !Store.isDone(this, t.id, day));
                getSystemService(NotificationManager.class).cancel(t.id);
                Scheduler.schedule(this, t);
                refresh();
            });
            row.setOnLongClickListener(v -> {
                new TimePickerDialog(this, (view, h, m) -> {
                    Store.setMinutes(this, t.id, h * 60 + m);
                    Scheduler.schedule(this, t);
                    refresh();
                }, min / 60, min % 60, false).show();
                return true;
            });
        }

        content.addView(bodyText("Tap a row to mark it done (tap again to undo). Long-press a row to change its time."));
    }

    // ------------------------------------------------------------------ GUIDE

    private void showGuide() {
        infoCard("Your targets", lines(
                "Goal: 110 kg -> 90 kg between 20-10-2026 and 20-03-2027 (about 0.9 kg per week). "
                        + "This is on the faster side. It works only if you follow the plan about 85-90% of the time, "
                        + "so consistency matters more than perfection.",
                "",
                "Daily targets:",
                "- Calories: about 1,750-1,800 (an estimate; the weekly feedback adjusts it)",
                "- Protein: 100-120 g",
                "- Water: 3.5 litres (7 x 500 ml)",
                "- Walking: 8,000-10,000 steps",
                "- Sleep: 7 hours (10:30 PM to 5:30 AM)",
                "",
                "Talk to a doctor before starting if you have any medical condition, take regular medicines, "
                        + "or feel unwell during exercise."));

        infoCard("What to eat, how much, when", lines(
                "7:10 AM  Breakfast (~400 kcal): 2 idli + sambar + 1 boiled egg (or 100 g paneer / sprouts), "
                        + "OR 40 g oats in 200 ml toned milk with a fruit.",
                "",
                "10:30 AM  Snack 1 (~120 kcal): 1 fruit or 1 glass buttermilk.",
                "",
                "12:00 PM  Lunch (~600 kcal): 1 cup cooked rice OR 2 rotis, 1 cup dal, 1 cup sabzi, big salad, "
                        + "100 g chicken / fish / paneer / soya, small bowl of curd.",
                "",
                "3:30 PM  Snack 2 (~100 kcal): 30 g roasted chana or sprouts + unsweetened green tea.",
                "",
                "7:30 PM  Dinner (~450 kcal): 2 rotis (no ghee) OR 3/4 cup rice, 1 cup vegetables, dal OR "
                        + "100 g chicken / fish / egg curry, salad. Finish by 8:00 PM.",
                "",
                "Seeds add about 100 kcal in total (see Seeds below).",
                "",
                "Avoid: sugary drinks, sweets, fried food, bakery items, packaged snacks, extra rice, "
                        + "eating after 8:30 PM. Keep cooking oil to about 3 teaspoons for the whole day.",
                "Weekly treat: one small planned treat on Sunday. Planned treats prevent binges.",
                "Eat slowly, stop at about 80% full, use a smaller plate."));

        infoCard("Water", lines(
                "3.5 litres a day = 7 glasses of 500 ml. Reminders: 5:30 AM, 9:00 AM, 11:30 AM, 2:00 PM, "
                        + "4:30 PM, 7:00 PM and 8:30 PM.",
                "Sip over 10 minutes instead of gulping. Drink a little more on hot or heavy-sweat days. "
                        + "Pale-yellow urine means you are doing well.",
                "If a doctor has told you to limit fluids (kidney or heart problems), follow the doctor's limit."));

        infoCard("Skin care", lines(
                "Morning: face wash -> pat dry -> rose water -> light gel moisturiser -> sunscreen SPF 30+.",
                "Midday: rinse with plain water only (no soap), spritz rose water.",
                "Night: face wash -> pat dry -> rose water -> light moisturiser.",
                "",
                "Rose water: use it AFTER the face wash as a toner (cotton pad or spritz) and as a midday refresh. "
                        + "Test a drop on your jaw first. Keep the bottle in a cool place or the fridge.",
                "Face wash: only 2 times a day. More than that dries the skin and makes it oilier.",
                "Basics: sunscreen every morning (reapply after 3-4 hours outdoors), never pick pimples, "
                        + "change your pillowcase weekly, wipe your phone screen, sleep 7 hours.",
                "See a dermatologist if acne is painful or leaves marks."));

        infoCard("Hair care", lines(
                "Wash days: Monday, Thursday and Saturday (during Bath 1 or Bath 2). Use a mild shampoo on the scalp, "
                        + "massage with fingertips for 1 minute, rinse well with lukewarm or cool water. "
                        + "Conditioner only on the lengths, not the scalp.",
                "Oil massage: Sunday night, 5-10 minutes of gentle fingertip massage with coconut or any light oil you like, "
                        + "and wash it off on Monday morning (leaving oil for days causes dirt and dandruff).",
                "Daily: pat dry with a soft towel, comb gently with a wide-tooth comb (start from the ends), "
                        + "avoid tight hairstyles and hot hair dryers. Clean your comb weekly and change the pillowcase weekly.",
                "Dandruff: use an anti-dandruff shampoo (zinc pyrithione or ketoconazole) 2 times a week for 3-4 weeks.",
                "For hair from inside: enough protein (your meals already include it), iron-rich foods (dal, greens), "
                        + "water and 7 hours of sleep. Fast weight loss can cause temporary hair shedding - "
                        + "that is another reason to keep the loss near 1 kg a week.",
                "See a doctor if you get patchy hair loss, very itchy or painful scalp, or heavy shedding for more than 2-3 months."));

        infoCard("Seeds", lines(
                "Seeds #1 (7:25 AM): 1 tbsp chia + flax, soaked overnight - omega-3 fats help skin moisture "
                        + "and keep you full.",
                "Seeds #2 (3:35 PM): 1 tbsp pumpkin + sunflower seeds - zinc and vitamin E support skin.",
                "Stick to these amounts; seeds are calorie-dense. Honestly, seeds give a small benefit - "
                        + "water, sleep, sunscreen and a clean diet matter more for skin."));

        infoCard("Brushing, baths and sleep", lines(
                "Brushing: 2 times a day (5:40 AM and 10:00 PM), 2 minutes each, soft brush + fluoride toothpaste, "
                        + "clean your tongue. Rinse your mouth with water after meals. Nothing to eat after the night brush.",
                "Baths: 2 times a day - after the morning workout (6:40 AM) and in the evening (6:15 PM), lukewarm water.",
                "Sleep: lights out 10:30 PM, wake 5:30 AM = 7 hours. Charge your phone across the room. "
                        + "Keep the same times on weekends."));

        infoCard("Your day (time budget)", lines(
                "Exercise 45 min: 5:50 - 6:35 AM (then bath, hair care and skin care until 7:10 AM)",
                "Study 8 h: 7:45-11:45 AM (4 h), 12:30-3:30 PM (3 h), 9:00-10:00 PM (1 h)",
                "Work 2.5 h: 3:45 - 6:15 PM",
                "Enjoy 1 h: 6:30 - 7:30 PM",
                "Family 1 h: 8:00 - 9:00 PM",
                "",
                "Every time can be changed: long-press a task on the Today tab."));

        infoCard("Exercise plan", lines(
                "Weeks 1-4 (build the habit): 30 min brisk walk (you can talk but not sing) + 2 rounds of: "
                        + "10 chair squats, 8 wall push-ups, 20 s plank, 10 glute bridges. Finish with 5 min stretching.",
                "",
                "Weeks 5-12: 35 min walk with 1 min fast / 2 min normal intervals + 3 rounds of: "
                        + "12 squats, 10 knee push-ups, 30 s plank, 12 glute bridges.",
                "",
                "Weeks 13-22: 35-40 min walk/jog (1 min jog / 2 min walk) + 3 rounds of: "
                        + "15 squats, 10 incline or full push-ups, 40 s plank, 8 lunges per leg.",
                "",
                "Saturday: easy 30 min walk + stretching. Sunday: one longer walk or cycle ride (60 min).",
                "If your knees hurt, choose low-impact options. Stop and get medical advice for chest pain, "
                        + "dizziness or sharp joint pain."));

        infoCard("Reminders, penalties, weekly feedback", lines(
                "Reminders: every task alerts at its time and repeats every few minutes (set the gap in Settings) "
                        + "until you tap Done. When the task's window runs out, it counts as MISSED.",
                "",
                "Penalties: each missed task adds a penalty (squats, push-ups, plank, extra walk). "
                        + "They show in red on the Today tab until you do them and tap Done. "
                        + "Finishing a task late stops the repeats but the penalty stays. "
                        + "Penalties are never about skipping food.",
                "",
                "Weekly feedback: every 7th day (Tuesday) at 5:45 AM, weigh yourself and save it in the Progress tab. "
                        + "The app compares your weight and routine completion with the plan and tells you what to change."));
    }

    // ------------------------------------------------------------------ PROGRESS

    private void showProgress() {
        final LocalDate today = LocalDate.now();
        final int idx = Store.dayIndex(today);
        double cur = Store.latestWeight(this);

        LinearLayout g = card(Color.WHITE);
        g.addView(tv("Your goal", 17, true, TEAL));
        g.addView(bodyText(String.format(Locale.US, "Start: %.0f kg  ->  Goal: %.0f kg by 20-03-2027",
                Store.START_WEIGHT, Store.GOAL_WEIGHT)));
        g.addView(bodyText(String.format(Locale.US,
                "Current: %.1f kg   |   Lost so far: %.1f kg   |   To go: %.1f kg",
                cur, Store.START_WEIGHT - cur, Math.max(0.0, cur - Store.GOAL_WEIGHT))));
        if (idx >= 0 && idx < Store.TOTAL_DAYS) {
            double target = Store.targetWeight(idx);
            double diff = cur - target;
            String status = diff <= 0
                    ? String.format(Locale.US, "You are %.1f kg AHEAD of the plan.", -diff)
                    : String.format(Locale.US, "You are %.1f kg behind the plan.", diff);
            g.addView(bodyText(String.format(Locale.US, "Target for today: %.1f kg. ", target) + status));
        }

        LinearLayout w = card(Color.WHITE);
        w.addView(tv("Log today's weight", 17, true, TEAL));
        final EditText et = new EditText(this);
        et.setHint("Weight in kg, e.g. 108.4");
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        w.addView(et, new LinearLayout.LayoutParams(MATCH, WRAP));
        w.addView(button("Save today's weight", v -> {
            String s = et.getText().toString().trim();
            double kg;
            try {
                kg = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                toast("Enter a number like 108.4");
                return;
            }
            if (kg < 30 || kg > 300) {
                toast("That does not look right - enter your weight in kg");
                return;
            }
            Store.saveWeight(this, today, kg);
            Plan.Task wt = Plan.byId(Plan.WEEKLY_ID);
            if (wt != null && Store.appliesOn(wt, today)) {
                Store.setDone(this, wt.id, today.toString(), true);
                getSystemService(NotificationManager.class).cancel(wt.id);
                Scheduler.schedule(this, wt);
            }
            toast("Saved " + kg + " kg");
            refresh();
        }));
        w.addView(bodyText("Weigh on check-in days (every Tuesday): after the toilet, before food or water, same scale."));

        int weeks = Math.min(idx / 7, 21);
        if (idx < 7) {
            infoCard("Weekly feedback",
                    "Your first weekly feedback arrives on " + Store.dateOf(7).format(LONG)
                            + ". Log your weight today and on that day.");
        } else {
            for (int wk = weeks; wk >= 1; wk--) {
                weekCard(wk);
            }
        }
    }

    private void weekCard(int wk) {
        int a = (wk - 1) * 7;
        int b = wk * 7;                       // days a .. b-1; weigh-in on day b
        int dailyCount = Plan.dailyCount();
        int done = 0;
        int missed = 0;
        Map<Integer, Integer> missCount = new HashMap<>();
        for (int d = a; d < b; d++) {
            String key = Store.dateOf(d).toString();
            for (Plan.Task t : Plan.TASKS) {
                if (t.weekly) continue;
                if (Store.isDone(this, t.id, key)) done++;
                if (Store.isMissed(this, t.id, key)) {
                    missed++;
                    missCount.merge(t.id, 1, Integer::sum);
                }
            }
        }
        int pct = Math.round(done * 100f / (7 * dailyCount));

        List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(missCount.entrySet());
        Collections.sort(entries, (x, y) -> y.getValue() - x.getValue());
        StringBuilder top = new StringBuilder();
        for (int i = 0; i < Math.min(3, entries.size()); i++) {
            Plan.Task mt = Plan.byId(entries.get(i).getKey());
            if (mt == null) continue;
            if (top.length() > 0) top.append("; ");
            top.append(mt.title).append(" (").append(entries.get(i).getValue()).append("x)");
        }

        Double endExact = Store.weightOn(this, Store.dateOf(b));
        double startW = Store.weightAtOrBefore(this, Store.dateOf(a));
        double pace = (Store.START_WEIGHT - Store.GOAL_WEIGHT) * 7.0 / (Store.TOTAL_DAYS - 1);

        LinearLayout c = card(Color.WHITE);
        c.addView(tv("Week " + wk + "   (" + Store.dateOf(a).format(SHORT) + " - "
                + Store.dateOf(b - 1).format(SHORT) + ")", 17, true, TEAL));
        c.addView(bodyText("Routine completion: " + pct + "%   |   Missed tasks: " + missed));

        if (endExact == null) {
            c.addView(bodyText("Weigh-in for " + Store.dateOf(b).format(SHORT)
                    + " not logged yet. Log it in the box above to get your weight verdict."));
            c.addView(bodyText("Coach: " + verdict(0, pct, top.toString(), false)));
            return;
        }
        double lost = startW - endExact;
        c.addView(bodyText(String.format(Locale.US,
                "Weight: %.1f -> %.1f kg  (%s%.1f kg; plan pace -%.1f kg per week)",
                startW, endExact, lost >= 0 ? "-" : "+", Math.abs(lost), pace)));
        c.addView(bodyText("Coach: " + verdict(lost, pct, top.toString(), true)));
    }

    private static String verdict(double lost, int pct, String topMiss, boolean haveWeight) {
        String misses = topMiss.isEmpty() ? "" : " Most missed: " + topMiss + ".";
        if (!haveWeight) {
            return pct >= 85
                    ? "Great consistency this week. Log your weight to see the result."
                    : "Consistency was " + pct + "%." + misses + " Aim for 85% or more.";
        }
        if (lost < 0) {
            return "Your weight went up. Check portion sizes (rice/roti), hidden sugar and oil, and weigh "
                    + "under the same conditions. Water and salt can swing weight by 1 kg - stay with the plan "
                    + "for one more week before changing anything." + misses;
        }
        if (pct < 70) {
            return "Consistency is the main problem (" + pct + "%)." + misses
                    + " Fix these first - the plan only works when it is followed. "
                    + "Long-press a task to move it to a time you can actually keep.";
        }
        if (lost > 1.5) {
            return String.format(Locale.US, "You lost %.1f kg - faster than planned. ", lost)
                    + "Eat about 150 kcal more (a fruit + curd) and keep protein high to protect muscle, "
                    + "energy and skin.";
        }
        if (lost >= 0.7) {
            return pct >= 85
                    ? "On track - great week. Keep the same plan."
                    : "Good loss, but completion was only " + pct + "%." + misses + " Tighten these up.";
        }
        if (pct >= 85) {
            return "You followed the plan but the loss is slow. Next week: cut about 150 kcal "
                    + "(a quarter less rice/roti, skip the evening fruit) and add 10 minutes to your walk. "
                    + "One week can mislead - judge the 2-week trend.";
        }
        return "Loss is slow and completion was " + pct + "%." + misses
                + " Raise completion above 85% before cutting more calories.";
    }

    // ------------------------------------------------------------------ SETTINGS

    private void showSettings() {
        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager am = getSystemService(AlarmManager.class);
            if (!am.canScheduleExactAlarms()) {
                LinearLayout warn = card(0xFFFFF1D6);
                warn.addView(tv("Exact alarms are OFF - reminders may be late", 15, true, 0xFF92400E));
                warn.addView(button("Allow exact alarms", v -> safeStart(
                        new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:" + getPackageName())))));
            }
        }

        // Alert mode
        LinearLayout m = card(Color.WHITE);
        m.addView(tv("Alert mode", 17, true, TEAL));
        final String[] modeValues = {"both", "sound", "vibrate"};
        String[] modeLabels = {
                "Sound + vibration (recommended)",
                "Sound only",
                "Vibration only (for institute / silent)"};
        RadioGroup rg = new RadioGroup(this);
        for (int i = 0; i < modeValues.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(modeLabels[i]);
            rb.setId(View.generateViewId());
            rg.addView(rb);
            if (modeValues[i].equals(Store.mode(this))) rb.setChecked(true);
        }
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            int pos = group.indexOfChild(group.findViewById(checkedId));
            if (pos >= 0) {
                Store.setMode(this, modeValues[pos]);
                toast("Alert mode saved");
            }
        });
        m.addView(rg);

        // Repeat gap
        LinearLayout r = card(Color.WHITE);
        r.addView(tv("Repeat reminder until done", 17, true, TEAL));
        final int[] repValues = {2, 5, 10};
        String[] repLabels = {"Every 2 minutes", "Every 5 minutes", "Every 10 minutes"};
        RadioGroup rg2 = new RadioGroup(this);
        for (int i = 0; i < repValues.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(repLabels[i]);
            rb.setId(View.generateViewId());
            rg2.addView(rb);
            if (repValues[i] == Store.repeatMin(this)) rb.setChecked(true);
        }
        rg2.setOnCheckedChangeListener((group, checkedId) -> {
            int pos = group.indexOfChild(group.findViewById(checkedId));
            if (pos >= 0) {
                Store.setRepeatMin(this, repValues[pos]);
                toast("Repeat gap saved");
            }
        });
        r.addView(rg2);

        // Tools
        LinearLayout tools = card(Color.WHITE);
        tools.addView(tv("Test and fix reminders", 17, true, TEAL));
        tools.addView(button("Send a test alert now", v -> Notifier.test(this)));
        tools.addView(button("Open notification settings", v -> safeStart(
                new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()))));
        tools.addView(button("Battery: allow background running", v -> safeStart(
                new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))));

        infoCard("Phone on silent at the institute?", lines(
                "Choose 'Vibration only' (or 'Sound + vibration'). FitCoach vibrates with alarm priority, "
                        + "which normally works even in silent mode. Tap 'Send a test alert' with your phone on "
                        + "silent to check it on your phone.",
                "",
                "If Do Not Disturb is on, allow FitCoach in the DND exceptions of your phone.",
                "",
                "For reliable timing: Battery -> FitCoach -> Unrestricted. On Xiaomi / Oppo / Vivo / Realme / "
                        + "Samsung, also allow 'Autostart' and 'Run in background' for FitCoach, "
                        + "and do not swipe-kill the app from recent apps with a 'clean' button."));
    }
}
