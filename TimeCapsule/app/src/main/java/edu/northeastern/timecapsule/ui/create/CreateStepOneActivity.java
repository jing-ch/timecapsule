package edu.northeastern.timecapsule.ui.create;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
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

                        // load first file as thumbnail (image or video)
                        Uri firstUri = Uri.parse(selectedMediaUris.get(0));
                        String mimeType = getContentResolver().getType(firstUri);
                        if (mimeType != null && mimeType.startsWith("video")) {
                            // extract first frame from video
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            try {
                                retriever.setDataSource(this, firstUri);
                                Bitmap frame = retriever.getFrameAtTime(0);
                                ivThumbnail.setImageBitmap(frame);
                                ivThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            } catch (Exception e) {
                                ivThumbnail.setImageResource(android.R.drawable.ic_media_play);
                            } finally {
                                try { retriever.release(); } catch (Exception ignored) {}
                            }
                        } else {
                            // Picasso ignores EXIF orientation, so we decode manually and
                            // rotate the bitmap to match the EXIF tag. This prevents photos
                            // from appearing rotated on devices (e.g. emulators) that store
                            // orientation in EXIF rather than baking it into the pixel data.
                            try (java.io.InputStream bitmapStream = getContentResolver().openInputStream(firstUri);
                                 java.io.InputStream exifStream = getContentResolver().openInputStream(firstUri)) {
                                Bitmap bitmap = BitmapFactory.decodeStream(bitmapStream);
                                int rotation = 0;
                                if (exifStream != null) {
                                    ExifInterface exif = new ExifInterface(exifStream);
                                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotation = 90;
                                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotation = 180;
                                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotation = 270;
                                }
                                if (rotation != 0 && bitmap != null) {
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(rotation);
                                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                                }
                                ivThumbnail.setImageBitmap(bitmap);
                                ivThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            } catch (Exception e) {
                                Picasso.get().load(firstUri).fit().centerCrop().into(ivThumbnail);
                            }
                        }

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