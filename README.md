# Bill Gernator (Android APK)

Wraps the Banjaara's billing web app in a native Android WebView shell, with
a coin system gated by Google AdMob rewarded ads.

## How it works

- `app/src/main/assets/bill.html` — your billing app, unchanged in behavior
  except: "Grand Total" → "Total Payable", a **Sender Name** field (set once,
  live-updates the business name everywhere on the invoice), and a **coin
  balance** system.
- Coins: generating a bill costs **5 coins**. Watching a rewarded ad grants
  **10 coins**. Balance is stored on-device (resets if the app is
  uninstalled).
- `MainActivity.java` loads that HTML into a WebView and exposes
  `window.AndroidAds.showRewardedAd()` to it. When a user finishes watching
  an ad, the app calls `window.onRewardedAdEarned()` in the page, which adds
  the coins.

## Ad unit status

Both AdMob IDs are wired in and ready:
- **App ID**: `ca-app-pub-4344006394945876~1672630117` (in `AndroidManifest.xml`)
- **Rewarded ad unit**: `ca-app-pub-4344006394945876/1082487651` (in `MainActivity.java`)

**⚠️ These are real, live ads — not test ads.** Don't repeatedly tap "watch
ad" yourself to test the flow; Google can flag your AdMob account for
invalid traffic from self-clicks. To test safely:
- Register your phone as a test device in the AdMob console
  (Settings → Test devices), which makes real ad units serve harmless test
  creatives on that device only, **or**
- Temporarily swap `REWARDED_AD_UNIT_ID` in `MainActivity.java` back to
  Google's public test ID `ca-app-pub-3940256099942544/5224354917` while
  actively testing, then swap back before real use.

## Building the APK

Every push to `main`/`master` (or manually via **Actions → Build APK → Run
workflow**) builds a debug APK and uploads it as a workflow artifact named
`bill-gernator-debug-apk`. Download it from the Actions run page.

This is a **debug**, unsigned build — fine for installing and testing on
your own device, but not set up for Play Store release signing. Ask if you
want a signed release build added later (needs a keystore + GitHub secrets).

## Swapping the app icon

Replace the PNGs in `app/src/main/res/mipmap-*/ic_launcher.png` (and
`ic_launcher_round.png`) with your real icon at the matching sizes:
mdpi 48px, hdpi 72px, xhdpi 96px, xxhdpi 144px, xxxhdpi 192px.

## Installing the APK

Download `app-debug.apk` from the Actions artifact, transfer it to your
Android phone, and open it (you'll need to allow "install from unknown
sources" the first time).

## Uploading this project to GitHub

You need this project as a real GitHub repo (not just a zip) for the
`.github/workflows/build.yml` file to trigger automatically. Two ways:

### Option A — Upload via GitHub's website (no extraction needed by you)

1. Go to [github.com](https://github.com) → click **+** (top right) → **New
   repository**. Name it (e.g. `bill-gernator`), keep it Private or Public
   as you like, don't add a README/gitignore (you already have one), click
   **Create repository**.
2. On the new empty repo page, click **uploading an existing file**.
3. **Do not drag the zip file itself** — GitHub will just store the zip as
   one file, it won't extract it, and the workflow won't be found.
4. Extract the zip **first** (on your phone: most file manager apps have
   "Extract"/"Unzip" built in, or use an app like ZArchiver). You'll get a
   `BillGernator` folder.
5. Drag the **contents** of that folder (the `app` folder, `.github`
   folder, `build.gradle`, `settings.gradle`, etc. — not the outer
   `BillGernator` folder itself) into the GitHub upload box. GitHub lets you
   select multiple files/folders at once.
6. Scroll down, click **Commit changes**.
7. Go to the **Actions** tab of your repo — a build should start
   automatically within a few seconds. Wait for the green checkmark, click
   into the run, and download the APK from **Artifacts** at the bottom.

### Option B — Extract on your device first, then upload folder-by-folder

If your phone's browser won't let you upload a whole folder at once:
1. Extract the zip locally.
2. In GitHub's upload page, upload the top-level files first (`build.gradle`,
   `settings.gradle`, `gradle.properties`, `gradlew`, `README.md`), commit.
3. Then use **Add file → Upload files** again for the `app/` folder
   contents, keeping the same folder structure (GitHub auto-creates folders
   based on the path you drop files into, or you can create files one at a
   time via **Add file → Create new file** and type the path like
   `app/build.gradle` in the filename box, which auto-creates the folder).
4. Repeat for `.github/workflows/build.yml` and `gradle/wrapper/`.
5. This is slower — Option A (drag the whole extracted folder's contents at
   once) is much easier if your device supports it.

**Key point either way: GitHub needs the individual files in their folders,
not a zip sitting in the repo.** A zip file uploaded as-is won't be read by
GitHub Actions.
