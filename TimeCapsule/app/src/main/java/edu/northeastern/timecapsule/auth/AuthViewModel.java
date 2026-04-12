package edu.northeastern.timecapsule.auth;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AuthViewModel extends ViewModel {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public void login(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> currentUser.setValue(result.getUser()))
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }

    public void register(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("email", email);
                        data.put("displayName", email);
                        firestore.collection("users")
                                .document(user.getUid())
                                .set(data, SetOptions.merge())
                                .addOnCompleteListener(task -> currentUser.setValue(user));
                    }
                })
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void signOut() {
        auth.signOut();
        currentUser.setValue(null);
    }
}
