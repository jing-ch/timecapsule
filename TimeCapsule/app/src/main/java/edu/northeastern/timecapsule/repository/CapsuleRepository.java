package edu.northeastern.timecapsule.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

import edu.northeastern.timecapsule.model.Capsule;

/**
 * Hey Chuwei and Kaiyin,
 *
 * Quick note on how we structured the code -- we're using MVVM architecture:
 *   Activity (View)  -> only handles UI and button clicks
 *   ViewModel        -> holds data and logic, survives screen rotation
 *   Repository       -> the only place that talks to Firestore (that's this file!)
 *
 * So please don't write db.collection(...) in your own files -- just call the methods
 * here instead. That way if anything changes in Firestore, we only fix it in one place.
 * Trust me it saves a lot of headache later.
 *
 * HOW TO USE (in your ViewModel):
 *   If you extended BaseViewModel, you already have "repo" ready -- just call it directly:
 *     repo.fetchUserCapsules(userId)
 *   Each method returns a LiveData, observe it in your Activity as usual.
 *
 * WHAT'S AVAILABLE:
 *   fetchUserCapsules(userId)  -> Teammate B, this is yours -- loads the capsule list
 *   deleteCapsule(capsuleId)   -> Teammate B, this is yours -- for the delete dialog
 *   saveCapsule(capsule)       -> Teammate C, this is yours -- submits the creation form
 */
public class CapsuleRepository {

    private static final String COLLECTION = "capsules";

    private static CapsuleRepository instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CapsuleRepository() {}

    /** Call this to get the repository -- do NOT use new CapsuleRepository() */
    public static CapsuleRepository getInstance() {
        if (instance == null) {
            instance = new CapsuleRepository();
        }
        return instance;
    }

    /**
     * Teammate B -- use this to load the capsule list.
     * Returns all capsules for the given user, newest first. Updates in real time.
     *
     * @param userId  get this from FirebaseAuth.getInstance().getCurrentUser().getUid()
     */
    public LiveData<List<Capsule>> fetchUserCapsules(String userId) {
        MutableLiveData<List<Capsule>> liveData = new MutableLiveData<>();

        db.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;
                    liveData.setValue(snapshot.toObjects(Capsule.class));
                });

        return liveData;
    }

    /**
     * Teammate C -- use this to save a new capsule.
     * Call this at the end of the 3-step wizard. Returns true on success, false on failure.
     *
     * @param capsule  a fully filled Capsule object (use setters to populate all fields first)
     */
    public LiveData<Boolean> saveCapsule(Capsule capsule) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        db.collection(COLLECTION)
                .add(capsule)
                .addOnSuccessListener(ref -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }

    /**
     * Teammate B -- use this to delete a capsule.
     * Call this when the user confirms deletion. Returns true on success, false on failure.
     *
     * @param capsuleId  get this from capsule.getCapsuleId()
     */
    public LiveData<Boolean> deleteCapsule(String capsuleId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        db.collection(COLLECTION)
                .document(capsuleId)
                .delete()
                .addOnSuccessListener(unused -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }
}
