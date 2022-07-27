package com.homework.mygallery.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavHostController;
import androidx.navigation.fragment.NavHostFragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.homework.mygallery.R;
import com.homework.mygallery.view.home.PermissionFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 将权限申请回调传给PermissionFragment类型的子Fragment
        Fragment fragment = getSupportFragmentManager().getFragments().get(0);
        if (fragment instanceof NavHostFragment) {
            NavHostFragment navHostFragment = (NavHostFragment) fragment;
            for (Fragment fragment1 : navHostFragment.getChildFragmentManager().getFragments()) {
                if (fragment1 instanceof PermissionFragment) {
                    ((PermissionFragment) fragment1).onPermissionsResult(requestCode, permissions, grantResults);
                }
            }
        }
    }
}