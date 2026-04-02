package edu.northeastern.timecapsule.viewmodel;

import androidx.lifecycle.LiveData;

import edu.northeastern.timecapsule.model.Capsule;

public class CapsuleDetailViewModel extends BaseViewModel {

    private LiveData<Capsule> capsule;

    public void loadCapsule(String capsuleId) {
        capsule = repo.fetchCapsuleById(capsuleId);
    }

    public LiveData<Capsule> getCapsule() {
        return capsule;
    }
}