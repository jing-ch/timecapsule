package edu.northeastern.timecapsule.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;
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

    /** "all", "locked", or "unlocked" */
    private String currentStatusFilter = "all";
    private String currentTitleQuery = "";

    public void filterByTitle(String query) {
        currentTitleQuery = query == null ? "" : query.trim();
        applyFilters();
    }

    public void filterByStatus(String status) {
        currentStatusFilter = status == null ? "all" : status;
        applyFilters();
    }

    private void applyFilters() {
        List<Capsule> result = new ArrayList<>();
        String lower = currentTitleQuery.toLowerCase();
        Date now = new Date();

        for (Capsule capsule : originalCapsules) {
            // title filter
            if (!lower.isEmpty() && (capsule.getTitle() == null ||
                    !capsule.getTitle().toLowerCase().contains(lower))) {
                continue;
            }
            // status filter — use time comparison, same as CapsuleAdapter
            Timestamp unlockTime = capsule.getUnlockTime();
            boolean isEffectivelyUnlocked = unlockTime == null || !now.before(unlockTime.toDate());
            if ("locked".equals(currentStatusFilter) && isEffectivelyUnlocked) continue;
            if ("unlocked".equals(currentStatusFilter) && !isEffectivelyUnlocked) continue;

            result.add(capsule);
        }

        filteredCapsules.setValue(result);
    }

    public LiveData<Boolean> deleteCapsule(String capsuleId) {
        return repo.deleteCapsule(capsuleId);
    }
}