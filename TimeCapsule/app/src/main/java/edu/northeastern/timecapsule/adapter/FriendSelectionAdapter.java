package edu.northeastern.timecapsule.adapter;

import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.northeastern.timecapsule.model.Friend;

public class FriendSelectionAdapter extends RecyclerView.Adapter<FriendSelectionAdapter.FriendSelectionViewHolder> {

    private final List<Friend> friends;
    private final Set<String> selectedFriendUids;
    private final Set<String> duplicateNames;

    public FriendSelectionAdapter(List<Friend> friends, Set<String> preselectedIds) {
        this.friends = friends != null ? friends : new ArrayList<>();
        this.selectedFriendUids = preselectedIds != null ? new HashSet<>(preselectedIds) : new HashSet<>();
        this.duplicateNames = buildDuplicateNames(this.friends);
    }

    private static Set<String> buildDuplicateNames(List<Friend> friends) {
        Map<String, Integer> counts = new HashMap<>();
        for (Friend f : friends) {
            String name = f.getFriendName() != null ? f.getFriendName() : "";
            counts.put(name, counts.getOrDefault(name, 0) + 1);
        }
        Set<String> duplicates = new HashSet<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > 1) duplicates.add(entry.getKey());
        }
        return duplicates;
    }

    @NonNull
    @Override
    public FriendSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CheckBox checkBox = new CheckBox(parent.getContext());
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        checkBox.setLayoutParams(params);
        checkBox.setPadding(32, 24, 32, 24);
        return new FriendSelectionViewHolder(checkBox);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendSelectionViewHolder holder, int position) {
        Friend friend = friends.get(position);

        String uid = friend.getFriendUid();
        String name = friend.getFriendName() != null ? friend.getFriendName() : "Unknown User";
        String email = friend.getFriendEmail() != null ? friend.getFriendEmail() : "";

        holder.checkBox.setOnCheckedChangeListener(null);
        String label = duplicateNames.contains(name) ? name + " (" + email + ")" : name;
        holder.checkBox.setText(label);
        holder.checkBox.setChecked(uid != null && selectedFriendUids.contains(uid));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (uid == null) return;

            if (isChecked) {
                selectedFriendUids.add(uid);
            } else {
                selectedFriendUids.remove(uid);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public ArrayList<String> getSelectedFriendUids() {
        return new ArrayList<>(selectedFriendUids);
    }

    public ArrayList<String> getSelectedFriendNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Friend friend : friends) {
            if (friend.getFriendUid() != null && selectedFriendUids.contains(friend.getFriendUid())) {
                names.add(friend.getFriendName() != null ? friend.getFriendName() : "Unknown User");
            }
        }
        return names;
    }

    static class FriendSelectionViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public FriendSelectionViewHolder(@NonNull CheckBox itemView) {
            super(itemView);
            checkBox = itemView;
        }
    }
}