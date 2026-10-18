package com.zamer.fitcoach;

import java.util.ArrayList;
import java.util.List;

/** Fixed content of the plan: the daily schedule and the penalty list. */
final class Plan {

    static final class Task {
        final int id;
        final int minutes;       // default time, minutes after midnight
        final String title;
        final String detail;
        final String category;
        final int windowMin;     // minutes after the due time before it counts as MISSED
        final boolean weekly;    // only on check-in days (every 7th day)

        Task(int id, String hhmm, String title, String detail, String category,
             int windowMin, boolean weekly) {
            this.id = id;
            this.minutes = Integer.parseInt(hhmm.substring(0, 2)) * 60
                    + Integer.parseInt(hhmm.substring(3, 5));
            this.title = title;
            this.detail = detail;
            this.category = category;
            this.windowMin = windowMin;
            this.weekly = weekly;
        }
    }

    static final List<Task> TASKS = new ArrayList<>();
    static int WEEKLY_ID = -1;
    private static int nextId = 1;

    static final String[] PENALTIES = {
            "20 chair squats",
            "2 x 10 wall or knee push-ups",
            "1-minute plank (or 3 x 20 seconds)",
            "10 minutes of extra brisk walking",
            "20 glute bridges",
            "15 step-ups on each leg (sturdy step)"
    };

    private static void add(String hhmm, String cat, int win, String title, String detail) {
        TASKS.add(new Task(nextId++, hhmm, title, detail, cat, win, false));
    }

    static {
        add("05:30", "WAKE", 30, "Wake up + drink 500 ml water",
                "Out of bed right away - no snooze, no phone in bed. Drink 500 ml plain or lukewarm water (water #1 of 7).");
        add("05:40", "HYGIENE", 60, "Brush teeth (2 min)",
                "Soft brush, fluoride toothpaste, 2 full minutes: outer, inner, chewing surfaces and tongue. Brush 2 times a day (morning + night).");

        WEEKLY_ID = nextId;
        TASKS.add(new Task(nextId++, "05:45", "Weekly check-in: weigh yourself",
                "Same scale, after the toilet, before food or water, light clothes. Save it in the Progress tab to get your weekly feedback.",
                "PROGRESS", 600, true));

        add("05:50", "FITNESS", 60, "Start exercise (45 min)",
                "Tap Done when you START. 5 min warm-up, 35 min main workout, 5 min stretching. This month's routine is in the Guide tab. Stop if you feel chest pain, dizziness or sharp joint pain.");
        add("06:40", "HYGIENE", 60, "Bath 1 (after workout)",
                "Lukewarm water, 5-10 minutes. Wash neck, back, underarms and feet well. Dry fully, especially skin folds.");
        add("06:50", "HAIR", 60, "Hair care (after bath)",
                "Pat hair dry with a soft towel (do not rub hard). Comb gently with a wide-tooth comb. Shampoo only on wash days (Mon / Thu / Sat); Sunday night = oil massage, wash it off Monday morning. See the Guide tab.");
        add("06:55", "SKIN", 60, "Morning skin care",
                "1) Face wash (gentle, 30-40 sec, lukewarm water)  2) Pat dry  3) Rose water on a cotton pad or as a spritz  4) Light gel moisturiser  5) Sunscreen SPF 30+ before leaving home.");
        add("07:10", "MEAL", 60, "Breakfast (~400 kcal)",
                "2 idli + sambar + 1 boiled egg (or 100 g paneer / sprouts), OR 40 g oats cooked in 200 ml toned milk with a fruit. Tea/coffee without sugar.");
        add("07:25", "SEEDS", 90, "Seeds #1: chia + flax (1 tbsp)",
                "Soaked overnight in water (ground flax works best). Mix into curd or oats. Healthy fats support skin moisture. Do not exceed 1 tbsp.");
        add("07:45", "STUDY", 30, "Start Study block 1 (4 h)",
                "Tap Done when you SIT DOWN to study. 50 min study + 10 min break. Phone on silent/vibrate, face down. Block ends 11:45 AM.");
        add("09:00", "WATER", 45, "Water 500 ml (#2 of 7)",
                "Sip over 10 minutes, do not gulp.");
        add("10:30", "MEAL", 60, "Snack 1 (~120 kcal)",
                "1 fruit (apple, guava, papaya, orange) OR 1 glass buttermilk. No biscuits or fried snacks.");
        add("11:30", "WATER", 45, "Water 500 ml (#3 of 7)",
                "Sip over 10 minutes.");
        add("12:00", "MEAL", 60, "Lunch (~600 kcal)",
                "1 cup cooked rice OR 2 rotis, 1 cup dal, 1 cup vegetable curry, big salad, 100 g chicken / fish / paneer / soya chunks, 1 small bowl curd. Eat slowly (20 min).");
        add("12:30", "STUDY", 30, "Start Study block 2 (3 h)",
                "Tap Done when you START. Block ends 3:30 PM. 10 min break every 50 min. A short walk after lunch helps digestion.");
        add("14:00", "WATER", 45, "Water 500 ml (#4 of 7)",
                "Sip over 10 minutes.");
        add("14:30", "SKIN", 60, "Midday face refresh",
                "Rinse face with plain water (NO soap), pat dry, spritz rose water. Washing with soap more than twice a day dries and irritates the skin.");
        add("15:30", "MEAL", 60, "Snack 2 (~100 kcal)",
                "30 g roasted chana or a small bowl of sprouts, with unsweetened green tea or black coffee.");
        add("15:35", "SEEDS", 90, "Seeds #2: pumpkin + sunflower (1 tbsp)",
                "Zinc and vitamin E for skin. Eat with the snack. Seeds are calorie-dense - keep to 1 tbsp.");
        add("15:45", "WORK", 30, "Start Work (2.5 h)",
                "Tap Done when you START. Work block ends 6:15 PM.");
        add("16:30", "WATER", 45, "Water 500 ml (#5 of 7)",
                "Sip over 10 minutes.");
        add("18:15", "HYGIENE", 60, "Bath 2 (evening)",
                "Lukewarm water, 5-10 minutes. Rinse your face with plain water only (face wash is for morning and night).");
        add("18:30", "ENJOY", 30, "Start Enjoy hour (1 h)",
                "Tap Done when you START. One guilt-free hour: walk with friends, music, a game, a hobby. You earned it - no studying.");
        add("19:00", "WATER", 45, "Water 500 ml (#6 of 7, before dinner)",
                "Drinking water before dinner helps you eat less.");
        add("19:30", "MEAL", 60, "Dinner (~450 kcal)",
                "2 rotis (no ghee) OR 3/4 cup rice, 1 cup vegetables, 1 cup dal OR 100 g chicken / fish / egg curry, salad. Finish by 8:00 PM. After 8:30 PM only water.");
        add("20:00", "FAMILY", 30, "Start Family time (1 h)",
                "Tap Done when you START. One hour with family - phone away.");
        add("20:30", "WATER", 45, "Water 500 ml (#7 of 7)",
                "Last full glass of the day. After this, only small sips so sleep is not interrupted.");
        add("21:00", "STUDY", 30, "Start Study block 3 (1 h)",
                "Tap Done when you START. Final block (9-10 PM): revise today's work and plan tomorrow.");
        add("22:00", "HYGIENE", 60, "Brush teeth (night, 2 min)",
                "2 minutes, soft brush, fluoride toothpaste, tongue too. Nothing to eat after brushing. Also soak 1 tbsp chia + flax in a glass of water for tomorrow's Seeds #1.");
        add("22:05", "SKIN", 60, "Night skin care",
                "1) Face wash 30-40 sec  2) Pat dry  3) Rose water on a cotton pad  4) Light moisturiser. Change your pillowcase once a week. Do not pick pimples.");
        add("22:30", "SLEEP", 45, "Lights out - sleep",
                "Charge your phone across the room. 7 hours of sleep (until 5:30 AM) - poor sleep raises hunger hormones and slows fat loss.");
    }

    static Task byId(int id) {
        for (Task t : TASKS) {
            if (t.id == id) return t;
        }
        return null;
    }

    /** Number of tasks that happen every day (excludes the weekly check-in). */
    static int dailyCount() {
        int n = 0;
        for (Task t : TASKS) {
            if (!t.weekly) n++;
        }
        return n;
    }
}
