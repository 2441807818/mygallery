package com.homework.mygallery.view.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * @introduction： 权限Fragment基类
 * @author： 林锦焜
 * @time： 2022/7/27 20:02
 */
public abstract class PermissionFragment extends Fragment {

    public abstract void onPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

}
