package edu.northeastern.timecapsule.auth;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public void login(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> currentUser.setValue(result.getUser()))
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }

    public void register(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> currentUser.setValue(result.getUser()))
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
