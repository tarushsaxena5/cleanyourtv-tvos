package com.gathin.tvhome;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Manages Bluetooth adapter state, profiles (A2DP and Headset), connection tracking,
 * and device actions for the Apple tvOS top bar pill and quick-menu popup.
 */
public class BluetoothHelper {

    public interface Callback {
        void onBluetoothStateChanged();
    }

    public static class DeviceInfo {
        public final BluetoothDevice device;
        public final String name;
        public final String address;
        public final boolean isConnected;
        public final boolean isAudio;
        public final boolean isRemote;

        public DeviceInfo(BluetoothDevice device, String name, String address,
                          boolean isConnected, boolean isAudio, boolean isRemote) {
            this.device = device;
            this.name = name != null && !name.trim().isEmpty() ? name : "Bluetooth Accessory";
            this.address = address != null ? address : "";
            this.isConnected = isConnected;
            this.isAudio = isAudio;
            this.isRemote = isRemote;
        }

        public int getIconRes() {
            if (isAudio) return R.drawable.ic_headset;
            if (isRemote) return R.drawable.ic_remote;
            return isConnected ? R.drawable.ic_bluetooth_connected : R.drawable.ic_bluetooth;
        }

        public String getCategoryLabel() {
            if (isAudio) return "Audio / Headphones";
            if (isRemote) return "Remote Control";
            return "Accessory";
        }
    }

    private final Context context;
    private final Callback callback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final BluetoothAdapter adapter;

    private BluetoothA2dp a2dpProfile;
    private BluetoothHeadset headsetProfile;
    private boolean receiverRegistered = false;

    private final BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                a2dpProfile = (BluetoothA2dp) proxy;
            } else if (profile == BluetoothProfile.HEADSET) {
                headsetProfile = (BluetoothHeadset) proxy;
            }
            notifyChanged();
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
                a2dpProfile = null;
            } else if (profile == BluetoothProfile.HEADSET) {
                headsetProfile = null;
            }
            notifyChanged();
        }
    };

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            notifyChanged();
        }
    };

    public BluetoothHelper(Context context, Callback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;
        BluetoothAdapter defaultAdapter = null;
        try {
            defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        } catch (Exception ignored) {}
        this.adapter = defaultAdapter;
    }

    public void start() {
        if (adapter == null) return;

        try {
            adapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP);
            adapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);
        } catch (Exception ignored) {}

        if (!receiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
            filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
            try {
                context.registerReceiver(btReceiver, filter);
                receiverRegistered = true;
            } catch (Exception ignored) {}
        }
    }

    public void stop() {
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(btReceiver);
            } catch (Exception ignored) {}
            receiverRegistered = false;
        }

        if (adapter != null) {
            try {
                if (a2dpProfile != null) adapter.closeProfileProxy(BluetoothProfile.A2DP, a2dpProfile);
                if (headsetProfile != null) adapter.closeProfileProxy(BluetoothProfile.HEADSET, headsetProfile);
            } catch (Exception ignored) {}
            a2dpProfile = null;
            headsetProfile = null;
        }
    }

    private void notifyChanged() {
        mainHandler.post(() -> {
            if (callback != null) callback.onBluetoothStateChanged();
        });
    }

    public boolean isBluetoothEnabled() {
        try {
            return adapter != null && adapter.isEnabled();
        } catch (SecurityException ignored) {
            return false;
        }
    }

    public boolean isDeviceConnected(BluetoothDevice device) {
        if (device == null) return false;
        try {
            if (a2dpProfile != null && a2dpProfile.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED) {
                return true;
            }
        } catch (SecurityException ignored) {}

        try {
            if (headsetProfile != null && headsetProfile.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED) {
                return true;
            }
        } catch (SecurityException ignored) {}

        try {
            Method isConnectedMethod = device.getClass().getMethod("isConnected");
            Object res = isConnectedMethod.invoke(device);
            if (res instanceof Boolean && (Boolean) res) {
                return true;
            }
        } catch (Exception ignored) {}

        return false;
    }

    public static boolean isRemoteDevice(BluetoothDevice device) {
        if (device == null) return false;
        try {
            String name = device.getName();
            if (name != null) {
                String lower = name.toLowerCase(Locale.ROOT);
                if (lower.contains("remote") || lower.contains("controller")) return true;
            }
        } catch (SecurityException ignored) {}

        try {
            BluetoothClass btClass = device.getBluetoothClass();
            if (btClass != null) {
                int dc = btClass.getDeviceClass();
                if (dc == BluetoothClass.Device.PERIPHERAL_KEYBOARD
                        || dc == BluetoothClass.Device.PERIPHERAL_POINTING
                        || dc == BluetoothClass.Device.PERIPHERAL_KEYBOARD_POINTING) {
                    return true;
                }
            }
        } catch (SecurityException ignored) {}

        return false;
    }

    public static boolean isAudioDevice(BluetoothDevice device) {
        if (device == null) return false;
        try {
            BluetoothClass btClass = device.getBluetoothClass();
            if (btClass != null && btClass.getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO) {
                return true;
            }
        } catch (SecurityException ignored) {}

        try {
            String name = device.getName();
            if (name != null) {
                String lower = name.toLowerCase(Locale.ROOT);
                if (lower.contains("headset") || lower.contains("headphone") || lower.contains("earphone")
                        || lower.contains("buds") || lower.contains("airpod") || lower.contains("speaker")
                        || lower.contains("soundbar") || lower.contains("audio") || lower.contains("thunder")
                        || lower.contains("rockerz") || lower.contains("boat") || lower.contains("sony")) {
                    return true;
                }
            }
        } catch (SecurityException ignored) {}

        return false;
    }

    /** Returns all bonded devices separated into connected and saved. */
    public List<DeviceInfo> getAllBondedDevices() {
        if (adapter == null) return Collections.emptyList();
        Set<BluetoothDevice> bonded;
        try {
            bonded = adapter.getBondedDevices();
        } catch (SecurityException ignored) {
            return Collections.emptyList();
        }
        if (bonded == null || bonded.isEmpty()) return Collections.emptyList();

        List<DeviceInfo> list = new ArrayList<>();
        for (BluetoothDevice device : bonded) {
            String name = null;
            try { name = device.getName(); } catch (SecurityException ignored) {}
            String addr = device.getAddress();
            boolean connected = isDeviceConnected(device);
            boolean audio = isAudioDevice(device);
            boolean remote = isRemoteDevice(device);
            list.add(new DeviceInfo(device, name, addr, connected, audio, remote));
        }

        // Sort: connected first, then audio devices, then others
        list.sort((a, b) -> {
            if (a.isConnected != b.isConnected) return a.isConnected ? -1 : 1;
            if (a.isAudio != b.isAudio) return a.isAudio ? -1 : 1;
            return a.name.compareToIgnoreCase(b.name);
        });

        return list;
    }

    /**
     * Determines what device should be showcased on the top-bar pill.
     * Prioritizes active audio accessories (headphones/speakers), then active accessories.
     * Returns null if idle or only the TV remote is active.
     */
    public DeviceInfo getActivePillDevice() {
        List<DeviceInfo> all = getAllBondedDevices();
        for (DeviceInfo d : all) {
            if (d.isConnected && d.isAudio) return d;
        }
        for (DeviceInfo d : all) {
            if (d.isConnected && !d.isRemote) return d;
        }
        return null;
    }

    /** Connects to an audio accessory via A2DP/Headset reflection. */
    public void connectDevice(BluetoothDevice device) {
        if (device == null) return;
        boolean invoked = false;
        if (a2dpProfile != null) {
            try {
                Method m = a2dpProfile.getClass().getMethod("connect", BluetoothDevice.class);
                m.invoke(a2dpProfile, device);
                invoked = true;
            } catch (Exception ignored) {}
        }
        if (headsetProfile != null) {
            try {
                Method m = headsetProfile.getClass().getMethod("connect", BluetoothDevice.class);
                m.invoke(headsetProfile, device);
                invoked = true;
            } catch (Exception ignored) {}
        }
        if (!invoked) {
            launchPairing(context);
        }
    }

    /** Disconnects an audio accessory via reflection. */
    public void disconnectDevice(BluetoothDevice device) {
        if (device == null) return;
        if (a2dpProfile != null) {
            try {
                Method m = a2dpProfile.getClass().getMethod("disconnect", BluetoothDevice.class);
                m.invoke(a2dpProfile, device);
            } catch (Exception ignored) {}
        }
        if (headsetProfile != null) {
            try {
                Method m = headsetProfile.getClass().getMethod("disconnect", BluetoothDevice.class);
                m.invoke(headsetProfile, device);
            } catch (Exception ignored) {}
        }
    }

    /** Unpairs / forgets a bonded device. */
    public void unpairDevice(BluetoothDevice device) {
        if (device == null) return;
        try {
            Method m = device.getClass().getMethod("removeBond");
            m.invoke(device);
        } catch (Exception ignored) {}
    }

    /** Launches the Android TV accessory pairing radar screen. */
    public static void launchPairing(Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.accessories.AddAccessoryActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        } catch (Exception ignored) {}

        try {
            Intent intent = new Intent("com.android.tv.settings.accessories.AccessoryActivity");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        } catch (Exception ignored) {}

        try {
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        } catch (Exception ignored) {}

        try {
            Intent intent = new Intent("android.bluetooth.devicepicker.action.LAUNCH");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ignored) {}
    }
}
