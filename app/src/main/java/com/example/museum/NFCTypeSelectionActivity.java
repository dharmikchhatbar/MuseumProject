package com.example.museum;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**

 Author: Dharmik Parthiv Chhatbar

 Year: Spring 2024

 NFCTypeSelectionActivity allows the user to select the type of NFC interaction,

 either NFC Text or NFC JSON, and navigates to the respective activity.
 */
public class NFCTypeSelectionActivity extends AppCompatActivity {

    NFC nfc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_type_selection_activity);

        // Initialize NFC object
        nfc = new NFC(this);

        // Pop-up message at the start of the application
        nfc.showNfcAlertDialog();

        // Set up NFC Text button click listener
        Button nfcTextButton = findViewById(R.id.NFCText);
        nfcTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the NFCTextActivity
                Intent intent = new Intent(NFCTypeSelectionActivity.this, NFCTextActivity.class);
                startActivity(intent);
            }
        });

        // Set up NFC JSON button click listener
        Button nfcJsonButton = findViewById(R.id.NFCJson);
        nfcJsonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the NFCJsonActivity
                Intent intent = new Intent(NFCTypeSelectionActivity.this, NFCJsonActivity.class);
                startActivity(intent);
            }
        });
    }
}
