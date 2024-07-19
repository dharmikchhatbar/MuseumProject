package com.example.museum;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: Dharmik Parthiv Chhatbar
 * Year: Spring 2024
 *
 * QRScanner allows the user to scan QR codes using the device's camera.
 * It displays the scanned text in a TextView and provides a button to initiate
 * the scanning process again.
 */
public class QRScanner extends AppCompatActivity {

    private TextView scannedText; // TextView to display the scanned text
    private ImageView imageView;
    private TextView titleTextView;
    private TextView bodyTextView;
    private TextView urlTextView;
    private TextView videoLinkTextView;
    private Button scanButton; // Button to initiate the scanning process

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scanner_activity); // Set the layout for the activity

        // Initialize UI components
        imageView = findViewById(R.id.imageView);
        titleTextView = findViewById(R.id.titleTextView);
        bodyTextView = findViewById(R.id.bodyTextView);
        urlTextView = findViewById(R.id.urlTextView);
        videoLinkTextView = findViewById(R.id.videoLinkTextView);
        scanButton = findViewById(R.id.scanButton);

        // Start QR code scanner
        startQRScanner();

        // Set OnClickListener for scan button
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQRScanner(); // Call method to start QR code scanner again
            }
        });
    }

    // Method to start QR code scanner
    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this); // Create IntentIntegrator instance
        integrator.setOrientationLocked(false); // Allow orientation changes during scanning
        integrator.setPrompt("Scan a QR code"); // Set prompt message for the scanner
        integrator.initiateScan(); // Start QR code scanning process
    }

    // Handle the result of QR code scanning
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Parse the result from the QR code scanner
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show(); // Show toast message if scan is cancelled
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result.getContents());
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
                    Toast.makeText(this, "Invalid QR Code format", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
