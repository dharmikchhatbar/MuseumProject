package com.example.museum;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Author: Dharmik Parthiv Chhatbar
 * Year: Spring 2024
 *
 * MainActivity serves as the entry point to the application.
 * It provides buttons to navigate to different functionalities of the app,
 * including NFC, BLE, and QR code scanning.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Initializes the activity and sets up button click listeners to navigate to other activities.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up NFC button click listener
        Button nfcButton = findViewById(R.id.NFC); // Define a Button variable named nfcButton and link it to the NFC button in the layout
        nfcButton.setOnClickListener(new View.OnClickListener() { // Set up a click listener for the NFC button
            @Override
            public void onClick(View v) { // Define what happens when the NFC button is clicked
                // Create an Intent to start the NFCTypeSelectionActivity
                Intent intent = new Intent(MainActivity.this, NFCTypeSelectionActivity.class); // Create an Intent to navigate from MainActivity to NFCTypeSelectionActivity
                startActivity(intent); // Start the NFCTypeSelectionActivity
            }
        });

        // Set up BLE button click listener
        Button BLEButton = findViewById(R.id.BLE); // Define a Button variable named BLEButton and link it to the BLE button in the layout
        BLEButton.setOnClickListener(new View.OnClickListener() { // Set up a click listener for the BLE button
            @Override
            public void onClick(View v) { // Define what happens when the BLE button is clicked
                // Create an Intent to start the BLEList activity
                Intent intent = new Intent(MainActivity.this, BLEList.class); // Create an Intent to navigate from MainActivity to BLEList activity
                startActivity(intent); // Start the BLEList activity
            }
        });

        // Set up QR button click listener
        Button QRButton = findViewById(R.id.QR); // Define a Button variable named QRButton and link it to the QR button in the layout
        QRButton.setOnClickListener(new View.OnClickListener() { // Set up a click listener for the QR button
            @Override
            public void onClick(View v) { // Define what happens when the QR button is clicked
                // Create an Intent to start the QRScanner activity
                Intent intent = new Intent(MainActivity.this, QRScanner.class); // Create an Intent to navigate from MainActivity to QRScanner activity
                startActivity(intent); // Start the QRScanner activity
            }
        });
    }
}
