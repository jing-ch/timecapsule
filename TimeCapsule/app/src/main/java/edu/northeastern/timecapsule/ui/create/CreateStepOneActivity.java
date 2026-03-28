package edu.northeastern.timecapsule.ui.create;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.timecapsule.R;

/**
 * Step 1: collects title and media input.
 */
public class CreateStepOneActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private LinearLayout layoutMedia;
    private EditText etCapsuleTitle;
    private Button btnNextStep;

    /** Stores selected media URIs for passing to the next step */
    private final ArrayList<String> selectedMediaUris = new ArrayList<>();

    /** Launcher for picking a media file */
    private ActivityResultLauncher<String> mediaPickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_step_one);

        initViews();
        initLaunchers();
        setupListeners();
    }

    /** Binds UI elements from XML */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutMedia = findViewById(R.id.layoutMedia);
        etCapsuleTitle = findViewById(R.id.etCapsuleTitle);
        btnNextStep = findViewById(R.id.btnNextStep);
    }

    /** Initializes media picker launcher */
    private void initLaunchers() {
        mediaPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedMediaUris.add(uri.toString());

                        Toast.makeText(this, "1 file selected", Toast.LENGTH_SHORT).show();

                        // TODO: display selected media preview or count on UI
                        // TODO: support selecting multiple media files
                    }
                }
        );
    }

    /** Sets up button click listeners */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        layoutMedia.setOnClickListener(v -> openMediaPicker());

        btnNextStep.setOnClickListener(v -> goToStepTwo());
    }

    /** Opens system picker to select media */
    private void openMediaPicker() {
        mediaPickerLauncher.launch("*/*");

        // TODO: restrict file type (e.g., "image/*" or "video/*")
        // TODO: implement multi-selection support
        // TODO: upload media to Firebase Storage in final step
    }

    /** Validates input and navigates to Step 2 */
    private void goToStepTwo() {
        String title = etCapsuleTitle.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etCapsuleTitle.setError("Please enter a title");
            etCapsuleTitle.requestFocus();
            return;
        }

        Intent intent = new Intent(CreateStepOneActivity.this, CreateStepTwoActivity.class);
        intent.putExtra("capsule_title", title);
        intent.putStringArrayListExtra("capsule_media_uris", selectedMediaUris);
        startActivity(intent);
    }

    /** Adds a single media URI */
    private void addMediaUri(Uri uri) {
        if (uri == null) return;
        selectedMediaUris.add(uri.toString());
    }

    /** Replaces all selected media */
    private void setSelectedMedia(List<Uri> uris) {
        selectedMediaUris.clear();
        if (uris == null) return;

        for (Uri uri : uris) {
            if (uri != null) {
                selectedMediaUris.add(uri.toString());
            }
        }
    }
}