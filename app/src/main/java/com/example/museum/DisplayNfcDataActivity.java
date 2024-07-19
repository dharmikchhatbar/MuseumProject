package com.example.museum;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Author: Dharmik Parthiv Chhatbar
 * Year: Spring 2024
 *
 * DisplayNfcDataActivity is responsible for handling NFC interactions within the application.
 * It reads NDEF messages from NFC tags, parses the data, and displays relevant information
 * such as images, text, URLs, and video links. The activity uses Glide for image loading
 * and provides functionality to open URLs in a web browser.
 */

public class DisplayNfcDataActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private boolean isSearching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_nfc_data);

        // Initialize views
        ImageView imageView = findViewById(R.id.imageView);
        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView bodyTextView = findViewById(R.id.bodyTextView);
        TextView urlTextView = findViewById(R.id.urlTextView);
        TextView videoLinkTextView = findViewById(R.id.videoLinkTextView);

        // Get the default NFC adapter for this device
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device", Toast.LENGTH_SHORT).show();
            finish(); // Close the app if NFC is not available
            return;
        }

        // Handle the intent that started this activity
        Intent intent = getIntent();
        if (intent != null) {
            handleIntent(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Enable NFC foreground dispatch if searching for NFC tags
        if (isSearching) {
            enableNfcForegroundDispatch();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Disable NFC foreground dispatch if searching for NFC tags
        if (isSearching) {
            disableNfcForegroundDispatch();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Handle new NFC intent
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // Check if the intent action is NDEF_DISCOVERED
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                Ndef ndef = Ndef.get(tag);
                if (ndef == null) {
                    Toast.makeText(this, "NDEF is not supported by this Tag.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the NDEF message from the tag
                NdefMessage ndefMessage = ndef.getCachedNdefMessage();
                if (ndefMessage != null) {
                    for (NdefRecord ndefRecord : ndefMessage.getRecords()) {
                        if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                            try {
                                // Read text from the NDEF record
                                String nfcData = readText(ndefRecord);
                                // Parse and display the NFC data
                                parseNfcData(nfcData);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private String readText(NdefRecord record) throws UnsupportedEncodingException {
        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

    private void parseNfcData(String nfcData) {
        ImageView imageView = findViewById(R.id.imageView);
        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView bodyTextView = findViewById(R.id.bodyTextView);
        TextView urlTextView = findViewById(R.id.urlTextView);
        TextView videoLinkTextView = findViewById(R.id.videoLinkTextView);

        try {
            // Parse the NFC data as a JSON object
            JSONObject jsonObject = new JSONObject(nfcData);
            String imageLink = jsonObject.getString("ImageLink");
            String title = jsonObject.getString("Title");
            String body = jsonObject.getString("Body");
            String url = jsonObject.getString("URL");
            String videoLink = jsonObject.getString("VideoLink");

            // Load image using Glide
            Glide.with(this).load(imageLink).into(imageView);

            // Set text for TextViews
            titleTextView.setText(title);
            bodyTextView.setText(body);
            urlTextView.setText(url);
            urlTextView.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            });
            videoLinkTextView.setText(videoLink);
            videoLinkTextView.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoLink));
                startActivity(browserIntent);
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void enableNfcForegroundDispatch() {
        Intent intent = new Intent(this, DisplayNfcDataActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
        IntentFilter[] intentFilters = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    private void disableNfcForegroundDispatch() {
        nfcAdapter.disableForegroundDispatch(this);
    }
}
