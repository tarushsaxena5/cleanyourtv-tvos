package com.gathin.tvhome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Translucent Apple tvOS-style quick management menu for Bluetooth accessories:
 * shows connected devices (with 1-click disconnect), saved devices (with 1-click connect & hold to forget),
 * and direct pairing discovery button.
 */
public class BluetoothDialog {

    public static void show(MainActivity activity, View anchor, BluetoothHelper helper) {
        if (activity == null || anchor == null || helper == null) return;

        float density = activity.getResources().getDisplayMetrics().density;
        View panel = activity.getLayoutInflater().inflate(R.layout.popup_bluetooth, null, false);
        LinearLayout rows = panel.findViewById(R.id.rows);
        View btnPairNew = panel.findViewById(R.id.btnPairNew);

        // Pre-create popup so rows can reference it in callbacks
        int width = (int) (340 * density);
        final PopupWindow popup = new PopupWindow(panel, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setAnimationStyle(0);
        popup.setElevation(0f);
        popup.setOnDismissListener(() -> activity.setOpenPopup(null));
        activity.setOpenPopup(popup);

        if (!helper.isBluetoothEnabled()) {
            addStatusRow(activity, rows, popup, R.drawable.ic_bluetooth,
                    activity.getString(R.string.bluetooth_off),
                    "Click to open TV Bluetooth Settings", () -> {
                        try {
                            activity.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                        } catch (Exception ignored) {}
                    });
        } else {
            List<BluetoothHelper.DeviceInfo> all = helper.getAllBondedDevices();
            List<BluetoothHelper.DeviceInfo> connected = new ArrayList<>();
            List<BluetoothHelper.DeviceInfo> saved = new ArrayList<>();

            for (BluetoothHelper.DeviceInfo d : all) {
                if (d.isConnected) connected.add(d);
                else saved.add(d);
            }

            if (connected.isEmpty() && saved.isEmpty()) {
                addStatusRow(activity, rows, popup, R.drawable.ic_bluetooth,
                        activity.getString(R.string.no_paired_devices),
                        "Click 'Pair New Accessory' to search", null);
            } else {
                if (!connected.isEmpty()) {
                    addSectionHeader(activity, rows, activity.getString(R.string.bluetooth_connected_section));
                    for (BluetoothHelper.DeviceInfo d : connected) {
                        addDeviceRow(activity, rows, popup, helper, d, true, density);
                    }
                }

                if (!saved.isEmpty()) {
                    if (!connected.isEmpty()) addSeparator(rows, density);
                    addSectionHeader(activity, rows, activity.getString(R.string.bluetooth_paired_section));
                    for (BluetoothHelper.DeviceInfo d : saved) {
                        addDeviceRow(activity, rows, popup, helper, d, false, density);
                    }
                }
            }
        }

        btnPairNew.setOnFocusChangeListener((v, hasFocus) -> {
            int primary = hasFocus ? 0xFF111111 : 0xFFFFFFFF;
            ImageView pairIcon = btnPairNew.findViewById(R.id.pairIcon);
            TextView pairText = btnPairNew.findViewById(R.id.pairText);
            if (pairIcon != null) pairIcon.setColorFilter(primary);
            if (pairText != null) pairText.setTextColor(primary);
        });

        btnPairNew.setOnClickListener(v -> {
            popup.dismiss();
            BluetoothHelper.launchPairing(activity);
        });

        panel.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int finalWidth = Math.max((int) (340 * density), panel.getMeasuredWidth());
        popup.setWidth(finalWidth);

        // Apple tvOS drop-in animation
        panel.setAlpha(0f);
        panel.setPivotY(0f);
        panel.setScaleY(0.92f);
        panel.setTranslationY(-8 * density);
        popup.showAsDropDown(anchor, 0, (int) (10 * density));
        panel.animate().alpha(1f).scaleY(1f).translationY(0f).setDuration(220)
                .setInterpolator(new DecelerateInterpolator(1.6f)).start();

        if (rows.getChildCount() > 0) {
            View first = rows.getChildAt(0);
            if (first instanceof LinearLayout && first.isFocusable()) {
                first.requestFocus();
            } else if (rows.getChildCount() > 1 && rows.getChildAt(1).isFocusable()) {
                rows.getChildAt(1).requestFocus();
            } else {
                btnPairNew.requestFocus();
            }
        } else {
            btnPairNew.requestFocus();
        }
    }

    private static void addSectionHeader(Context context, LinearLayout container, String title) {
        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setTextColor(0x8CFFFFFF);
        tv.setTextSize(11f);
        tv.setLetterSpacing(0.16f);
        tv.setAllCaps(true);
        tv.setFocusable(false);
        try {
            tv.setTypeface(ResourcesCompat.getFont(context, R.font.outfit_medium));
        } catch (Exception ignored) {}
        float density = context.getResources().getDisplayMetrics().density;
        tv.setPadding((int) (12 * density), (int) (10 * density), (int) (12 * density), (int) (4 * density));
        container.addView(tv);
    }

    private static void addSeparator(LinearLayout container, float density) {
        View line = new View(container.getContext());
        line.setBackgroundColor(0x1FFFFFFF);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) density);
        int m = (int) (6 * density), h = (int) (12 * density);
        lp.setMargins(h, m, h, m);
        container.addView(line, lp);
    }

    private static void addDeviceRow(MainActivity activity, LinearLayout container,
                                     PopupWindow popup, BluetoothHelper helper,
                                     BluetoothHelper.DeviceInfo d, boolean isConnected, float density) {
        View row = activity.getLayoutInflater().inflate(R.layout.item_bluetooth_device, container, false);

        ImageView icon = row.findViewById(R.id.deviceIcon);
        TextView name = row.findViewById(R.id.deviceName);
        TextView type = row.findViewById(R.id.deviceType);
        View dot = row.findViewById(R.id.statusDot);
        TextView status = row.findViewById(R.id.statusText);

        icon.setImageResource(d.getIconRes());
        name.setText(d.name);
        type.setText(d.getCategoryLabel());

        if (isConnected) {
            dot.setVisibility(View.VISIBLE);
            status.setText(activity.getString(R.string.connected));
            row.setOnClickListener(v -> {
                if (d.isRemote) {
                    Toast.makeText(activity, d.name + " is your TV Remote", Toast.LENGTH_SHORT).show();
                    return;
                }
                helper.disconnectDevice(d.device);
                Toast.makeText(activity, "Disconnected " + d.name, Toast.LENGTH_SHORT).show();
                popup.dismiss();
            });
        } else {
            dot.setVisibility(View.GONE);
            status.setText(d.isAudio ? activity.getString(R.string.connect) : "");
            row.setOnClickListener(v -> {
                if (d.isAudio) {
                    Toast.makeText(activity, "Connecting to " + d.name + "...", Toast.LENGTH_SHORT).show();
                    helper.connectDevice(d.device);
                    popup.dismiss();
                } else {
                    BluetoothHelper.launchPairing(activity);
                    popup.dismiss();
                }
            });
        }

        // Long press to unpair / forget accessory
        row.setOnLongClickListener(v -> {
            if (d.isRemote) {
                Toast.makeText(activity, "Cannot unpair TV Remote here", Toast.LENGTH_SHORT).show();
                return true;
            }
            helper.unpairDevice(d.device);
            Toast.makeText(activity, "Unpaired " + d.name, Toast.LENGTH_SHORT).show();
            popup.dismiss();
            return true;
        });

        row.setOnFocusChangeListener((v, hasFocus) -> {
            int primary = hasFocus ? 0xFF111111 : 0xFFFFFFFF;
            int dim = hasFocus ? 0xFF555555 : 0x8CFFFFFF;
            name.setTextColor(primary);
            type.setTextColor(dim);
            status.setTextColor(dim);
            icon.setColorFilter(primary);
        });

        container.addView(row);
    }

    private static void addStatusRow(MainActivity activity, LinearLayout container,
                                     PopupWindow popup, int iconRes, String title, String subtitle, Runnable action) {
        View row = activity.getLayoutInflater().inflate(R.layout.item_bluetooth_device, container, false);
        ImageView icon = row.findViewById(R.id.deviceIcon);
        TextView name = row.findViewById(R.id.deviceName);
        TextView type = row.findViewById(R.id.deviceType);
        View dot = row.findViewById(R.id.statusDot);
        TextView status = row.findViewById(R.id.statusText);

        icon.setImageResource(iconRes);
        name.setText(title);
        type.setText(subtitle != null ? subtitle : "");
        dot.setVisibility(View.GONE);
        status.setVisibility(View.GONE);

        row.setOnFocusChangeListener((v, hasFocus) -> {
            int primary = hasFocus ? 0xFF111111 : 0xFFFFFFFF;
            int dim = hasFocus ? 0xFF555555 : 0x8CFFFFFF;
            name.setTextColor(primary);
            type.setTextColor(dim);
            icon.setColorFilter(primary);
        });

        if (action != null) {
            row.setOnClickListener(v -> {
                popup.dismiss();
                action.run();
            });
        } else {
            row.setClickable(false);
            row.setFocusable(false);
        }
        container.addView(row);
    }
}
