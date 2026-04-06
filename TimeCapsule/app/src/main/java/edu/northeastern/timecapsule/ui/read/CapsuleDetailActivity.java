package edu.northeastern.timecapsule.ui.read;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.Timestamp;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.northeastern.timecapsule.R;
import edu.northeastern.timecapsule.model.Capsule;
import edu.northeastern.timecapsule.viewmodel.CapsuleDetailViewModel;

public class CapsuleDetailActivity extends AppCompatActivity {

    private CapsuleDetailViewModel viewModel;

    private TextView titleText;
    private TextView lockedStatusText;
    private TextView countdownText;
    private TextView waitingDotsText;
    private ImageView waitingIcon;
    private View waitingArea;

    private View lockedSection;
    private View unlockedSection;

    private TextView contentText;
    private TextView locationText;
    private ImageView detailImage;
    private VideoView detailVideo;
    private TextView noMediaText;
    private View videoPreviewLayout;
    private View videoContainer;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;
    private Runnable dotsRunnable;
    private long unlockTimeMillis = -1L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capsule_detail);

        ImageButton btnBack = findViewById(R.id.btnBack);
        titleText = findViewById(R.id.tvDetailTitle);

        lockedSection = findViewById(R.id.layoutLockedSection);
        unlockedSection = findViewById(R.id.layoutUnlockedSection);

        lockedStatusText = findViewById(R.id.tvLockedStatus);
        countdownText = findViewById(R.id.tvDetailCountdown);
        waitingDotsText = findViewById(R.id.tvWaitingDots);
        waitingIcon = findViewById(R.id.ivWaiting);
        waitingArea = findViewById(R.id.layoutWaitingArea);

        contentText = findViewById(R.id.tvDetailContent);
        locationText = findViewById(R.id.tvDetailLocation);
        detailImage = findViewById(R.id.ivDetailImage);
        detailVideo = findViewById(R.id.vvDetailVideo);
        noMediaText = findViewById(R.id.tvNoMedia);
        videoPreviewLayout = findViewById(R.id.layoutVideoPreview);
        videoContainer = findViewById(R.id.layoutVideoContainer);

        btnBack.setOnClickListener(v -> finish());

        String capsuleId = resolveCapsuleId();

        viewModel = new ViewModelProvider(this).get(CapsuleDetailViewModel.class);
        viewModel.loadCapsule(capsuleId);

        viewModel.getCapsule().observe(this, capsule -> {
            if (capsule == null) {
                titleText.setText("Capsule not found");
                return;
            }

            bindCapsule(capsule);
        });
    }

    private String resolveCapsuleId() {
        String capsuleId = getIntent().getStringExtra("capsuleId");
        if (capsuleId != null && !capsuleId.isEmpty()) {
            return capsuleId;
        }

        Uri data = getIntent().getData();
        if (data == null) {
            return null;
        }

        return data.getLastPathSegment();
    }

    private void bindCapsule(Capsule capsule) {
        titleText.setText(capsule.getTitle() != null ? capsule.getTitle() : "Untitled");

        Timestamp unlockTimestamp = capsule.getUnlockTime();
        boolean isUnlocked = false;

        if (unlockTimestamp != null) {
            Date unlockDate = unlockTimestamp.toDate();
            isUnlocked = System.currentTimeMillis() >= unlockDate.getTime();
        }

        if (isUnlocked) {
            showUnlockedCapsule(capsule);
        } else {
            showLockedCapsule(capsule);
        }
    }

    private void showLockedCapsule(Capsule capsule) {
        lockedSection.setVisibility(View.VISIBLE);
        unlockedSection.setVisibility(View.GONE);

        startWaitingAnimation();
        startDotsAnimation();
        startFloatingAnimation();

        Timestamp unlockTimestamp = capsule.getUnlockTime();
        if (unlockTimestamp == null) {
            lockedStatusText.setText("Unlock time not available");
            countdownText.setText("");
            return;
        }

        Date unlockDate = unlockTimestamp.toDate();
        unlockTimeMillis = unlockDate.getTime();

        lockedStatusText.setText("Locked until " + formatDate(unlockDate));
        startCountdown();
    }

    private void showUnlockedCapsule(Capsule capsule) {
        lockedSection.setVisibility(View.GONE);
        unlockedSection.setVisibility(View.VISIBLE);

        stopLockedAnimations();

        contentText.setText(
                capsule.getContent() != null && !capsule.getContent().trim().isEmpty()
                        ? capsule.getContent()
                        : "No message"
        );

        locationText.setText(
                capsule.getLocationName() != null && !capsule.getLocationName().trim().isEmpty()
                        ? capsule.getLocationName()
                        : "No location"
        );

        bindMedia(capsule);
    }

    private void bindMedia(Capsule capsule) {
        detailImage.setVisibility(View.GONE);
        videoContainer.setVisibility(View.GONE);
        noMediaText.setVisibility(View.GONE);
        videoPreviewLayout.setVisibility(View.GONE);

        List<String> mediaUrls = capsule.getMediaUrls();
        List<String> mediaTypes = capsule.getMediaTypes();

        if (mediaUrls == null || mediaUrls.isEmpty() || mediaTypes == null || mediaTypes.isEmpty()) {
            noMediaText.setVisibility(View.VISIBLE);
            return;
        }

        String firstUrl = mediaUrls.get(0);
        String firstType = mediaTypes.get(0);

        if (firstType == null || firstUrl == null || firstUrl.trim().isEmpty()) {
            noMediaText.setVisibility(View.VISIBLE);
            return;
        }

        if (firstType.equalsIgnoreCase("image") || firstType.equalsIgnoreCase("photo")) {
            detailImage.setVisibility(View.VISIBLE);
            Picasso.get().load(firstUrl).into(detailImage);

            detailImage.setOnClickListener(v -> showLargeImagePreview(firstUrl));

        } else if (firstType.equalsIgnoreCase("video")) {
            videoPreviewLayout.setVisibility(View.VISIBLE);

            videoPreviewLayout.setOnClickListener(v -> {
                videoPreviewLayout.setVisibility(View.GONE);
                videoContainer.setVisibility(View.VISIBLE);
                detailVideo.setVideoURI(Uri.parse(firstUrl));
                detailVideo.start();
            });

        } else {
            noMediaText.setVisibility(View.VISIBLE);
        }
    }

    private void showLargeImagePreview(String imageUrl) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_image_preview);

        ImageView previewImage = dialog.findViewById(R.id.ivPreviewLarge);
        Picasso.get().load(imageUrl).into(previewImage);

        previewImage.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void startCountdown() {
        if (countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
        }

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long diff = unlockTimeMillis - now;

                if (diff > 0) {
                    countdownText.setText(formatCountdown(diff));
                    handler.postDelayed(this, 1000);
                } else {
                    countdownText.setText("Ready to View");
                }
            }
        };

        handler.post(countdownRunnable);
    }

    private void startWaitingAnimation() {
        AnimationSet iconSet = new AnimationSet(true);

        AlphaAnimation iconFade = new AlphaAnimation(0.55f, 1.0f);
        iconFade.setDuration(1000);
        iconFade.setRepeatMode(Animation.REVERSE);
        iconFade.setRepeatCount(Animation.INFINITE);

        ScaleAnimation iconScale = new ScaleAnimation(
                1f, 1.10f,
                1f, 1.10f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        iconScale.setDuration(1000);
        iconScale.setRepeatMode(Animation.REVERSE);
        iconScale.setRepeatCount(Animation.INFINITE);

        iconSet.addAnimation(iconFade);
        iconSet.addAnimation(iconScale);
        waitingIcon.startAnimation(iconSet);

        AlphaAnimation textFade = new AlphaAnimation(0.4f, 1.0f);
        textFade.setDuration(900);
        textFade.setRepeatMode(Animation.REVERSE);
        textFade.setRepeatCount(Animation.INFINITE);
        waitingDotsText.startAnimation(textFade);
    }

    private void startDotsAnimation() {
        if (dotsRunnable != null) {
            handler.removeCallbacks(dotsRunnable);
        }

        dotsRunnable = new Runnable() {
            int count = 0;

            @Override
            public void run() {
                StringBuilder dots = new StringBuilder();
                for (int i = 0; i < count % 4; i++) {
                    dots.append(".");
                }

                waitingDotsText.setText("Waiting" + dots);
                count++;
                handler.postDelayed(this, 500);
            }
        };

        handler.post(dotsRunnable);
    }

    private void startFloatingAnimation() {
        if (waitingArea == null) {
            return;
        }

        TranslateAnimation floatAnim = new TranslateAnimation(0, 0, 0, -10);
        floatAnim.setDuration(1400);
        floatAnim.setRepeatMode(Animation.REVERSE);
        floatAnim.setRepeatCount(Animation.INFINITE);
        waitingArea.startAnimation(floatAnim);
    }

    private void stopLockedAnimations() {
        if (countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
        }
        if (dotsRunnable != null) {
            handler.removeCallbacks(dotsRunnable);
        }
        if (waitingIcon != null) {
            waitingIcon.clearAnimation();
        }
        if (waitingDotsText != null) {
            waitingDotsText.clearAnimation();
        }
        if (waitingArea != null) {
            waitingArea.clearAnimation();
        }
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    private String formatCountdown(long millis) {
        long totalSeconds = millis / 1000;
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m " + seconds + "s left";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s left";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s left";
        } else {
            return seconds + "s left";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLockedAnimations();
    }
}
