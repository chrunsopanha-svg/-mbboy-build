package com.mbboy.smoothtool;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ToolboxActivity extends Activity {
    static final String EXTRA_SECTION = "section";

    private final List<CheckBox> checks = new ArrayList<>();
    private LinearLayout col;
    private String section;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        section = getIntent().getStringExtra(EXTRA_SECTION);
        if (section == null) section = "checklist";

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Ui.BG);
        col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(this, 16);
        col.setPadding(p, p, p, p);
        sv.addView(col);
        setContentView(sv);

        TextView title = Ui.text(this, Guides.titleOf(section), 20, Ui.ORANGE, true);
        title.setPadding(0, 0, 0, Ui.dp(this, 8));
        col.addView(title);

        render(Guides.linesOf(section));

        switch (section) {
            case "checklist":
                col.addView(Ui.button(this, "Reset checklist", Ui.CARD, 14, 12, v -> {
                    for (CheckBox c : checks) c.setChecked(false);
                }));
                break;
            case "metrics":
                addSnapshot();
                break;
            case "settings":
                addSettingsButtons();
                break;
            default:
                break;
        }
        addNavigation();
    }

    private void render(String[] lines) {
        for (String line : lines) {
            if (line.startsWith("# ")) {
                TextView h = Ui.text(this, line.substring(2), 16, Ui.ORANGE, true);
                h.setPadding(0, Ui.dp(this, 16), 0, Ui.dp(this, 4));
                col.addView(h);
            } else if (line.startsWith("[ ] ")) {
                CheckBox cb = new CheckBox(this);
                cb.setText(line.substring(4));
                cb.setTextColor(Ui.TEXT);
                cb.setTextSize(14);
                checks.add(cb);
                col.addView(cb);
            } else if (line.startsWith("- ")) {
                TextView t = Ui.text(this, "\u2022  " + line.substring(2), 14, Ui.TEXT, false);
                t.setPadding(Ui.dp(this, 4), Ui.dp(this, 3), 0, Ui.dp(this, 3));
                col.addView(t);
            } else {
                TextView t = Ui.text(this, line, 14, Ui.TEXT, false);
                t.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
                col.addView(t);
            }
        }
    }

    private void addSnapshot() {
        final TextView snap = Ui.text(this, DeviceInfo.snapshot(this), 13, Ui.TEXT, false);
        int p = Ui.dp(this, 12);
        snap.setPadding(p, p, p, p);
        snap.setBackground(Ui.rounded(this, Ui.CARD, 10));
        snap.setTextIsSelectable(true);
        col.addView(snap);
        col.addView(Ui.button(this, "Refresh snapshot", Ui.ORANGE, 14, 12, v -> snap.setText(DeviceInfo.snapshot(this))));
    }

    private void addSettingsButtons() {
        addSetting("Display settings", new Intent(Settings.ACTION_DISPLAY_SETTINGS));
        if (Build.VERSION.SDK_INT >= 22) {
            addSetting("Battery saver", new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS));
        }
        addSetting("Battery usage", new Intent("android.intent.action.POWER_USAGE_SUMMARY"));
        addSetting("Wi-Fi", new Intent(Settings.ACTION_WIFI_SETTINGS));
        addSetting("Mobile network / wireless", new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        addSetting("Internal storage", new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
        addSetting("All apps", new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS));
        addSetting("Mobile Legends app info (if installed)", SettingsLauncher.appDetails(SettingsLauncher.MLBB_PACKAGE));
        addSetting("Developer options (if enabled on your phone)", new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        if (Build.VERSION.SDK_INT >= 23) {
            addSetting("Overlay permission for this app", new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:" + getPackageName())));
        }
    }

    private void addSetting(String label, final Intent intent) {
        col.addView(Ui.button(this, label, Ui.CARD, 14, 12, v -> SettingsLauncher.open(this, intent)));
    }

    private void addNavigation() {
        TextView h = Ui.text(this, "Other guides", 14, Ui.MUTED, true);
        h.setPadding(0, Ui.dp(this, 24), 0, Ui.dp(this, 2));
        col.addView(h);
        for (String[] entry : Guides.INDEX) {
            final String id = entry[0];
            if (id.equals(section)) continue;
            col.addView(Ui.button(this, entry[1], Color.TRANSPARENT, 14, 10, v -> {
                startActivity(new Intent(this, ToolboxActivity.class).putExtra(EXTRA_SECTION, id));
                finish();
            }));
        }
    }
}
