package edu.northeastern.timecapsule.ui.create;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import edu.northeastern.timecapsule.R;

/**
 * Step 2: collects message and location input.
 */
public class CreateStepTwoActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etMessage;
    private TextView tvCount;
    private TextView tvLocation;
    private TextView tvRemove;
    private Button btnNextStep;

    /** Title passed from Step 1 */
    private String title;

    /** Media URIs passed from Step 1 */
    private ArrayList<String> mediaUris;

    /** User-entered location, null if not set */
    private String userLocation = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_step_two);

        readIntentData();
        initViews();
        setupMessageBox();
        setupListeners();
        updateCharacterCount();
    }

    /** Reads data from the previous step */
    private void readIntentData() {
        Intent intent = getIntent();
        title = intent.getStringExtra("capsule_title");
        mediaUris = intent.getStringArrayListExtra("capsule_media_uris");

        if (mediaUris == null) {
            mediaUris = new ArrayList<>();
        }
    }

    /** Binds UI elements from XML */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etMessage = findViewById(R.id.etMessage);
        tvCount = findViewById(R.id.tvCount);
        tvLocation = findViewById(R.id.tvLocation);
        tvRemove = findViewById(R.id.tvRemove);
        btnNextStep = findViewById(R.id.btnNextStep);
    }

    /** Configures scrolling behavior and input limits for the message box */
    @SuppressLint("ClickableViewAccessibility")
    private void setupMessageBox() {
        etMessage.setVerticalScrollBarEnabled(true);
        etMessage.setMovementMethod(new ScrollingMovementMethod());
        etMessage.setHorizontallyScrolling(false);

        etMessage.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        etMessage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});
    }

    /** Sets up button and text listeners */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCharacterCount();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });

        tvLocation.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("e.g. Tokyo, Japan");
            if (userLocation != null) input.setText(userLocation);

            new AlertDialog.Builder(this)
                    .setTitle("Enter location")
                    .setView(input)
                    .setPositiveButton("OK", (dialog, which) -> {
                        String entered = input.getText().toString().trim();
                        if (!entered.isEmpty()) {
                            userLocation = entered;
                            tvLocation.setText(userLocation);
                            tvLocation.setTextColor(Color.parseColor("#444444"));
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        tvRemove.setOnClickListener(v -> {
            userLocation = null;
            tvLocation.setText(R.string.enter_a_location);
            tvLocation.setTextColor(Color.parseColor("#444444"));
        });

        btnNextStep.setOnClickListener(v -> goToStepThree());
    }

    /** Updates the message character counter */
    @SuppressLint("SetTextI18n")
    private void updateCharacterCount() {
        int length = etMessage.getText().toString().length();
        tvCount.setText(length + "/1000");
        tvCount.setTextColor(length > 900 ? Color.RED : Color.parseColor("#C8C8C8"));
    }

    /** Validates input and navigates to Step 3 */
    private void goToStepThree() {
        String message = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            etMessage.setError("Please enter a message");
            etMessage.requestFocus();
            return;
        }

        Intent intent = new Intent(CreateStepTwoActivity.this, CreateStepThreeActivity.class);
        intent.putExtra("capsule_title", title);
        intent.putStringArrayListExtra("capsule_media_uris", mediaUris);
        intent.putExtra("capsule_message", message);
        intent.putExtra("capsule_location", userLocation);
        startActivity(intent);
    }
}