package com.xigenix.versionupdate;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class UpdateHelper {

    public static String KEY_UPDATE_URL = "android_update_url";
    public static String KEY_VERSION = "android_version";
    public static String KEY_UPDATE_TYPE = "android_update_isForce";

    public interface OnUpdateCheckListener{
        void onUpdateCheckListener(String appUrl, Boolean updateType);
    }

    public static Builder with(Context context){
        return new Builder(context);
    }

    private final OnUpdateCheckListener onUpdateCheckListener;
    private final Context context;

    public UpdateHelper(Context context, OnUpdateCheckListener onUpdateCheckListener) {
        this.onUpdateCheckListener = onUpdateCheckListener;
        this.context = context;
    }

    public void check(){

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10)
                .build();

        remoteConfig.setConfigSettingsAsync(settings);
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            Log.e("RM", "Fetch and activate succeeded.");

                            String currentVersion = remoteConfig.getString(KEY_VERSION);
                            String appVersion = getAppVersion(context);
                            String updateUrl = remoteConfig.getString(KEY_UPDATE_URL);
                            Boolean updateType = remoteConfig.getBoolean(KEY_UPDATE_TYPE);

                            if(!TextUtils.equals(currentVersion, appVersion) && onUpdateCheckListener != null){
                                onUpdateCheckListener.onUpdateCheckListener(updateUrl, updateType);
                            }

                        }
                    }
                });
    }

    private String getAppVersion(Context context){
        String result = "";

        try{
            result = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName.replace("[a-zA-Z]|-", "");
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        return result;
    }

    public static class Builder{
        private final Context context;
        private OnUpdateCheckListener onUpdateCheckListener;

        public Builder(Context context){
            this.context = context;
        }

        public Builder onUpdateCheck(OnUpdateCheckListener onUpdateCheckListener){
            this.onUpdateCheckListener = onUpdateCheckListener;
            return this;
        }

        public UpdateHelper build(){
            return new UpdateHelper(context, onUpdateCheckListener);
        }

        public UpdateHelper check(){
            UpdateHelper updateHelper = build();
            updateHelper.check();
            return updateHelper;
        }
    }

}
