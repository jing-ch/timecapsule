package edu.northeastern.timecapsule.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import edu.northeastern.timecapsule.model.Capsule;

/**
 * ViewModel for capsule creation.
 */
public class CreateCapsuleViewModel extends BaseViewModel {

    /** Save result state */
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

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

        repo.saveCapsule(capsule).observeForever(success -> {
            if (success != null && success) {
                saveResult.setValue(true);
            } else {
                saveResult.setValue(false);
                errorMessage.setValue("Failed to save capsule");
            }
        });
    }
}