package edu.northeastern.timecapsule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.northeastern.timecapsule.R;
import edu.northeastern.timecapsule.model.Capsule;
import com.google.firebase.auth.FirebaseAuth;

public class CapsuleAdapter extends RecyclerView.Adapter<CapsuleAdapter.CapsuleViewHolder> {

    public interface OnCapsuleClickListener {
        void onCapsuleClick(Capsule capsule);
        void onCapsuleLongClick(Capsule capsule);
    }

    private final List<Capsule> capsuleList = new ArrayList<>();
    private final OnCapsuleClickListener listener;

    public CapsuleAdapter(OnCapsuleClickListener listener) {
        this.listener = listener;
    }

    public void setCapsules(List<Capsule> capsules) {
        capsuleList.clear();
        if (capsules != null) {
            capsuleList.addAll(capsules);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CapsuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_capsule, parent, false);
        return new CapsuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CapsuleViewHolder holder, int position) {
        Capsule capsule = capsuleList.get(position);
        holder.bind(capsule, listener);
    }

    @Override
    public int getItemCount() {
        return capsuleList.size();
    }

    static class CapsuleViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView sharedBadgeText;
        TextView unlockText;
        TextView countdownText;
        ImageView lockStatusIcon;

        public CapsuleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tvCapsuleTitle);
            sharedBadgeText = itemView.findViewById(R.id.tvSharedBadge);
            unlockText = itemView.findViewById(R.id.tvCapsuleUnlock);
            countdownText = itemView.findViewById(R.id.tvCountdown);
            lockStatusIcon = itemView.findViewById(R.id.ivLockStatus);
        }

        public void bind(Capsule capsule, OnCapsuleClickListener listener) {
            titleText.setText(capsule.getTitle() != null ? capsule.getTitle() : "Untitled Capsule");

            String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : null;
            boolean isSharedWithMe = currentUid != null && !capsule.isOwnedBy(currentUid);
            sharedBadgeText.setVisibility(isSharedWithMe ? View.VISIBLE : View.GONE);

            Timestamp unlockTimestamp = capsule.getUnlockTime();
            Date now = new Date();

            if (unlockTimestamp != null) {
                Date unlockDate = unlockTimestamp.toDate();

                if (now.before(unlockDate)) {
                    lockStatusIcon.setImageResource(android.R.drawable.ic_lock_lock);
                    unlockText.setText("Locked until " + formatDate(unlockDate));
                    countdownText.setVisibility(View.VISIBLE);
                    countdownText.setText(formatCountdown(unlockDate.getTime() - now.getTime()));
                } else {
                    lockStatusIcon.setImageResource(android.R.drawable.ic_lock_idle_lock);
                    unlockText.setText("Ready to View");
                    countdownText.setVisibility(View.GONE);
                }
            } else {
                lockStatusIcon.setImageResource(android.R.drawable.ic_lock_idle_lock);
                unlockText.setText("Ready to View");
                countdownText.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onCapsuleClick(capsule));
            itemView.setOnLongClickListener(v -> {
                listener.onCapsuleLongClick(capsule);
                return true;
            });
        }

        private String formatDate(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return sdf.format(date);
        }

        private String formatCountdown(long millis) {
            long totalSeconds = millis / 1000;
            long days = totalSeconds / (24 * 3600);
            long hours = (totalSeconds % (24 * 3600)) / 3600;
            long minutes = (totalSeconds % 3600) / 60;

            if (days > 0) {
                return days + "d " + hours + "h " + minutes + "m left";
            } else if (hours > 0) {
                return hours + "h " + minutes + "m left";
            } else if (minutes > 0) {
                return minutes + "m left";
            } else {
                return "Less than 1 minute left";
            }
        }
    }
}
