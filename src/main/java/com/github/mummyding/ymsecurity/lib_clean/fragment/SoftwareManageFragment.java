package com.github.mummyding.ymsecurity.lib_clean.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mummyding.ymsecurity.lib_clean.R;
import com.github.mummyding.ymsecurity.lib_clean.adapter.SoftwareManageAdapter;
import com.github.mummyding.ymbase.base.BaseFragment;
import com.github.mummyding.ymsecurity.lib_clean.model.AppInfo;
import com.github.mummyding.ymbase.util.StorageUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class SoftwareManageFragment extends BaseFragment {


    Context mContext;
    public static final int REFRESH_BT = 111;
    private static final String ARG_POSITION = "position";
    private int position; // 0:应用软件，2 系统软件
    SoftwareManageAdapter mAutoStartAdapter;

    ListView listview;

    TextView topText;
    List<AppInfo> userAppInfos = null;
    List<AppInfo> systemAppInfos = null;
    View mProgressBar;
    TextView mProgressBarText;

    private Method mGetPackageSizeInfoMethod;

    AsyncTask<Void, Integer, List<AppInfo>> mFetchInstalledAppsTask;
    private View mRootView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getArguments().getInt(ARG_POSITION);


    }

    private void initViews() {
        listview = (ListView) bindView(R.id.listview);
        topText = (TextView) bindView(R.id.topText);
        mProgressBar = bindView(R.id.progressBar);
        mProgressBarText = (TextView) bindView(R.id.progressBarText);
    }


    private View bindView(int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        mRootView = inflater.inflate(R.layout.fragment_software, container, false);
        initViews();
        mContext = getActivity();
        try {
            mGetPackageSizeInfoMethod = mContext.getPackageManager().getClass().getMethod(
                    "getPackageSizeInfo", String.class, IPackageStatsObserver.class);


        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return mRootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        fillData();
    }

    @Override
    public void onPause() {
        super.onPause();
        mFetchInstalledAppsTask.cancel(true);
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void fillData() {

        if (position == 0) {
            topText.setText("");

        } else {
            topText.setText("卸载下列应用，会影响正常使用");
        }


        mFetchInstalledAppsTask = new AsyncTask<Void, Integer, List<AppInfo>>() {
            private int mAppCount = 0;

            @Override
            protected List<AppInfo> doInBackground(Void... params) {
                PackageManager pm = mContext.getPackageManager();
                List<PackageInfo> packInfos = pm.getInstalledPackages(0);
                publishProgress(0, packInfos.size());
                List<AppInfo> appinfos = new ArrayList<AppInfo>();
                for (PackageInfo packInfo : packInfos) {
                    publishProgress(++mAppCount, packInfos.size());
                    final AppInfo appInfo = new AppInfo();
                    Drawable appIcon = packInfo.applicationInfo.loadIcon(pm);
                    appInfo.setAppIcon(appIcon);

                    int flags = packInfo.applicationInfo.flags;

                    int uid = packInfo.applicationInfo.uid;

                    appInfo.setUid(uid);

                    if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        appInfo.setUserApp(false);//系统应用
                    } else {
                        appInfo.setUserApp(true);//用户应用
                    }
                    if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                        appInfo.setInRom(false);
                    } else {
                        appInfo.setInRom(true);
                    }
                    String appName = packInfo.applicationInfo.loadLabel(pm).toString();
                    appInfo.setAppName(appName);
                    String packname = packInfo.packageName;
                    appInfo.setPackname(packname);
                    String version = packInfo.versionName;
                    appInfo.setVersion(version);
                    try {
                        mGetPackageSizeInfoMethod.invoke(mContext.getPackageManager(), new Object[]{
                                packname,
                                new IPackageStatsObserver.Stub() {
                                    @Override
                                    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                                        synchronized (appInfo) {
                                            appInfo.setPkgSize(pStats.cacheSize + pStats.codeSize + pStats.dataSize);

                                        }
                                    }
                                }
                        });
                    } catch (Exception e) {
                    }

                    appinfos.add(appInfo);
                }
                return appinfos;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                try {
                    mProgressBarText.setText(getString(R.string.scanning_m_of_n, values[0], values[1]));
                } catch (Exception e) {

                }

            }

            @Override
            protected void onPreExecute() {
                try {
                    showProgressBar(true);
                    mProgressBarText.setText(R.string.scanning);
                } catch (Exception e) {

                }

                //    loading.setVisibility(View.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(List<AppInfo> result) {

                super.onPostExecute(result);


                try {

                    showProgressBar(false);

                    userAppInfos = new ArrayList<>();
                    systemAppInfos = new ArrayList<>();
                    long allSize = 0;
                    for (AppInfo a : result) {
                        if (a.isUserApp()) {
                            allSize += a.getPkgSize();
                            userAppInfos.add(a);
                        } else {
                            systemAppInfos.add(a);
                        }
                    }
                    if (position == 0) {
                        topText.setText(getString(R.string.software_top_text, userAppInfos.size(), StorageUtil.convertStorage(allSize)));
                        mAutoStartAdapter = new SoftwareManageAdapter(mContext, userAppInfos);
                        listview.setAdapter(mAutoStartAdapter);

                    } else {

                        mAutoStartAdapter = new SoftwareManageAdapter(mContext, systemAppInfos);
                        listview.setAdapter(mAutoStartAdapter);

                    }
                } catch (Exception e) {

                }
            }

        };
        mFetchInstalledAppsTask.execute();


    }


    private boolean isProgressBarVisible() {
        return mProgressBar.getVisibility() == View.VISIBLE;
    }

    private void showProgressBar(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.startAnimation(AnimationUtils.loadAnimation(
                    mContext, android.R.anim.fade_out));
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}