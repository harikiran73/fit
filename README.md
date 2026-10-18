# FitCoach - personal trainer app (110 kg -> 90 kg, 20-10-2026 to 20-03-2027)

An Android app with a daily schedule, diet / water / skin-care guide, reminders that repeat
until you tap Done (sound, vibration, or both), penalties for missed tasks, and weekly feedback.

## Get your APK (no Android Studio needed)

1. Make a free account at github.com and click **New repository**. Name it `FitCoach`
   and choose **Private**. Click **Create repository**.
2. On the new repo page click **uploading an existing file**.
3. Unzip FitCoach.zip. Open the unzipped folder and drag **everything inside it**
   (the `app` folder, `.github` folder, `build.gradle`, `settings.gradle`, `gradle.properties`,
   `README.md`, `.gitignore`) into the browser. Click **Commit changes**.
   - Check that `.github/workflows/build.yml` is in the repo. If the `.github` folder did not
     upload: click **Add file -> Create new file**, type `.github/workflows/build.yml` as the
     name, paste the content of that file from the unzipped folder, and commit.
4. Click the **Actions** tab (click "I understand, enable workflows" if asked). The workflow
   **Build APK** starts by itself. The first build takes about 5-8 minutes.
   You can also start it with **Run workflow**.
5. When the run shows a green tick, open it. At the bottom under **Artifacts** download
   `FitCoach-debug-apk`. Unzip it to get `app-debug.apk`.
6. Send `app-debug.apk` to your phone (USB, WhatsApp, Telegram, Google Drive) and tap it.
   Allow "Install unknown apps" when Android asks. If Play Protect warns, choose **Install anyway**
   (it is a self-built debug app).

## First-time setup on the phone

1. Open FitCoach and allow **Notifications**.
2. Settings tab -> choose **Alert mode** (Vibration only is best for the institute) and the repeat gap.
3. Tap **Send a test alert now** with the phone on silent to check vibration.
4. Settings tab -> **Battery: allow background running** -> set FitCoach to Unrestricted.
   On Xiaomi / Oppo / Vivo / Realme / Samsung also allow Autostart / Run in background.
5. Reminders start on 20-10-2026 at 5:30 AM. Before that, the Today tab shows a preview.

## Using it

- **Today**: tap a row to mark it done, long-press to change its time. Red box = penalties to do.
- **Guide**: full diet, water, skin care, seeds, exercise and sleep plan.
- **Progress**: save your weight; every Tuesday you get weekly feedback.

## Changing the plan

- Dates, start weight and goal: top of `app/src/main/java/com/zamer/fitcoach/Store.java`.
- Tasks, times, texts and penalties: `Plan.java`.
