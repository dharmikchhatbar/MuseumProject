package com.example.museum;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Dharmik Parthiv Chhatbar
 * Year: Spring 2024
 *
 * BLEList displays a list of nearby Bluetooth Low Energy (BLE) devices
 * that start with the name "BlueCharm". It allows the user to scan for
 * nearby BLE devices and shows their name, address, RSSI (signal strength),
 * and approximate distance from the device.
 */
public class BLEList extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int SCAN_INTERVAL_MS = 10000; // 10 seconds
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> deviceList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Set<String> uniqueAddresses = new HashSet<>();
    private Handler handler = new Handler();
    private Runnable scanRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_list_item);

        // Check if the device supports Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if Bluetooth is enabled, if not, request to enable it
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Set up the button click listener to start scanning
        Button startScanButton = findViewById(R.id.scanButton);
        startScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPeriodicScan();
            }
        });

        // Set up the list adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        ListView deviceListView = findViewById(android.R.id.list);
        deviceListView.setAdapter(adapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        // Initialize the scan runnable
        scanRunnable = new Runnable() {
            @Override
            public void run() {
                startScan();
                handler.postDelayed(this, SCAN_INTERVAL_MS);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the broadcast receiver
        unregisterReceiver(receiver);
        // Stop the periodic scan
        handler.removeCallbacks(scanRunnable);
    }

    // Start BLE device scan
    private void startScan() {
        // Clear the previous list and unique addresses set
        deviceList.clear();
        uniqueAddresses.clear();
        adapter.notifyDataSetChanged();

        // Start discovery to find nearby devices
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, REQUEST_ENABLE_BT);
            return;
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    // Start periodic BLE device scan
    private void startPeriodicScan() {
        handler.post(scanRunnable);
    }

    // BroadcastReceiver to listen for discovered devices
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Check if the device is already in the set
                if (uniqueAddresses.add(device.getAddress())) {
                    // Check if the device name starts with "BlueCharm"
                    if (device.getName() != null && device.getName().startsWith("BlueCharm")) {
                        // Get the RSSI (signal strength)
                        int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                        // Estimate distance based on RSSI (simple linear approximation)
                        double distance = calculateDistance(rssi);

                        // Build device info string
                        String deviceInfo = "Name: " + device.getName() + "\n"
                                + "Address: " + device.getAddress() + "\n"
                                + "RSSI: " + rssi + " dBm\n"
                                + "Approximate Distance: " + String.format("%.2f meters", distance);

                        // Add device info to the list
                        deviceList.add(deviceInfo);

                        // Update the UI
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }

        // Method to calculate distance based on RSSI (simple linear approximation)
        private double calculateDistance(int rssi) {
            // Use a simple linear approximation for distance estimation
            // This is a simplified model and may not be accurate in all scenarios
            // You might need to calibrate this based on your specific use case

            // Convert RSSI to meters using a linear approximation
            // Distance = 10 ^ ((Measured Power - RSSI) / (10 * n))
            // n is the path loss exponent (typically ranging from 2 to 4)

            double measuredPower = -72; // Measured Power at 1 meter, adjust based on your measurements
            double pathLossExponent = 2.4; // Path loss exponent, adjust based on your environment

            return Math.pow(10, ((measuredPower - rssi) / (10 * pathLossExponent)));
        }
    };
}
