package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class Dashboard extends AppCompatActivity {
    private TextView cpuInfoTextView, memoryInfoTextView, batteryInfoTextView, sensorInfoTextView, simInfoTextView, networkInfoTextView,header;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        cpuInfoTextView = findViewById(R.id.cpu_data);
        memoryInfoTextView = findViewById(R.id.memory_data);
        batteryInfoTextView = findViewById(R.id.battery_data);
        sensorInfoTextView = findViewById(R.id.sensorData);
        networkInfoTextView = findViewById(R.id.networkData1);

        getCPUInfo();
        getSensorInfo();
        getMemoryInfo();
        getBatteryInfo();
        getNetworkInfo();
    }
    private void getCPUInfo() {
        StringBuilder cpuInfo = new StringBuilder();
        cpuInfo.append("CPU Arch: ").append(System.getProperty("os.arch")).append("\n");
        cpuInfo.append("CPU Cores: ").append(getNumCores()).append("\n");
        cpuInfoTextView.setText(cpuInfo);
    }

    private int getNumCores() {
        try {
            File cpuInfo = new File("/sys/devices/system/cpu/");
            File[] files = cpuInfo.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return Pattern.matches("cpu[0-9]+", pathname.getName());
                }
            });
            assert files != null;
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void getSensorInfo() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ALL);
        StringBuilder sensorInfo = new StringBuilder("");
        if (sensor != null) {
            sensorInfo.append(sensor.getName()).append("\n");
        } else {
            sensorInfo.append("No sensors available.\n");
        }
        sensorInfoTextView.setText(sensorInfo.toString());
    }



    private void getMemoryInfo() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long totalMemory = memoryInfo.totalMem;
        long freeMemory = memoryInfo.availMem;
        long usedMemory = totalMemory - freeMemory;

        String memoryInfoText = "Total Memory: " + formatFileSize(totalMemory) + "\n"
                + "Used Memory: " + formatFileSize(usedMemory) + "\n"
                + "Free Memory: " + formatFileSize(freeMemory);

        memoryInfoTextView.setText(memoryInfoText);
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private void getBatteryInfo() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        // BatteryManager.EXTRA_STATUS
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        // BatteryManager.EXTRA_LEVEL
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        // BatteryManager.EXTRA_SCALE
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPercentage = level / (float) scale * 100;

        String chargingStatus = isCharging ? "Charging" : "Not Charging";
        String batteryInfoText = "Battery Level: " + batteryPercentage + "%\n"
                + "Charging Status: " + chargingStatus;

        batteryInfoTextView.setText(batteryInfoText);
    }

    private void getNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        } else {
            Toast.makeText(this, "Network info not available for API level below 23", Toast.LENGTH_SHORT).show();
            return;
        }

        String networkInfoText = "";

        if (networkCapabilities != null) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                networkInfoText += "Network Type: WIFI\n";
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                networkInfoText += "Network Type: Cellular\n";
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    networkInfoText += "Internet Access: Yes\n";
                } else {
                    networkInfoText += "Internet Access: No\n";
                }
            }
        } else {
            networkInfoText = "No active network";
        }

        networkInfoTextView.setText(networkInfoText);
    }

}