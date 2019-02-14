package com.example.rene.nightparty0.Ayuda;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MiFirebaseInstanceService extends FirebaseInstanceIdService {

    public static final String TAG = "MYTOKEN";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "TOKEN => " + token);
        storeToken(token);
    }

    private void storeToken(String token) {
        SharedPreferencesManager.getInstance(getApplicationContext()).storeToken(token);
    }

}
