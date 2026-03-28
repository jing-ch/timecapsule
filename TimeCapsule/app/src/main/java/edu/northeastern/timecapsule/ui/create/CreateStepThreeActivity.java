package edu.northeastern.timecapsule.ui.create;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import edu.northeastern.timecapsule.R;
import edu.northeastern.timecapsule.model.Capsule;
import edu.northeastern.timecapsule.viewmodel.CreateCapsuleViewModel;

/**
 * Step 3: collects final settings and submits the capsule.
 */
public class CreateStepThreeActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private LinearLayout layoutDateTime;
    private LinearLayout btnPrivate;
    private LinearLayout btnPublic;
    private Button btnCreate;
    private TextView tvDateTime;

    /** Stores the user-selected unlock date and time */
    private Date selectedUnlockDate = null;

    /** ViewModel for capsule creation */
    private CreateCapsuleViewModel viewModel;

    /** Title passed from previous steps */
    private String title;

    /** Message passed from previous steps */
    private String message;

    /** Location passed from previous steps */
    private String location;

    /** Media URIs passed from previous steps */
    private ArrayList<String> mediaUris;

    /** Current privacy selection */
    private boolean isPrivate = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_step_three);

        readIntentData();
        initViews();
        initViewModel();
        setupListeners();
        observeViewModel();
    }

    /** Reads data from the previous step */
    private void readIntentData() {
        Intent intent = getIntent();
        title = intent.getStringExtra("capsule_title");
        message = intent.getStringExtra("capsule_message");
        location = intent.getStringExtra("capsule_location");
        mediaUris = intent.getStringArrayListExtra("capsule_media_uris");

        if (mediaUris == null) {
            mediaUris = new ArrayList<>();
        }
    }

    /** Binds UI elements from XML */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutDateTime = findViewById(R.id.layoutDateTime);
        btnPrivate = findViewById(R.id.btnPrivate);
        btnPublic = findViewById(R.id.btnPublic);
        btnCreate = findViewById(R.id.btnCreate);
        tvDateTime = findViewById(R.id.tvDateTime);
    }

    /** Initializes ViewModel */
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(CreateCapsuleViewModel.class);
    }

    /** Sets up click listeners */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPrivate.setOnClickListener(v -> {
            isPrivate = true;
            updatePrivacySelection();
        });

        btnPublic.setOnClickListener(v -> {
            isPrivate = false;
            updatePrivacySelection();
        });

        btnCreate.setOnClickListener(v -> createCapsule());

        layoutDateTime.setOnClickListener(v -> openDateTimePicker());
    }

    /** Opens MaterialDatePicker, then TimePickerDialog to select unlock date and time */
    private void openDateTimePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select unlock date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraints)
                .build();

        datePicker.addOnPositiveButtonClickListener(dateMillis -> {
            Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            utcCal.setTimeInMillis(dateMillis);
            int year = utcCal.get(Calendar.YEAR);
            int month = utcCal.get(Calendar.MONTH);
            int day = utcCal.get(Calendar.DAY_OF_MONTH);

            TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day, hourOfDay, minute, 0);
                selected.set(Calendar.MILLISECOND, 0);
                selectedUnlockDate = selected.getTime();

                SimpleDateFormat fmt = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
                tvDateTime.setText(fmt.format(selectedUnlockDate));
            }, 12, 0, false);

            timePicker.show();
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    /** Observes ViewModel state */
    private void observeViewModel() {
        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Capsule created successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /** Updates selected privacy UI */
    private void updatePrivacySelection() {
        if (isPrivate) {
            btnPrivate.setAlpha(1.0f);
            btnPublic.setAlpha(0.6f);
            Toast.makeText(this, "Private selected", Toast.LENGTH_SHORT).show();
        } else {
            btnPrivate.setAlpha(0.6f);
            btnPublic.setAlpha(1.0f);
            Toast.makeText(this, "Public selected", Toast.LENGTH_SHORT).show();
        }

        // TODO: replace alpha-based selection with a better selected-state UI
    }

    /** Validates input and submits capsule data */
    private void createCapsule() {
        if (title == null || title.trim().isEmpty()) {
            viewModel.errorMessage.setValue("Missing title");
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            viewModel.errorMessage.setValue("Missing message");
            return;
        }

        if (selectedUnlockDate == null) {
            viewModel.errorMessage.setValue("Please select an unlock date and time");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Capsule capsule = new Capsule();
        capsule.setUserId(userId);
        capsule.setTitle(title);
        capsule.setContent(message);
        capsule.setLocationName((location != null && !location.isEmpty()) ? location : null);
        capsule.setPublic(!isPrivate);
        capsule.setUnlockTime(new Timestamp(selectedUnlockDate));
        capsule.setCreatedAt(Timestamp.now());
        capsule.setUnlocked(false);
        // media upload handled in a later task
        capsule.setMediaUrl(null);
        capsule.setMediaType(null);

        viewModel.saveCapsule(capsule);
    }
}