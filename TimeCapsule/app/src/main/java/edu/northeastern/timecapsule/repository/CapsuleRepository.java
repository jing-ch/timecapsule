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
 *   generateCapsuleId()                      -> Teammate C, call this first to get an ID
 *   saveCapsuleWithId(capsule, capsuleId)    -> Teammate C, this is yours -- submits the creation form
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
     * Teammate C -- call this before uploading media to get a capsule ID in advance.
     * Use the returned ID in the Storage path, then pass it to saveCapsuleWithId().
     */
    public String generateCapsuleId() {
        return db.collection(COLLECTION).document().getId();
    }

    /**
     * Teammate C -- use this to save a new capsule with a pre-generated ID.
     * Call generateCapsuleId() first to get the ID, use it in the Storage path,
     * then call this method to save the capsule. Returns true on success, false on failure.
     *
     * @param capsule    a fully filled Capsule object (use setters to populate all fields first)
     * @param capsuleId  the ID returned by generateCapsuleId()
     */
    public LiveData<Boolean> saveCapsuleWithId(Capsule capsule, String capsuleId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        db.collection(COLLECTION)
                .document(capsuleId)
                .set(capsule)
                .addOnSuccessListener(unused -> result.setValue(true))
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
