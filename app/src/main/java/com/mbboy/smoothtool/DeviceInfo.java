package com.mbboy.smoothtool;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * One-time snapshot of values Android really lets a normal app read, with no extra permissions.
 * Nothing here is live, estimated or invented. Tap Refresh to take a new snapshot.
 */
final class DeviceInfo {
    private DeviceInfo() {}

    static String snapshot(Activity a) {
        StringBuilder sb = new StringBuilder();
        sb.append("Taken at ").append(new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date())).append("\n\n");
        sb.append("Phone: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL).append('\n');
        sb.append("Android ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n\n");

        // Battery (sticky broadcast, no permission needed)
        Intent b = a.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (b != null) {
            int level = b.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = b.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level >= 0 && scale > 0) {
                sb.append(String.format(Locale.US, "Battery: %.0f%%\n", 100f * level / scale));
            }
            int status = b.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL;
            sb.append("Charging: ").append(charging ? "yes" : "no").append('\n');
            int t = b.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Integer.MIN_VALUE);
            if (t != Integer.MIN_VALUE) {
                sb.append(String.format(Locale.US, "Battery temperature: %.1f C (battery sensor only, not CPU/GPU)\n", t / 10f));
            }
        } else {
            sb.append("Battery: not reported\n");
        }

        PowerManager pm = (PowerManager) a.getSystemService(Activity.POWER_SERVICE);
        if (pm != null) {
            sb.append("Battery saver on: ").append(pm.isPowerSaveMode() ? "yes" : "no").append('\n');
            if (Build.VERSION.SDK_INT >= 29) {
                sb.append("Thermal status: ").append(thermalName(pm.getCurrentThermalStatus())).append('\n');
            }
        }

        // System-wide RAM
        ActivityManager am = (ActivityManager) a.getSystemService(Activity.ACTIVITY_SERVICE);
        if (am != null) {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            sb.append('\n').append(String.format(Locale.US,
                    "RAM: %d MB free of %d MB total (system-wide)\n", mi.availMem / 1048576L, mi.totalMem / 1048576L));
            sb.append("Android says memory is low: ").append(mi.lowMemory ? "yes" : "no").append('\n');
        }

        // Internal storage
        try {
            StatFs fs = new StatFs(Environment.getDataDirectory().getPath());
            long total = fs.getTotalBytes();
            long free = fs.getAvailableBytes();
            sb.append(String.format(Locale.US, "Storage: %.1f GB free of %.1f GB (%.0f%% free)\n",
                    free / 1073741824.0, total / 1073741824.0, 100.0 * free / total));
        } catch (RuntimeException e) {
            sb.append("Storage: could not be read\n");
        }

        // Screen refresh rate (NOT game FPS)
        float hz = a.getWindowManager().getDefaultDisplay().getRefreshRate();
        sb.append(String.format(Locale.US, "\nScreen refresh rate: %.0f Hz (the screen's limit, not the game's FPS)\n", hz));
        return sb.toString();
    }

    private static String thermalName(int s) {
        switch (s) {
            case 0: return "none";
            case 1: return "light";
            case 2: return "moderate";
            case 3: return "severe";
            case 4: return "critical";
            case 5: return "emergency";
            case 6: return "shutdown";
            default: return "unknown";
        }
    }
}
