package com.mbboy.smoothtool;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_NOTIF = 21;

    private TextView status;
    private boolean waitingForOverlayPermission = false;
    private boolean askedNotificationPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Ui.BG);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(this, 16);
        col.setPadding(p, p, p, p);
        sv.addView(col);

        col.addView(Ui.text(this, "MBBOY MLBB Smooth Tool", 22, Ui.ORANGE, true));
        TextView intro = Ui.text(this,
                "A floating toolbox of troubleshooting guides for Mobile Legends. "
                        + "It does not boost FPS and it never touches the game.", 14, Ui.MUTED, false);
        intro.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 12));
        col.addView(intro);

        status = Ui.text(this, "", 14, Ui.TEXT, false);
        int sp = Ui.dp(this, 12);
        status.setPadding(sp, sp, sp, sp);
        status.setBackground(Ui.rounded(this, Ui.CARD, 10));
        col.addView(status);

        col.addView(Ui.button(this, "Start floating button", Ui.ORANGE, 15, 14, v -> onStartClicked()));
        col.addView(Ui.button(this, "Hide / show button", Ui.CARD, 15, 14, v -> {
            if (OverlayService.running) OverlayService.send(this, OverlayService.ACTION_TOGGLE);
            else Toast.makeText(this, "Start the floating button first.", Toast.LENGTH_SHORT).show();
            status.postDelayed(this::refreshStatus, 400);
        }));
        col.addView(Ui.button(this, "Stop floating button", Ui.CARD, 15, 14, v -> {
            if (OverlayService.running) OverlayService.send(this, OverlayService.ACTION_STOP);
            status.postDelayed(this::refreshStatus, 400);
        }));

        TextView h = Ui.text(this, "Guides (work without the overlay)", 16, Ui.ORANGE, true);
        h.setPadding(0, Ui.dp(this, 20), 0, Ui.dp(this, 4));
        col.addView(h);
        for (String[] entry : Guides.INDEX) {
            final String id = entry[0];
            col.addView(Ui.button(this, entry[1], Ui.CARD, 14, 12, v ->
                    startActivity(new Intent(this, ToolboxActivity.class).putExtra(ToolboxActivity.EXTRA_SECTION, id))));
        }
        setContentView(sv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (waitingForOverlayPermission) {
            waitingForOverlayPermission = false;
            if (OverlayService.canDrawOverlays(this)) {
                continueStart();
            } else {
                Toast.makeText(this, "Permission not granted, so the floating button can't be shown.",
                        Toast.LENGTH_LONG).show();
            }
        }
        refreshStatus();
    }

    private void refreshStatus() {
        String s;
        if (OverlayService.running) {
            s = OverlayService.hidden
                    ? "Overlay is running but hidden. Tap 'Hide / show button' to bring it back."
                    : "Overlay is running. Open MLBB and look for the orange MBBOY button.";
        } else if (!OverlayService.canDrawOverlays(this)) {
            s = "Overlay permission not granted yet. It is requested only when you tap Start. "
                    + "The guides below work without it.";
        } else {
            s = "Overlay permission granted. Tap Start to show the button.";
        }
        status.setText(s);
    }

    private void onStartClicked() {
        if (OverlayService.canDrawOverlays(this)) {
            continueStart();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Allow display over other apps?")
                .setMessage("To show the floating button on top of Mobile Legends, Android needs the "
                        + "'Display over other apps' permission for this app. You will be taken to a "
                        + "settings screen where you switch it on.\n\nThis app uses it only to draw the button and menu.")
                .setPositiveButton("Continue", (d, w) -> {
                    waitingForOverlayPermission = true;
                    try {
                        startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName())));
                    } catch (RuntimeException e) {
                        waitingForOverlayPermission = false;
                        SettingsLauncher.open(this, new Intent(Settings.ACTION_SETTINGS));
                    }
                })
                .setNegativeButton("Not now", (d, w) -> Toast.makeText(this,
                        "No problem. The overlay stays off; the guides still work.", Toast.LENGTH_LONG).show())
                .show();
    }

    /** Overlay permission is granted; optionally ask for notification permission (Android 13+), then start. */
    private void continueStart() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                && !askedNotificationPermission) {
            askedNotificationPermission = true;
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
            return;
        }
        startOverlay();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIF) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                Toast.makeText(this, "Notification hidden. Use the menu's 'Close overlay' or this screen to stop.",
                        Toast.LENGTH_LONG).show();
            }
            startOverlay(); // works either way
        }
    }

    private void startOverlay() {
        OverlayService.send(this, OverlayService.ACTION_START);
        Toast.makeText(this, "Floating button started. Open MLBB.", Toast.LENGTH_SHORT).show();
        status.postDelayed(this::refreshStatus, 400);
    }
}
