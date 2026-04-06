package edu.northeastern.timecapsule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.timecapsule.R;
import edu.northeastern.timecapsule.model.Friend;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    public interface OnFriendActionListener {
        void onDeleteClick(Friend friend);
    }

    private final List<Friend> friends = new ArrayList<>();
    private final OnFriendActionListener listener;

    public FriendAdapter(OnFriendActionListener listener) {
        this.listener = listener;
    }

    public void setFriends(List<Friend> newFriends) {
        friends.clear();
        if (newFriends != null) {
            friends.addAll(newFriends);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friends.get(position);

        holder.tvFriendName.setText(
                friend.getFriendName() != null ? friend.getFriendName() : "Unknown User"
        );

        holder.tvFriendEmail.setText(
                friend.getFriendEmail() != null ? friend.getFriendEmail() : "No Email"
        );

        holder.btnDeleteFriend.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendName;
        TextView tvFriendEmail;
        ImageButton btnDeleteFriend;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            tvFriendEmail = itemView.findViewById(R.id.tvFriendEmail);
            btnDeleteFriend = itemView.findViewById(R.id.btnDeleteFriend);
        }
    }
}