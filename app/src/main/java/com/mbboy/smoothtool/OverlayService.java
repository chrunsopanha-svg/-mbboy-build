package com.mbboy.smoothtool;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Draws the orange MBBOY bubble and its menu. It only shows views and opens guides/settings screens.
 * It does not read other apps, measure FPS, or touch the game in any way.
 */
public class OverlayService extends Service {
    public static final String ACTION_START = "com.mbboy.smoothtool.START";
    public static final String ACTION_STOP = "com.mbboy.smoothtool.STOP";
    public static final String ACTION_TOGGLE = "com.mbboy.smoothtool.TOGGLE";

    private static final String CHANNEL_ID = "mbboy_overlay";
    private static final int NOTIF_ID = 1;

    public static volatile boolean running = false;
    public static volatile boolean hidden = false;

    private WindowManager wm;
    private TextView bubble;
    private View menu;
    private WindowManager.LayoutParams bubbleLp;
    private WindowManager.LayoutParams menuLp;
    private boolean bubbleAttached = false;
    private boolean menuAttached = false;
    private int bubbleSize;
    private int touchSlop;

    static void send(Context c, String action) {
        Intent i = new Intent(c, OverlayService.class).setAction(action);
        if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(i);
        else c.startService(i);
    }

    static boolean canDrawOverlays(Context c) {
        return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(c);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        enterForeground(); // must happen promptly after startForegroundService()

        if (ACTION_STOP.equals(action) || !canDrawOverlays(this)) {
            shutDown();
            return START_NOT_STICKY;
        }
        if (wm == null && !setUp()) {
            shutDown();
            return START_NOT_STICKY;
        }
        running = true;
        if (ACTION_TOGGLE.equals(action)) setHidden(!hidden);
        else setHidden(false);
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        closeMenu();
        if (bubbleAttached) {
            clampBubble();
            try { wm.updateViewLayout(bubble, bubbleLp); } catch (RuntimeException ignored) { }
        }
    }

    @Override
    public void onDestroy() {
        closeMenu();
        detachBubble();
        running = false;
        hidden = false;
        super.onDestroy();
    }

    // ---------------------------------------------------------------- setup

    private boolean setUp() {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm == null) return false;
        bubbleSize = Ui.dp(this, 56);
        touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        bubble = Ui.text(this, "MBBOY", 11, Color.WHITE, true);
        bubble.setGravity(Gravity.CENTER);
        bubble.setSingleLine(true);
        bubble.setIncludeFontPadding(false);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Ui.ORANGE);
        circle.setStroke(Ui.dp(this, 2), Color.WHITE);
        bubble.setBackground(circle);
        bubble.setContentDescription("MBBOY floating button. Tap for menu, drag to move.");
        bubble.setOnTouchListener(dragListener);
        bubble.setOnClickListener(v -> toggleMenu());

        bubbleLp = new WindowManager.LayoutParams(bubbleSize, bubbleSize, overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        bubbleLp.gravity = Gravity.TOP | Gravity.START;
        bubbleLp.x = Ui.dp(this, 12);
        bubbleLp.y = Ui.dp(this, 160);
        return true;
    }

    @SuppressWarnings("deprecation")
    private static int overlayType() {
        return Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    // ---------------------------------------------------------------- bubble

    private final View.OnTouchListener dragListener = new View.OnTouchListener() {
        private int startX, startY;
        private float downX, downY;
        private boolean moved;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startX = bubbleLp.x;
                    startY = bubbleLp.y;
                    downX = e.getRawX();
                    downY = e.getRawY();
                    moved = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = e.getRawX() - downX;
                    float dy = e.getRawY() - downY;
                    if (!moved && Math.hypot(dx, dy) > touchSlop) moved = true;
                    if (moved) {
                        if (menuAttached) closeMenu();
                        bubbleLp.x = startX + Math.round(dx);
                        bubbleLp.y = startY + Math.round(dy);
                        clampBubble();
                        try { wm.updateViewLayout(bubble, bubbleLp); } catch (RuntimeException ignored) { }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!moved) v.performClick();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    return true;
                default:
                    return false;
            }
        }
    };

    private void clampBubble() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        bubbleLp.x = Math.max(0, Math.min(bubbleLp.x, dm.widthPixels - bubbleSize));
        bubbleLp.y = Math.max(0, Math.min(bubbleLp.y, dm.heightPixels - bubbleSize));
    }

    private void attachBubble() {
        if (bubbleAttached || wm == null) return;
        clampBubble();
        try {
            wm.addView(bubble, bubbleLp);
            bubbleAttached = true;
        } catch (RuntimeException e) { // BadTokenException / SecurityException if permission was revoked
            Toast.makeText(this, "Android refused to show the overlay. Check 'Display over other apps'.",
                    Toast.LENGTH_LONG).show();
            shutDown();
        }
    }

    private void detachBubble() {
        if (!bubbleAttached || wm == null) return;
        try { wm.removeView(bubble); } catch (RuntimeException ignored) { }
        bubbleAttached = false;
    }

    private void setHidden(boolean h) {
        hidden = h;
        if (h) {
            closeMenu();
            detachBubble();
        } else {
            attachBubble();
        }
        refreshNotification();
    }

    // ---------------------------------------------------------------- menu

    private void toggleMenu() {
        if (menuAttached) closeMenu();
        else openMenu();
    }

    private void openMenu() {
        if (menuAttached || !bubbleAttached) return;
        menu = buildMenu();
        int w = Ui.dp(this, 230);
        menu.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int gap = Ui.dp(this, 8);
        int h = Math.min(menu.getMeasuredHeight(), dm.heightPixels - 2 * gap);

        int x = bubbleLp.x + bubbleSize + gap;
        if (x + w > dm.widthPixels) x = bubbleLp.x - w - gap;
        x = Math.max(0, Math.min(x, dm.widthPixels - w));
        int y = Math.max(gap, Math.min(bubbleLp.y, dm.heightPixels - h - gap));

        menuLp = new WindowManager.LayoutParams(w, h, overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        menuLp.gravity = Gravity.TOP | Gravity.START;
        menuLp.x = x;
        menuLp.y = y;
        try {
            wm.addView(menu, menuLp);
            menuAttached = true;
        } catch (RuntimeException e) {
            menu = null;
            Toast.makeText(this, "Couldn't open the menu.", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeMenu() {
        if (!menuAttached || wm == null) return;
        try { wm.removeView(menu); } catch (RuntimeException ignored) { }
        menuAttached = false;
        menu = null;
    }

    private View buildMenu() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(this, 10);
        root.setPadding(p, p, p, p);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = Ui.text(this, "MBBOY Smooth Tool", 14, Ui.ORANGE, true);
        header.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView collapse = Ui.text(this, "\u00d7", 22, Ui.MUTED, false);
        collapse.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 4), 0);
        collapse.setContentDescription("Collapse menu");
        collapse.setOnClickListener(v -> closeMenu());
        header.addView(collapse);
        root.addView(header);

        root.addView(Ui.text(this, "Troubleshooting guides. This menu cannot raise FPS.", 10, Ui.MUTED, false));

        for (String[] entry : Guides.INDEX) {
            final String id = entry[0];
            if (id.equals("about")) continue;
            root.addView(Ui.button(this, entry[1], 0xFF262E39, 13, 9, v -> openSection(id)));
        }

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView hide = Ui.button(this, "Hide", 0xFF262E39, 13, 9, v -> setHidden(true));
        TextView quit = Ui.button(this, "Close overlay", Ui.ORANGE, 13, 9, v -> shutDown());
        hide.setLayoutParams(rowParams(1f, 0));
        quit.setLayoutParams(rowParams(1.4f, Ui.dp(this, 6)));
        row.addView(hide);
        row.addView(quit);
        root.addView(row);

        ScrollView sv = new ScrollView(this);
        sv.setBackground(Ui.rounded(this, 0xF2161B22, 14));
        sv.addView(root);
        return sv;
    }

    private LinearLayout.LayoutParams rowParams(float weight, int startMargin) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
        lp.topMargin = Ui.dp(this, 6);
        lp.leftMargin = startMargin;
        return lp;
    }

    private void openSection(String id) {
        closeMenu();
        Intent i = new Intent(this, ToolboxActivity.class)
                .putExtra(ToolboxActivity.EXTRA_SECTION, id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(i);
        } catch (RuntimeException e) {
            Toast.makeText(this, "Android blocked opening the guide from here. Open the MBBOY app instead.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // ---------------------------------------------------------------- lifecycle / notification

    private void shutDown() {
        closeMenu();
        detachBubble();
        running = false;
        hidden = false;
        wm = null;
        stopForeground(true);
        stopSelf();
    }

    private void enterForeground() {
        Notification n = buildNotification();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIF_ID, n);
        }
    }

    private void refreshNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIF_ID, buildNotification());
    }

    private PendingIntent actionIntent(String action, int req) {
        Intent i = new Intent(this, OverlayService.class).setAction(action);
        int f = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
        return Build.VERSION.SDK_INT >= 26
                ? PendingIntent.getForegroundService(this, req, i, f)
                : PendingIntent.getService(this, req, i, f);
    }

    @SuppressWarnings("deprecation")
    private Notification buildNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26) {
            if (nm != null) {
                nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "MBBOY overlay",
                        NotificationManager.IMPORTANCE_LOW));
            }
            b = new Notification.Builder(this, CHANNEL_ID);
        } else {
            b = new Notification.Builder(this).setPriority(Notification.PRIORITY_LOW);
        }
        int f = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
        PendingIntent open = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), f);
        b.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("MBBOY floating toolbox")
                .setContentText(hidden ? "Button hidden" : "Button visible")
                .setContentIntent(open)
                .setOngoing(true)
                .addAction(0, hidden ? "Show" : "Hide", actionIntent(ACTION_TOGGLE, 1))
                .addAction(0, "Stop", actionIntent(ACTION_STOP, 2));
        return b.build();
    }
}
