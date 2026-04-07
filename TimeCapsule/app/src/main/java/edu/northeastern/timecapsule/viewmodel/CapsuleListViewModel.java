package edu.northeastern.timecapsule.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.timecapsule.model.Capsule;

public class CapsuleListViewModel extends BaseViewModel {

    private final MediatorLiveData<List<Capsule>> capsules = new MediatorLiveData<>();
    private final MutableLiveData<List<Capsule>> filteredCapsules = new MutableLiveData<>();
    private List<Capsule> originalCapsules = new ArrayList<>();
    private List<Capsule> ownCapsules = new ArrayList<>();
    private List<Capsule> sharedCapsules = new ArrayList<>();

    public void loadCapsules() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            capsules.setValue(new ArrayList<>());
            filteredCapsules.setValue(new ArrayList<>());
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        LiveData<List<Capsule>> ownSource = repo.fetchUserCapsules(userId);
        LiveData<List<Capsule>> sharedSource = repo.fetchSharedCapsules(userId);

        capsules.addSource(ownSource, list -> {
            ownCapsules = list != null ? list : new ArrayList<>();
            mergeCapsules();
        });
        capsules.addSource(sharedSource, list -> {
            sharedCapsules = list != null ? list : new ArrayList<>();
            mergeCapsules();
        });
    }

    public LiveData<List<Capsule>> getCapsules() {
        return capsules;
    }

    public LiveData<List<Capsule>> getFilteredCapsules() {
        return filteredCapsules;
    }

    public void setOriginalCapsules(List<Capsule> list) {
        originalCapsules = list != null ? list : new ArrayList<>();
        applyFilters();
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

    private void mergeCapsules() {
        Map<String, Capsule> merged = new LinkedHashMap<>();

        for (Capsule capsule : ownCapsules) {
            if (capsule != null && capsule.getCapsuleId() != null) {
                merged.put(capsule.getCapsuleId(), capsule);
            }
        }

        for (Capsule capsule : sharedCapsules) {
            if (capsule != null && capsule.getCapsuleId() != null) {
                merged.putIfAbsent(capsule.getCapsuleId(), capsule);
            }
        }

        List<Capsule> mergedList = new ArrayList<>(merged.values());
        capsules.setValue(mergedList);
        setOriginalCapsules(mergedList);
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
