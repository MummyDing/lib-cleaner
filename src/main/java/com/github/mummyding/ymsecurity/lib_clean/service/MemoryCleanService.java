package com.github.mummyding.ymsecurity.lib_clean.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.github.mummyding.ymsecurity.lib_clean.R;
import com.jaredrummler.android.processes.AndroidProcesses;
import com.github.mummyding.ymbase.model.AppProcessInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class MemoryCleanService extends Service {

    public static final String ACTION_CLEAN_AND_EXIT = "com.github.mummyding.ymsecurity.lib_clean.service.cleaner.CLEAN_AND_EXIT";

    private static final String TAG = "CacheCleanService";


    private OnProcessActionListener mOnActionListener;
    private boolean mIsScanning = false;
    private boolean mIsCleaning = false;

    private ActivityManager mActivityManager = null;
    private List<AppProcessInfo> mAppProcessList = null;
    private PackageManager mPackageManager = null;
    private Context mContext;


    public static interface OnProcessActionListener {
        public void onScanStarted(Context context);

        public void onScanProgressUpdated(Context context, int current, int max);

        public void onScanCompleted(Context context, List<AppProcessInfo> apps);

        public void onCleanStarted(Context context);

        public void onCleanCompleted(Context context, long cacheSize);
    }

    public class ProcessServiceBinder extends Binder {

        public MemoryCleanService getService() {
            return MemoryCleanService.this;
        }
    }

    private ProcessServiceBinder mBinder = new ProcessServiceBinder();


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mContext = getApplicationContext();

        try {
            mActivityManager = (ActivityManager)
                    getSystemService(Context.ACTIVITY_SERVICE);
            mPackageManager = getApplicationContext()
                    .getPackageManager();
        } catch (Exception e) {

        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action != null) {
            if (action.equals(ACTION_CLEAN_AND_EXIT)) {
                setOnActionListener(new OnProcessActionListener() {
                    @Override
                    public void onScanStarted(Context context) {

                    }

                    @Override
                    public void onScanProgressUpdated(Context context, int current, int max) {

                    }

                    @Override
                    public void onScanCompleted(Context context, List<AppProcessInfo> apps) {
                        //   if (getCacheSize() > 0) {
                        //     cleanCache();
                        // }
                    }

                    @Override
                    public void onCleanStarted(Context context) {

                    }

                    @Override
                    public void onCleanCompleted(Context context, long cacheSize) {
                        String msg = getString(R.string.cleaned, Formatter.formatShortFileSize(
                                MemoryCleanService.this, cacheSize));

                        Log.d(TAG, msg);

                        Toast.makeText(MemoryCleanService.this, msg, Toast.LENGTH_LONG).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                stopSelf();
                            }
                        }, 5000);
                    }
                });

                scanRunProcess();
            }
        }

        return START_NOT_STICKY;
    }


    private class TaskScan extends AsyncTask<Void, Integer, List<AppProcessInfo>> {

        private int mAppCount = 0;

        @Override
        protected void onPreExecute() {
            if (mOnActionListener != null) {
                mOnActionListener.onScanStarted(MemoryCleanService.this);
            }
        }

        @Override
        protected List<AppProcessInfo> doInBackground(Void... params) {
            mAppProcessList = new ArrayList<AppProcessInfo>();
            ApplicationInfo appInfo = null;
            AppProcessInfo abAppProcessInfo = null;

            List<ActivityManager.RunningAppProcessInfo> appProcessList =
                    AndroidProcesses.getRunningAppProcessInfo(mContext);
            publishProgress(0, appProcessList.size());

            for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
                publishProgress(++mAppCount, appProcessList.size());
                abAppProcessInfo = new AppProcessInfo(
                        appProcessInfo.processName, appProcessInfo.pid,
                        appProcessInfo.uid);
                try {
                    appInfo = mPackageManager.getApplicationInfo(appProcessInfo.processName, 0);


                    if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        abAppProcessInfo.isSystem = true;
                    } else {
                        abAppProcessInfo.isSystem = false;
                    }
                    Drawable icon = appInfo.loadIcon(mPackageManager);
                    String appName = appInfo.loadLabel(mPackageManager)
                            .toString();
                    abAppProcessInfo.icon = icon;
                    abAppProcessInfo.appName = appName;
                } catch (PackageManager.NameNotFoundException e) {
                    //   e.printStackTrace();

                    // :服务的命名

                    if (appProcessInfo.processName.indexOf(":") != -1) {
                        appInfo = getApplicationInfo(appProcessInfo.processName.split(":")[0]);
                        if (appInfo != null) {
                            Drawable icon = appInfo.loadIcon(mPackageManager);
                            abAppProcessInfo.icon = icon;
                        } else {
                            abAppProcessInfo.icon = mContext.getResources().getDrawable(R.drawable.ic_launcher);
                        }

                    } else {
                        abAppProcessInfo.icon = mContext.getResources().getDrawable(R.drawable.ic_launcher);
                    }
                    abAppProcessInfo.isSystem = true;
                    abAppProcessInfo.appName = appProcessInfo.processName;
                }


                long memsize = mActivityManager.getProcessMemoryInfo(new int[]{appProcessInfo.pid})[0].getTotalPrivateDirty() * 1024;
                abAppProcessInfo.memory = memsize;

                mAppProcessList.add(abAppProcessInfo);
            }


            return mAppProcessList;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mOnActionListener != null) {
                mOnActionListener.onScanProgressUpdated(MemoryCleanService.this, values[0], values[1]);
            }
        }

        @Override
        protected void onPostExecute(List<AppProcessInfo> result) {
            if (mOnActionListener != null) {
                mOnActionListener.onScanCompleted(MemoryCleanService.this, result);
            }

            mIsScanning = false;
        }
    }

    public void scanRunProcess() {
        new TaskScan().execute();
    }


    @TargetApi(Build.VERSION_CODES.FROYO)
    public void killBackgroundProcesses(String processName) {
        String packageName = null;
        try {
            if (processName.indexOf(":") == -1) {
                packageName = processName;
            } else {
                packageName = processName.split(":")[0];
            }
            mActivityManager.killBackgroundProcesses(packageName);
            //
            Method forceStopPackage = mActivityManager.getClass()
                    .getDeclaredMethod("forceStopPackage", String.class);
            forceStopPackage.setAccessible(true);
            forceStopPackage.invoke(mActivityManager, packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class TaskClean extends AsyncTask<Void, Void, Long> {

        @Override
        protected void onPreExecute() {
            if (mOnActionListener != null) {
                mOnActionListener.onCleanStarted(MemoryCleanService.this);
            }
        }

        @Override
        protected Long doInBackground(Void... params) {
            long beforeMemory = 0;
            long endMemory = 0;
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            mActivityManager.getMemoryInfo(memoryInfo);
            beforeMemory = memoryInfo.availMem;
            List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager
                    .getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo info : appProcessList) {
                killBackgroundProcesses(info.processName);
            }
            mActivityManager.getMemoryInfo(memoryInfo);
            endMemory = memoryInfo.availMem;
            return endMemory - beforeMemory;
        }

        @Override
        protected void onPostExecute(Long result) {


            if (mOnActionListener != null) {
                mOnActionListener.onCleanCompleted(MemoryCleanService.this, result);
            }


        }
    }


    public long getAvailMemory(Context context) {
        // 获取android当前可用内存大小
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        mActivityManager.getMemoryInfo(memoryInfo);
        // 当前系统可用内存 ,将获得的内存大小规格化

        return memoryInfo.availMem;
    }

    public void cleanAllProcess() {
        //  mIsCleaning = true;

        new TaskClean().execute();
    }

    public void setOnActionListener(OnProcessActionListener listener) {
        mOnActionListener = listener;
    }

    public ApplicationInfo getApplicationInfo(String processName) {
        if (processName == null) {
            return null;
        }
        List<ApplicationInfo> appList = mPackageManager
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo appInfo : appList) {
            if (processName.equals(appInfo.processName)) {
                return appInfo;
            }
        }
        return null;
    }

    public boolean isScanning() {
        return mIsScanning;
    }

    public boolean isCleaning() {
        return mIsCleaning;
    }


}
