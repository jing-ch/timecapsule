package edu.northeastern.timecapsule.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.timecapsule.model.Capsule;

public class CapsuleListViewModel extends BaseViewModel {

    private LiveData<List<Capsule>> capsules;
    private final MutableLiveData<List<Capsule>> filteredCapsules = new MutableLiveData<>();
    private List<Capsule> originalCapsules = new ArrayList<>();

    public void loadCapsules() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            filteredCapsules.setValue(new ArrayList<>());
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        capsules = repo.fetchUserCapsules(userId);
    }

    public LiveData<List<Capsule>> getCapsules() {
        return capsules;
    }

    public LiveData<List<Capsule>> getFilteredCapsules() {
        return filteredCapsules;
    }

    public void setOriginalCapsules(List<Capsule> list) {
        originalCapsules = list != null ? list : new ArrayList<>();
        filteredCapsules.setValue(originalCapsules);
    }

    public void filterByTitle(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredCapsules.setValue(originalCapsules);
            return;
        }

        List<Capsule> result = new ArrayList<>();
        String lower = query.toLowerCase().trim();

        for (Capsule capsule : originalCapsules) {
            if (capsule.getTitle() != null &&
                    capsule.getTitle().toLowerCase().contains(lower)) {
                result.add(capsule);
            }
        }

        filteredCapsules.setValue(result);
    }

    public LiveData<Boolean> deleteCapsule(String capsuleId) {
        return repo.deleteCapsule(capsuleId);
    }
}