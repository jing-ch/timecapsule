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

    /** Saves a capsule through repository */
    public void saveCapsule(Capsule capsule) {
        if (capsule == null) {
            errorMessage.setValue("Capsule is null");
            return;
        }

        LiveData<Boolean> source = repo.saveCapsule(capsule);
        saveResult.addSource(source, success -> {
            saveResult.setValue(success);
            saveResult.removeSource(source);
            if (success == null || !success) {
                errorMessage.setValue("Failed to save capsule");
            }
        });
    }
}