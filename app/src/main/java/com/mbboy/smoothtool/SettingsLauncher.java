package com.mbboy.smoothtool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

/** Opens standard Android settings screens. Never changes any setting itself. */
final class SettingsLauncher {
    static final String MLBB_PACKAGE = "com.mobile.legends";

    private SettingsLauncher() {}

    static void open(Context c, Intent intent) {
        if (!(c instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            c.startActivity(intent);
            return;
        } catch (RuntimeException ignored) {
            // ActivityNotFoundException or SecurityException: this phone doesn't expose that screen.
        }
        try {
            Intent fallback = new Intent(Settings.ACTION_SETTINGS);
            if (!(c instanceof Activity)) fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(fallback);
            Toast.makeText(c, "That screen isn't available on this phone. Opened main Settings instead.",
                    Toast.LENGTH_LONG).show();
        } catch (RuntimeException e) {
            Toast.makeText(c, "Couldn't open Settings on this phone.", Toast.LENGTH_LONG).show();
        }
    }

    static Intent appDetails(String pkg) {
        return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pkg));
    }
}
