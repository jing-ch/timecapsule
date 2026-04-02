package edu.northeastern.timecapsule.ui.create;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import edu.northeastern.timecapsule.R;

/**
 * Step 1: collects title and media input.
 */
public class CreateStepOneActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private LinearLayout layoutMedia;
    private TextView tvAddMedia;
    private TextView tvAddIcon;
    private FrameLayout layoutMediaPreview;
    private ImageView ivThumbnail;
    private TextView tvBadge;
    private TextView tvTapToChange;
    private EditText etCapsuleTitle;
    private Button btnNextStep;

    /** Stores selected media URIs for passing to the next step */
    private final ArrayList<String> selectedMediaUris = new ArrayList<>();

    /** Launcher for picking multiple image or video files */
    private ActivityResultLauncher<String[]> mediaPickerLauncher;

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
        tvAddMedia = findViewById(R.id.tvAddMedia);
        tvAddIcon = findViewById(R.id.tvAddIcon);
        layoutMediaPreview = findViewById(R.id.layoutMediaPreview);
        ivThumbnail = findViewById(R.id.ivThumbnail);
        tvBadge = findViewById(R.id.tvBadge);
        tvTapToChange = findViewById(R.id.tvTapToChange);
        etCapsuleTitle = findViewById(R.id.etCapsuleTitle);
        btnNextStep = findViewById(R.id.btnNextStep);
    }

    /** Initializes media picker launcher */
    private void initLaunchers() {
        mediaPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        selectedMediaUris.clear();
                        for (Uri uri : uris) {
                            selectedMediaUris.add(uri.toString());
                        }

                        int count = selectedMediaUris.size();

                        // hide empty state, show thumbnail + hint
                        tvAddIcon.setVisibility(View.GONE);
                        tvAddMedia.setVisibility(View.GONE);
                        layoutMediaPreview.setVisibility(View.VISIBLE);
                        tvTapToChange.setVisibility(View.VISIBLE);

                        // load first image as thumbnail
                        Picasso.get()
                                .load(Uri.parse(selectedMediaUris.get(0)))
                                .fit()
                                .centerCrop()
                                .into(ivThumbnail);

                        // show badge only for multiple selections
                        if (count > 1) {
                            tvBadge.setText("x" + count);
                            tvBadge.setVisibility(View.VISIBLE);
                        } else {
                            tvBadge.setVisibility(View.GONE);
                        }
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

    /** Opens system picker to select an image or video */
    private void openMediaPicker() {
        Toast.makeText(this, "Long press to select multiple files", Toast.LENGTH_SHORT).show();
        mediaPickerLauncher.launch(new String[]{"image/*", "video/*"});
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

}