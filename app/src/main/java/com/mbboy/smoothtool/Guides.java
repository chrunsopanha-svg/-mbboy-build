package com.mbboy.smoothtool;

/**
 * All guide text. Line format used by ToolboxActivity:
 *   "# text"  -> sub-heading,  "- text" -> bullet,  "[ ] text" -> tick box,  anything else -> paragraph.
 * Game menu names change between MLBB updates, so the text describes options generally.
 */
final class Guides {
    private Guides() {}

    static final String[][] INDEX = {
            {"checklist", "Lag checklist"},
            {"graphics", "Graphics & FPS settings guide"},
            {"network", "High ping / network guide"},
            {"battery", "Battery & overheating"},
            {"storage", "Storage & background apps"},
            {"metrics", "Device snapshot (real values only)"},
            {"settings", "Open Android settings"},
            {"about", "About & permissions"},
    };

    static String titleOf(String id) {
        for (String[] e : INDEX) if (e[0].equals(id)) return e[1];
        return INDEX[0][1];
    }

    static String[] linesOf(String id) {
        switch (id) {
            case "graphics": return GRAPHICS;
            case "network": return NETWORK;
            case "battery": return BATTERY;
            case "storage": return STORAGE;
            case "metrics": return METRICS;
            case "about": return ABOUT;
            case "settings": return SETTINGS;
            default: return CHECKLIST;
        }
    }

    static final String[] CHECKLIST = {
            "Find out where the lag comes from before changing anything.",
            "# Step 1 - What kind of lag is it?",
            "- Choppy animation but ping stays low and steady: probably the phone (graphics settings, heat, storage, memory).",
            "- Ping jumps or turns yellow/red but animation stays smooth: probably the network.",
            "- Both at once: fix the network first, then the phone.",
            "- MLBB shows ping on screen during a match. Recent versions also have an optional FPS display in the game's settings; the option names change between updates.",
            "# Step 2 - Quick checks",
            "[ ] Restart the phone if it hasn't been restarted in days",
            "[ ] Close other apps from the recent-apps screen",
            "[ ] At least 10-15% of internal storage is free",
            "[ ] Battery saver is off (or you accept slower performance)",
            "[ ] Phone is not hot to the touch; case removed; not charging while playing",
            "[ ] In-game graphics set to Smooth/Low or Balanced, frame rate Normal or High",
            "[ ] Stable Wi-Fi (close to the router) or a strong mobile signal",
            "[ ] No downloads, updates or video streaming on the same network",
            "[ ] MLBB and Android system updates are installed and the game's resource download is complete",
            "[ ] Other games/apps run smoothly (if not, the phone itself is the issue)",
            "# Step 3 - Decide",
            "If everything is ticked and lag remains, test at another time of day or on another network. Evening peaks and busy servers cause lag that no phone setting can fix.",
    };

    static final String[] GRAPHICS = {
            "These are settings you change yourself inside MLBB. This app cannot change them or make the game faster; it only reminds you what to try.",
            "# What to try (lowest effort first)",
            "- Graphics quality: choose Smooth/Low or Balanced instead of HD or Ultra HD.",
            "- Frame rate: choose Normal or High, whichever your phone offers. Higher frame-rate options use more CPU/GPU, create more heat and drain the battery faster. A steady lower FPS feels better than a high FPS that keeps dropping.",
            "- Turn off extras if the game lists them: shadows, anti-aliasing, high-resolution/HD textures, extra battle effects.",
            "- Restart the game after changing graphics options, then play one match to compare.",
            "# Notes for budget phones such as the OPPO A7",
            "- The A7 is an older entry-level phone. Expect Smooth/Balanced to be the realistic choice; some higher options may not even appear on your phone.",
            "- If lag only starts after 10-15 minutes, heat is the likely cause. See 'Battery & overheating'.",
            "# Phone-side options",
            "- Turn on Do Not Disturb to avoid pop-ups mid-match.",
            "- If your ColorOS version has a game mode or 'Game Space', look at what it offers, but treat claims of big FPS gains with caution.",
            "- Keep the system and the game updated.",
            "# What not to expect",
            "No overlay, cleaner or 'booster' app can unlock more FPS than the phone's hardware and the game's own limits allow.",
    };

    static final String[] NETWORK = {
            "Ping is the round-trip time between your phone and the game server, in milliseconds. Lower is better. As a very rough guide, under about 60 ms feels fine, 100+ ms is noticeable, and 150+ ms is painful. These thresholds are approximate.",
            "# Fixes to try, in order",
            "- Turn Wi-Fi off and use mobile data (or the reverse) and compare the in-game ping.",
            "- On Wi-Fi: move closer to the router, use the 5 GHz band if your router and phone support it, and restart the router.",
            "- Pause downloads, app updates, cloud backups and video streaming on every device on the network.",
            "- Toggle airplane mode on for about 10 seconds, then off, to re-register on the mobile network.",
            "- Turn off any VPN, then test. A game VPN can help or hurt depending on your ISP, so only keep it if ping is clearly better.",
            "- Check the game's own server/region and network options if shown. Your account's region usually can't be changed freely.",
            "- If ping is bad at the same time every evening on every device, the cause is probably your ISP or local congestion. Contact them.",
            "# Honest limits",
            "This app does not measure ping. A meaningful ping test needs to talk to the game's servers, and this app deliberately has no Internet permission. Use the ping shown inside MLBB.",
    };

    static final String[] BATTERY = {
            "When a phone gets hot, Android and the chipset slow down on purpose to protect themselves (thermal throttling). That shows up as FPS dropping mid-match.",
            "# Reduce heat",
            "- Take the case off while playing.",
            "- Avoid direct sun, hot cars and soft surfaces such as beds or pillows.",
            "- Don't play while charging if you can avoid it; charging adds heat of its own.",
            "- Lower the screen brightness.",
            "- Use a lower graphics setting and a lower frame-rate option.",
            "- Take a short break if the phone is uncomfortable to hold.",
            "# Battery",
            "- Battery saver may reduce performance. Try it off during matches if you have enough charge.",
            "- Below roughly 20% some phones throttle more. Charge before long sessions.",
            "- Old batteries (two to three years or more) can cause slowdowns and sudden drops. A battery replacement at a service centre can help.",
            "# Safety",
            "If the phone becomes too hot to hold, the battery looks swollen, or you notice a burning smell, stop using it and power it off.",
            "# What this app can show",
            "The Device snapshot shows battery percentage and battery temperature (from the battery sensor). It cannot read CPU or GPU temperature on current Android versions.",
    };

    static final String[] STORAGE = {
            "# Storage",
            "- Keep at least 10-15% of internal storage free. Nearly full storage makes the whole phone slower, including game loading.",
            "- Remove unused apps, old downloads, and chat-app media (video and voice-note folders grow large).",
            "- Move photos and videos to cloud storage or a memory card if your phone supports it.",
            "- In Settings > Apps > Mobile Legends > Storage you can choose Clear cache. Do not choose Clear data unless you want to delete local game data and re-download resources.",
            "# Background apps",
            "- Swipe away apps you don't need from the recent-apps screen before playing.",
            "- Pause large downloads, auto-updates and cloud sync while you play.",
            "- Android manages memory itself. Closing apps frees RAM only for apps that are actually running.",
            "- Your phone's own cleaner or 'Phone Manager' tool (if it has one) is the right place for force-stopping apps. This app will not do that.",
            "- Avoid 'Background process limit' and 'Don't keep activities' in Developer options. They can cause crashes and reloads.",
            "# Honest limits",
            "A normal app can see total and free RAM and storage but cannot see how much RAM each other app uses, and cannot legitimately kill other apps without root.",
    };

    static final String[] METRICS = {
            "# What Android lets this app read (no special permission)",
            "- Battery percentage, charging state and battery temperature",
            "- Total and available system RAM, and whether Android reports low memory",
            "- Free and total internal storage",
            "- Whether battery saver is on",
            "- The screen's refresh rate (a display limit, not the game's FPS)",
            "- Thermal status category on Android 10 and newer",
            "# What it cannot read",
            "- Another app's FPS, CPU use, GPU use or RAM use (blocked for normal apps on modern Android)",
            "- CPU/GPU temperature on current Android versions",
            "- Ping to the game server (this app has no Internet permission)",
            "Anything that claims to show these live inside another app is estimating, using elevated access, or faking it. This app does none of that.",
            "# Snapshot",
    };

    static final String[] SETTINGS = {
            "These buttons only open Android's own settings screens so you can look at or change things yourself. This app changes nothing. Some screens vary by phone; if one isn't available, main Settings opens instead.",
            "# Shortcuts",
    };

    static final String[] ABOUT = {
            "MBBOY MLBB Smooth Tool is a lightweight set of troubleshooting guides with an optional floating button. It is not affiliated with Moonton or Mobile Legends: Bang Bang.",
            "# What it does not do",
            "- It does not increase FPS, inject into the game, automate play, or touch anti-cheat.",
            "- It does not force-stop other apps or change protected settings.",
            "- It has no ads, no analytics and no Internet permission.",
            "# Permissions",
            "- Display over other apps (SYSTEM_ALERT_WINDOW): draws the button and menu. Requested only when you tap Start.",
            "- Foreground service (+ special use type on Android 14): keeps the overlay alive while you play, with a notification.",
            "- Notifications (Android 13+): optional. Lets you Hide/Show/Stop from the notification.",
            "# Background activity",
            "The service runs only while the floating button is active. Stop it from the menu, the notification, or the main screen.",
            "Version 0.1.0",
    };
}
