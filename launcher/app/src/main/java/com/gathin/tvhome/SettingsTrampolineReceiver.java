package com.gathin.tvhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

/**
 * Trampoline receiver for system settings button broadcasts and vendor shortcut intents.
 * Forwards directly to Android TV settings without requiring vendor bloatware.
 */
public class SettingsTrampolineReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) return;
        try {
            Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(settingsIntent);
        } catch (Exception e) {
            try {
                Intent fallback = context.getPackageManager().getLaunchIntentForPackage("com.android.tv.settings");
                if (fallback != null) {
                    fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(fallback);
                }
            } catch (Exception ignored) {}
        }
    }
}
