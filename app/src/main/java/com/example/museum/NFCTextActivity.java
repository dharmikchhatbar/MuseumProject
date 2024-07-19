package com.example.museum;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Author: Dharmik Parthiv Chhatbar
 * Year: Spring 2024
 *
 * NFCTextActivity allows users to write text to NFC tags and read text from NFC tags.
 * It provides functionalities like writing text to tags, reading text from tags,
 * enabling NFC read mode, and disabling NFC read mode.
 */
public class NFCTextActivity extends AppCompatActivity {

    TextView nfc_contents;

    Button writeButton;
    TextView editText;
    NFC nfc;

    /**
     * Initializes the activity and sets up NFC-related components.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_text_activity);
        nfc_contents = findViewById(R.id.nfc_contents);
        writeButton = findViewById(R.id.writeButton);
        editText = findViewById(R.id.editText);

        nfc = new NFC(this);

        // Set up write button click listener
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Write text to NFC tag
                String textToWrite = editText.getText().toString();
                if (!textToWrite.isEmpty()) {
                    nfc.writeNFC(textToWrite);
                } else {
                    Toast.makeText(NFCTextActivity.this, "Please enter text to write to NFC tag", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Enables NFC read mode when the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        nfc.enableForegroundDispatch(this);
    }

    /**
     * Disables NFC read mode when the activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();
        nfc.disableForegroundDispatch(this);
    }

    /**
     * Handles new NFC intents.
     *
     * @param intent The new intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Handle NFC read
        nfc.readNFC(intent);
    }
}

