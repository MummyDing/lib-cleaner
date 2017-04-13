/*
 * Copyright (C) 2012 www.amsoft.cn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mummyding.ymsecurity.lib_clean;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;


import com.github.mummyding.ymsecurity.lib_clean.model.AutoStartInfo;

import java.util.ArrayList;
import java.util.List;


// TODO: Auto-generated Javadoc

public class BootStartUtils {

    private static final String BOOT_START_PERMISSION = "android.permission.RECEIVE_BOOT_COMPLETED";
    // private Context mContext;

    public BootStartUtils(Context context) {
        //   mContext = context;
    }

    public static List<AutoStartInfo> fetchStartAutoApps(Context mContext) {
        PackageManager pm = mContext.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        List<ResolveInfo> resolveInfoList = pm.queryBroadcastReceivers(intent, PackageManager.GET_DISABLED_COMPONENTS);
        List<AutoStartInfo> appList = new ArrayList<AutoStartInfo>();
        String appName = null;
        String packageReceiver = null;
        Drawable icon = null;
        boolean isSystem = false;
        boolean isenable = true;
        if (resolveInfoList.size() > 0) {

            appName = resolveInfoList.get(0).loadLabel(pm).toString();
            packageReceiver = resolveInfoList.get(0).activityInfo.packageName + "/" + resolveInfoList.get(0).activityInfo.name;
            icon = resolveInfoList.get(0).loadIcon(pm);
            ComponentName mComponentName1 = new ComponentName(resolveInfoList.get(0).activityInfo.packageName, resolveInfoList.get(0).activityInfo.name);

            if (pm.getComponentEnabledSetting(mComponentName1) == 2) {

                isenable = false;
            } else {
                isenable = true;
            }
            if ((resolveInfoList.get(0).activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                isSystem = true;
            } else {
                isSystem = false;
            }
            for (int i = 1; i < resolveInfoList.size(); i++) {
                AutoStartInfo mAutoStartInfo = new AutoStartInfo();
                if (appName.equals(resolveInfoList.get(i).loadLabel(pm).toString())) {
                    packageReceiver = packageReceiver + ";" + resolveInfoList.get(i).activityInfo.packageName + "/" + resolveInfoList.get(i).activityInfo.name;
                } else {
                    mAutoStartInfo.setLabel(appName);
                    mAutoStartInfo.setSystem(isSystem);
                    mAutoStartInfo.setEnable(isenable);
                    mAutoStartInfo.setIcon(icon);
                    mAutoStartInfo.setPackageReceiver(packageReceiver);

                    appList.add(mAutoStartInfo);
                    appName = resolveInfoList.get(i).loadLabel(pm).toString();
                    packageReceiver = resolveInfoList.get(i).activityInfo.packageName + "/" + resolveInfoList.get(i).activityInfo.name;
                    icon = resolveInfoList.get(i).loadIcon(pm);
                    ComponentName mComponentName2 = new ComponentName(resolveInfoList.get(i).activityInfo.packageName, resolveInfoList.get(i).activityInfo.name);
                    if (pm.getComponentEnabledSetting(mComponentName2) == 2) {

                        isenable = false;
                    } else {
                        isenable = true;
                    }

                    if ((resolveInfoList.get(i).activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        isSystem = true;
                    } else {
                        isSystem = false;
                    }

                }

            }
            AutoStartInfo mAutoStartInfo = new AutoStartInfo();
            mAutoStartInfo.setLabel(appName);
            mAutoStartInfo.setSystem(isSystem);
            mAutoStartInfo.setEnable(isenable);
            mAutoStartInfo.setIcon(icon);
            mAutoStartInfo.setPackageReceiver(packageReceiver);
            appList.add(mAutoStartInfo);
        }
        return appList;
    }
}