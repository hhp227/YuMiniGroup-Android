package com.hhp227.yu_minigroup.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.dto.User;

public class LoginViewModel extends ViewModel {
    public MutableLiveData<State> mState = new MutableLiveData<>(null);

    public void login(String id, String password) {
        Log.e("TEST", "id: " + id + ", password: " + password);
    }

    public static final class State {
        public boolean isLoading;

        public User user;

        public String message;

        public State(boolean isLoading, User user, String message) {
            this.isLoading = isLoading;
            this.user = user;
            this.message = message;
        }
    }
}
