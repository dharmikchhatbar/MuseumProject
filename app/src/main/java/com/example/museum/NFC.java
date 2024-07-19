package com.example.museum;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Author: Dharmik Parthiv Chhatbar
 * Year: Spring 2024
 *
 * NFC class provides functionalities related to NFC, such as reading and writing NFC tags,
 * enabling and disabling foreground dispatch, and handling NFC intents.
 */
public class NFC {

    // Constants for messages
    public static final String Error_Detected = "No NFC Tag Detected";
    public static final String Write_Success = "Tag Activated";
    public static final String Write_Error = "Error Activating Tag";

    // NFC-related variables
    private NfcAdapter nfcAdapter; // Instance of the NFC adapter
    private Context context; // Context of the application
    private PendingIntent pendingIntent; // PendingIntent to handle NFC intents
    private IntentFilter[] writingTagFilters; // IntentFilters for writing to NFC tags
    private boolean writeMode = false; // Flag to indicate whether NFC write mode is enabled
    private Tag myTag; // The NFC tag being operated on

    // Constructor
    public NFC(Context context) {
        this.context = context;
        nfcAdapter = NfcAdapter.getDefaultAdapter(context); // Get the default NFC adapter for the device

        // Check if NFC is available on the device
        if (nfcAdapter == null) {
            showNfcAlertDialog(); // Show an alert dialog if NFC is not available
        }

        int flagForNfc = 0;

        // Create a PendingIntent for NFC intent
        pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), flagForNfc | PendingIntent.FLAG_MUTABLE);

        // Create an IntentFilter for detecting NFC tag
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writingTagFilters = new IntentFilter[]{tagDetected}; // Set up IntentFilters for writing NFC tags
    }

    // Check if NFC is enabled on the device
    public boolean isNfcEnabled() {
        return nfcAdapter == null || !nfcAdapter.isEnabled();
    }

    // Show a dialog to prompt the user to enable NFC
    public void showNfcAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Message");
        alertDialogBuilder.setMessage("This app uses NFC to read and write data on NFC Tags. Do you want to allow?");
        alertDialogBuilder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isNfcEnabled()) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle("ALERT!");
                    alertDialogBuilder.setMessage("You need to turn on NFC. Click yes to go to NFC Settings");
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            context.startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                        }
                    });
                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // End the Program
                            ((NFCTextActivity) context).finish();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });
        alertDialogBuilder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // End the Program
                ((NFCTextActivity) context).finish();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // Enable foreground dispatch to capture NFC tag
    public void enableForegroundDispatch(NFCTextActivity nfcJsonActivity) {
        if (nfcAdapter != null) {
            writeMode = true;
            nfcAdapter.enableForegroundDispatch((NFCTextActivity) context, pendingIntent, writingTagFilters, null);
        }
    }

    // Disable foreground dispatch for NFC
    public void disableForegroundDispatch(NFCTextActivity nfcJsonActivity) {
        if (nfcAdapter != null) {
            writeMode = false;
            nfcAdapter.disableForegroundDispatch((NFCTextActivity) context);
        }
    }

    // Write data to an NFC tag
    public void writeNFC(String text) {
        try {
            if (myTag == null) {
                Toast.makeText(context, Error_Detected, Toast.LENGTH_SHORT).show();
            } else {
                // Write the content to the NFC tag
                write(text, myTag);
                Toast.makeText(context, Write_Success, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException | FormatException e) {
            Toast.makeText(context, Write_Error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Write JSON data to an NFC tag
    public void writeJsonToNFC(JSONObject jsonObject) {
        try {
            if (myTag == null) {
                Toast.makeText(context, Error_Detected, Toast.LENGTH_SHORT).show();
            } else {
                String jsonString = jsonObject.toString();
                write(jsonString, myTag);
                Toast.makeText(context, Write_Success, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException | FormatException e) {
            Toast.makeText(context, Write_Error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Read data from an NFC tag
    public void readNFC(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }

    // Write NDEF message to an NFC tag
    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = {createRecord(text)};
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }

// Create an NDEF record for the given text
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes(StandardCharsets.US_ASCII);
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
    }

    // Process and display the NFC tag content
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs != null && msgs.length > 0) {
            NdefRecord[] records = msgs[0].getRecords();
            for (NdefRecord record : records) {
                if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        byte[] payload = record.getPayload();
                        // Get the text encoding (1st byte) and language code (next few bytes)
                        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
                        int languageCodeLength = payload[0] & 51;
                        String text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);

                        // Update the nfc_contents TextView with the NFC content
                        ((NFCTextActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((NFCTextActivity) context).nfc_contents.setText(text);
                            }
                        });
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Read JSON data from an NFC tag
    public String readJsonFromTag(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                NdefMessage ndefMessage = ndef.getNdefMessage();
                if (ndefMessage != null) {
                    NdefRecord[] records = ndefMessage.getRecords();
                    if (records.length > 0) {
                        NdefRecord ndefRecord = records[0];
                        return parseTextRecord(ndefRecord);
                    }
                }
                ndef.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Parse text record from NFC tag
    private String parseTextRecord(NdefRecord record) {
        if (record != null && record.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
            try {
                byte[] payload = record.getPayload();
                // Get the text encoding (1st byte) and language code (next few bytes)
                String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
                int languageCodeLength = payload[0] & 51;
                return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}


