package edu.northeastern.timecapsule.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import edu.northeastern.timecapsule.model.Capsule;

/**
 * ViewModel for capsule creation.
 */
public class CreateCapsuleViewModel extends BaseViewModel {

    /** Save result state */
    private final MediatorLiveData<Boolean> saveResult = new MediatorLiveData<>();

    /** Returns save result */
    public LiveData<Boolean> getSaveResult() {
        return saveResult;
    }

    /** Saves a capsule with a pre-generated ID through repository */
    public void saveCapsuleWithId(Capsule capsule, String capsuleId) {
        if (capsule == null) {
            errorMessage.setValue("Capsule is null");
            return;
        }

        LiveData<Boolean> source = repo.saveCapsuleWithId(capsule, capsuleId);
        saveResult.addSource(source, success -> {
            saveResult.setValue(success);
            saveResult.removeSource(source);
            if (success == null || !success) {
                errorMessage.setValue("Failed to save capsule");
            }
        });
    }
}