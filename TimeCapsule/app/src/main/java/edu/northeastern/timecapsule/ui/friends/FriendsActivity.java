package edu.northeastern.timecapsule.ui.friends;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.timecapsule.R;
import edu.northeastern.timecapsule.adapter.FriendAdapter;
import edu.northeastern.timecapsule.model.Friend;
import edu.northeastern.timecapsule.repository.FriendRepository;

public class FriendsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etFriendEmail;
    private Button btnAddFriend;
    private RecyclerView recyclerViewFriends;
    private TextView tvFriendCount;
    private TextView tvEmpty;

    private FriendAdapter adapter;
    private FriendRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        btnBack = findViewById(R.id.btnBack);
        etFriendEmail = findViewById(R.id.etFriendEmail);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        recyclerViewFriends = findViewById(R.id.recyclerViewFriends);
        tvFriendCount = findViewById(R.id.tvFriendCount);
        tvEmpty = findViewById(R.id.tvEmpty);

        repository = FriendRepository.getInstance();

        adapter = new FriendAdapter(friend -> showDeleteDialog(friend));

        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnAddFriend.setOnClickListener(v -> {
            String email = etFriendEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etFriendEmail.setError("Please enter a friend email");
                etFriendEmail.requestFocus();
                return;
            }

            repository.addFriendByEmail(email, new FriendRepository.AddFriendCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(FriendsActivity.this, "Friend added successfully", Toast.LENGTH_SHORT).show();
                    etFriendEmail.setText("");
                    loadFriends();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(FriendsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        loadFriends();
    }

    private void loadFriends() {
        repository.loadFriends(new FriendRepository.LoadFriendsCallback() {
            @Override
            public void onSuccess(List<Friend> friends) {
                adapter.setFriends(friends);
                updateEmptyState(friends);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(FriendsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState(List<Friend> friends) {
        int count = friends == null ? 0 : friends.size();
        tvFriendCount.setText(count + (count == 1 ? " Friend" : " Friends"));

        if (count == 0) {
            tvEmpty.setVisibility(TextView.VISIBLE);
            recyclerViewFriends.setVisibility(RecyclerView.GONE);
        } else {
            tvEmpty.setVisibility(TextView.GONE);
            recyclerViewFriends.setVisibility(RecyclerView.VISIBLE);
        }
    }

    private void showDeleteDialog(Friend friend) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Friend")
                .setMessage("Remove " + friend.getFriendEmail() + " from your friends list?")
                .setPositiveButton("Delete", (dialog, which) -> deleteFriend(friend))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteFriend(Friend friend) {
        if (friend.getFriendUid() == null || friend.getFriendUid().isEmpty()) {
            Toast.makeText(this, "Friend id is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.deleteFriend(friend.getFriendUid(), new FriendRepository.DeleteFriendCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(FriendsActivity.this, "Friend deleted", Toast.LENGTH_SHORT).show();
                loadFriends();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(FriendsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}