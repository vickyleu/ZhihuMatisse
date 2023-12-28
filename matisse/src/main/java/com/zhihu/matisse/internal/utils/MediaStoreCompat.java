/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.internal.utils;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;
import androidx.fragment.app.Fragment;

import com.dylanc.activityresult.launcher.AppDetailsSettingsLauncher;
import com.dylanc.activityresult.launcher.RequestPermissionLauncher;
import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.ui.CameraActivity;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaStoreCompat {

    private final WeakReference<AppCompatActivity> mContext;
    private final WeakReference<Fragment> mFragment;
    private CaptureStrategy mCaptureStrategy;
    private Uri mCurrentPhotoUri;
    private String mCurrentPhotoPath;

    public MediaStoreCompat(AppCompatActivity activity) {
        mContext = new WeakReference<>(activity);
        mFragment = null;
    }

    public MediaStoreCompat(AppCompatActivity activity, Fragment fragment) {
        mContext = new WeakReference<>(activity);
        mFragment = new WeakReference<>(fragment);
    }

    /**
     * Checks whether the device has a camera feature or not.
     *
     * @param context a context to check for camera feature.
     * @return true if the device has a camera feature. false otherwise.
     */
    public static boolean hasCameraFeature(Context context) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public void setCaptureStrategy(CaptureStrategy strategy) {
        mCaptureStrategy = strategy;
    }

    public void dispatchCaptureIntent(Context context, RequestPermissionLauncher permissionLauncher,
                                      AppDetailsSettingsLauncher appDetailsSettingsLauncher, int requestCode) {
        int granted=  ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if(granted != PackageManager.PERMISSION_GRANTED){
            //显示一个dialog,提示用户需要相机权限,需要打开设置页面去开启权限,一个是取消,一个是去设置
            permissionLauncher.launch(Manifest.permission.CAMERA, ActivityOptionsCompat.makeBasic(), grantedState -> {
                if(!grantedState){
                    // 相机权限已经被禁用了,需要跳转到系统设置页面,去手动开启 appDetailsSettingsLauncher
                    AlertDialogUtils.showDialog(context, "需要相机权限", "取消", "去设置", (dialog, which) -> {
                        dialog.dismiss();
                    }, (dialog, which) -> {
                        dialog.dismiss();
                        openSettings(context, appDetailsSettingsLauncher, requestCode);
                    });
                }else{
                    openCamera(context, requestCode);
                }
            });
            return;
        }
        openCamera(context, requestCode);
    }

    private void openSettings(Context context, AppDetailsSettingsLauncher appDetailsSettingsLauncher, int requestCode) {
        appDetailsSettingsLauncher.launch(o -> {
            int granted1 = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
            if(granted1 == PackageManager.PERMISSION_GRANTED) {
                openCamera(context, requestCode);
            }else{
                Toast.makeText(context, "未开启相机权限", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCamera(Context context, int requestCode) {
        Intent captureIntent = new Intent(context, CameraActivity.class);
        if (captureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                mCurrentPhotoPath = photoFile.getAbsolutePath();
                mCurrentPhotoUri = FileProvider.getUriForFile(mContext.get(),
                        mCaptureStrategy.authority, photoFile);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoPath);
//                    captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                if (mFragment != null) {
                    mFragment.get().startActivityForResult(captureIntent, requestCode);
                    mFragment.get().requireActivity().overridePendingTransition(R.anim.translate_in,0);
                } else {
                    mContext.get().startActivityForResult(captureIntent, requestCode);
                    mContext.get().overridePendingTransition(R.anim.translate_in,0);
                }
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = String.format("JPEG_%s.jpg", timeStamp);
        File storageDir;
        if (mCaptureStrategy.isPublic) {
            storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            if (!storageDir.exists()) storageDir.mkdirs();
        } else {
            storageDir = mContext.get().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        if (mCaptureStrategy.directory != null) {
            storageDir = new File(storageDir, mCaptureStrategy.directory);
            if (!storageDir.exists()) storageDir.mkdirs();
        }

        // Avoid joining path components manually
        File tempFile = new File(storageDir, imageFileName);

        // Handle the situation that user's external storage is not ready
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }

        return tempFile;
    }

    public Uri getCurrentPhotoUri() {
        return mCurrentPhotoUri;
    }

    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    static class AlertDialogUtils {
        public static void showDialog(Context context, String title, String negativeText,
                                      String positiveText, DialogInterface.OnClickListener negativeListener,
                                      DialogInterface.OnClickListener positiveListener) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.Alert_Zhihu);
            builder.setTitle(title);
            builder.setNegativeButton(negativeText, negativeListener);
            builder.setPositiveButton(positiveText, positiveListener);
            // 设置 AlertDialog 的宽度和高度
//            builder.setSize(0.75f * context.getResources().getDisplayMetrics().widthPixels, 150);
            AlertDialog dialog = builder.create();
            // 设置dialog的宽高,宽是屏幕宽的0.75,高是150dp
            dialog.getWindow().setLayout((int) (0.75f * context.getResources().getDisplayMetrics().widthPixels), dp2px(context, 150));
            dialog.show();
        }

        private static int dp2px(Context context, int dp) {
            float density = context.getResources().getDisplayMetrics().density;
            return (int) (dp * density + 0.5f);
        }
    }
}
