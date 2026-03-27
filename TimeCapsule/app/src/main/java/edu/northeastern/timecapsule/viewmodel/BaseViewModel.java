package edu.northeastern.timecapsule.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import edu.northeastern.timecapsule.repository.CapsuleRepository;

/**
 * Hey Chuwei and Kaiyin,
 *
 * We're using MVVM, so all your ViewModels should extend this class instead of ViewModel directly.
 * Just change "extends ViewModel" to "extends BaseViewModel" and you get two things for free:
 *
 * (1) "repo" -- use this to talk to Firestore, no setup needed.
 *     Example:  repo.fetchUserCapsules(userId)
 *     Check CapsuleRepository.java to see all available methods and which ones are for you.
 *
 * (2) "errorMessage" -- use this to show an error Toast in your Activity.
 *     In your ViewModel:   errorMessage.setValue("Something went wrong!");
 *     In your Activity:    viewModel.errorMessage.observe(this, msg ->
 *                              Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
 *
 * You don't need to change anything in this file. Just extend it and you're good to go!
 */
public class BaseViewModel extends ViewModel {

    /** Use this to call Firestore — see CapsuleRepository for available methods */
    protected final CapsuleRepository repo = CapsuleRepository.getInstance();

    /** Post an error message here to show a Toast in your Activity */
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
}
