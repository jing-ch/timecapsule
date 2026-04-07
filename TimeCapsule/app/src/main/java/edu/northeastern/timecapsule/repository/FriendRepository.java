package edu.northeastern.timecapsule.repository;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.timecapsule.model.Friend;

public class FriendRepository {

    public interface AddFriendCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public interface LoadFriendsCallback {
        void onSuccess(List<Friend> friends);
        void onFailure(String message);
    }

    public interface DeleteFriendCallback {
        void onSuccess();
        void onFailure(String message);
    }

    private static FriendRepository instance;

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    private FriendRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static FriendRepository getInstance() {
        if (instance == null) {
            instance = new FriendRepository();
        }
        return instance;
    }

    public void addFriendByEmail(@NonNull String email, @NonNull AddFriendCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            callback.onFailure("User not logged in");
            return;
        }

        String trimmedEmail = email.trim();
        String currentUid = currentUser.getUid();
        String currentEmail = currentUser.getEmail();

        if (trimmedEmail.isEmpty()) {
            callback.onFailure("Email cannot be empty");
            return;
        }

        if (currentEmail != null && currentEmail.equalsIgnoreCase(trimmedEmail)) {
            callback.onFailure("You cannot add yourself");
            return;
        }

        firestore.collection("users")
                .whereEqualTo("email", trimmedEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onFailure("No user found with this email");
                        return;
                    }

                    DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);

                    String friendUid = userDoc.getId();
                    String friendEmail = userDoc.getString("email");
                    String friendName = userDoc.getString("displayName");

                    if (friendUid.equals(currentUid)) {
                        callback.onFailure("You cannot add yourself");
                        return;
                    }

                    firestore.collection("users")
                            .document(currentUid)
                            .collection("friends")
                            .document(friendUid)
                            .get()
                            .addOnSuccessListener(friendSnapshot -> {
                                if (friendSnapshot.exists()) {
                                    callback.onFailure("This friend is already added");
                                    return;
                                }

                                Map<String, Object> friendData = new HashMap<>();
                                friendData.put("friendUid", friendUid);
                                friendData.put("friendEmail", friendEmail != null ? friendEmail : trimmedEmail);
                                friendData.put("friendName", friendName != null ? friendName : "Unknown User");
                                friendData.put("addedAt", Timestamp.now());

                                firestore.collection("users")
                                        .document(currentUid)
                                        .collection("friends")
                                        .document(friendUid)
                                        .set(friendData)
                                        .addOnSuccessListener(unused -> callback.onSuccess())
                                        .addOnFailureListener(e ->
                                                callback.onFailure("Failed to save friend: " + e.getMessage()));
                            })
                            .addOnFailureListener(e ->
                                    callback.onFailure("Failed to check existing friend: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to search user: " + e.getMessage()));
    }

    public void loadFriends(@NonNull LoadFriendsCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            callback.onFailure("User not logged in");
            return;
        }

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Friend> friends = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Friend friend = doc.toObject(Friend.class);
                        if (friend != null) {
                            friends.add(friend);
                        }
                    }

                    callback.onSuccess(friends);
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to load friends: " + e.getMessage()));
    }

    public void deleteFriend(@NonNull String friendUid, @NonNull DeleteFriendCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            callback.onFailure("User not logged in");
            return;
        }

        if (friendUid.trim().isEmpty()) {
            callback.onFailure("Friend id is empty");
            return;
        }

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .document(friendUid)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to delete friend: " + e.getMessage()));
    }
}