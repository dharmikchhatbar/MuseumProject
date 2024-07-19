package com.example.museum;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: Dharmik Parthiv Chhatbar
 * Year: Spring 2024
 *
 * NFCJsonActivity is responsible for writing JSON data to NFC tags.
 * It allows users to input data into text fields, which is then
 * converted into a JSON object and written to an NFC tag upon detection.
 * The activity handles NFC tag discovery, writing operations, and provides
 * feedback to the user via Toast messages.
 */

public class NFCJsonActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] writingTagFilters;
    private Tag myTag;

    private EditText editID, editTitle, editBody, editURL, editImageLink, editVideoLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_json_activity);

        // Initialize UI components
        editID = findViewById(R.id.editID);
        editTitle = findViewById(R.id.editTitle);
        editBody = findViewById(R.id.editBody);
        editURL = findViewById(R.id.editURL);
        editImageLink = findViewById(R.id.editImageLink);
        editVideoLink = findViewById(R.id.editVideoLink);

        // Get the default NFC adapter for this device
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            showNfcSettingsDialog();
        }

        int flagForNfc = 0;
        // Initialize pending intent for NFC operations
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), flagForNfc | PendingIntent.FLAG_MUTABLE);
        // Set up intent filter for detecting NFC tags
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writingTagFilters = new IntentFilter[]{tagDetected};

        // Set up the button to write JSON data to NFC tag
        Button writeButton = findViewById(R.id.writeButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeJsonToTag(myTag);
            }
        });
    }

    // Show a dialog if NFC is not available on the device
    private void showNfcSettingsDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("NFC Not Available");
        alertDialogBuilder.setMessage("This device doesn't support NFC. This feature won't work.");
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // Write JSON data to the detected NFC tag
    private void writeJsonToTag(Tag tag) {
        if (tag == null) {
            Toast.makeText(this, "NFC tag is null", Toast.LENGTH_SHORT).show();
        } else {
            // Get input data from text fields
            String id = editID.getText().toString();
            String title = editTitle.getText().toString();
            String body = editBody.getText().toString();
            String url = editURL.getText().toString();
            String imageLink = editImageLink.getText().toString();
            String videoLink = editVideoLink.getText().toString();

            // Create a JSON object with the input data
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("ID", id);
                jsonObject.put("Title", title);
                jsonObject.put("Body", body);
                jsonObject.put("URL", url);
                jsonObject.put("ImageLink", imageLink);
                jsonObject.put("VideoLink", videoLink);

                // Convert the JSON object to a string and create an NDEF record
                String jsonString = jsonObject.toString();
                NdefRecord[] records = {createRecord(jsonString)};
                NdefMessage message = new NdefMessage(records);

                // Write the NDEF message to the NFC tag
                Ndef ndef = Ndef.get(tag);
                if (ndef != null) {
                    try {
                        ndef.connect();
                        ndef.writeNdefMessage(message);
                        Toast.makeText(this, "JSON written to NFC tag", Toast.LENGTH_SHORT).show();
                    } catch (IOException | FormatException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error writing JSON to NFC tag", Toast.LENGTH_SHORT).show();
                    } finally {
                        ndef.close();
                    }
                } else {
                    // Check if the tag is NdefFormatable and format it if possible
                    NdefFormatable ndefFormatable = NdefFormatable.get(tag);
                    if (ndefFormatable != null) {
                        try {
                            ndefFormatable.connect();
                            ndefFormatable.format(message);
                            Toast.makeText(this, "JSON written to NFC tag", Toast.LENGTH_SHORT).show();
                        } catch (IOException | FormatException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error formatting NFC tag", Toast.LENGTH_SHORT).show();
                        } finally {
                            ndefFormatable.close();
                        }
                    } else {
                        Toast.makeText(this, "NFC tag is not writable", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating JSON", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    // Create an NDEF record from a text string
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // Handle the new NFC intent and retrieve the tag
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // Optionally, you can handle tag discovery events here
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatch();
    }

    // Enable foreground dispatch to handle NFC intents while the activity is in the foreground
    private void enableForegroundDispatch() {
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, writingTagFilters, null);
        }
    }

    // Disable foreground dispatch when the activity is not in the foreground
    private void disableForegroundDispatch() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }
}