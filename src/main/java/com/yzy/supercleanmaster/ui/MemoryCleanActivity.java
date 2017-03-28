package com.yzy.supercleanmaster.ui;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mummyding.ymbase.QuickReturnType;
import com.github.mummyding.ymbase.QuickReturnListViewOnScrollListener;
import com.github.mummyding.ymbase.view.waveview.WaveView;
import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.adapter.MemoryCleanAdapter;
import com.github.mummyding.ymbase.base.BaseSwipeBackActivity;
import com.github.mummyding.ymbase.model.AppProcessInfo;
import com.github.mummyding.ymbase.model.StorageSize;
import com.yzy.supercleanmaster.service.MemoryCleanService;
import com.github.mummyding.ymbase.util.StorageUtil;
import com.github.mummyding.ymbase.util.SystemBarTintManager;
import com.github.mummyding.ymbase.util.T;
import com.github.mummyding.ymbase.util.UIElementsHelper;
import com.github.mummyding.ymbase.widget.textcounter.CounterView;
import com.github.mummyding.ymbase.widget.textcounter.formatters.DecimalFormatter;

import java.util.ArrayList;
import java.util.List;



public class MemoryCleanActivity extends BaseSwipeBackActivity implements MemoryCleanService.OnPeocessActionListener {

    ActionBar ab;

    ListView mListView;

    WaveView mwaveView;
    RelativeLayout header;
    List<AppProcessInfo> mAppProcessInfos = new ArrayList<>();
    MemoryCleanAdapter mMemoryCleanAdapter;

    CounterView textCounter;
    TextView sufix;
    public long Allmemory;

    LinearLayout bottom_lin;

    View mProgressBar;
    TextView mProgressBarText;

    Button clearButton;
    private static final int INITIAL_DELAY_MILLIS = 300;

    private MemoryCleanService mMemoryCleanService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMemoryCleanService = ((MemoryCleanService.ProcessServiceBinder) service).getService();
            mMemoryCleanService.setOnActionListener(MemoryCleanActivity.this);
            mMemoryCleanService.scanRunProcess();
            //  updateStorageUsage();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMemoryCleanService.setOnActionListener(null);
            mMemoryCleanService = null;
        }
    };

    private void initViews() {
        mListView = (ListView) bindView(R.id.listview);
        mwaveView = (WaveView) bindView(R.id.wave_view);
        header = (RelativeLayout) bindView(R.id.header);
        textCounter = (CounterView) bindView(R.id.textCounter);
        sufix = (TextView) bindView(R.id.sufix);
        bottom_lin = (LinearLayout) bindView(R.id.bottom_lin);
        mProgressBar = bindView(R.id.progressBar);
        mProgressBarText = (TextView) bindView(R.id.progressBarText);
//        clearButton = (Button) bindView(R.id.clear_button);
        clearButton =  (Button) findViewById(R.id.clear_button);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickClear();
            }
        });
    }

    private View bindView(int id) {
        return findViewById(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_clean);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //  applyKitKatTranslucency();
        initViews();
        mMemoryCleanAdapter = new MemoryCleanAdapter(mContext, mAppProcessInfos);
        mListView.setAdapter(mMemoryCleanAdapter);
        bindService(new Intent(mContext, MemoryCleanService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
        int footerHeight = mContext.getResources().getDimensionPixelSize(R.dimen.footer_height);
        mListView.setOnScrollListener(new QuickReturnListViewOnScrollListener(QuickReturnType.FOOTER, null, 0, bottom_lin, footerHeight));
        textCounter.setAutoFormat(false);
        textCounter.setFormatter(new DecimalFormatter());
        textCounter.setAutoStart(false);
        textCounter.setIncrement(5f); // the amount the number increments at each time interval
        textCounter.setTimeInterval(50); // the time interval (ms) at which the text changes
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Apply KitKat specific translucency.
     */
    private void applyKitKatTranslucency() {

        // KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager mTintManager = new SystemBarTintManager(this);
            mTintManager.setStatusBarTintEnabled(true);
            mTintManager.setNavigationBarTintEnabled(true);
            // mTintManager.setTintColor(0xF00099CC);

            mTintManager.setTintDrawable(UIElementsHelper
                    .getGeneralActionBarBackground(this));

            getActionBar().setBackgroundDrawable(
                    UIElementsHelper.getGeneralActionBarBackground(this));

        }

    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    @Override
    public void onScanStarted(Context context) {
        mProgressBarText.setText(R.string.scanning);
        showProgressBar(true);
    }

    @Override
    public void onScanProgressUpdated(Context context, int current, int max) {
        mProgressBarText.setText(getString(R.string.scanning_m_of_n, current, max));
    }

    @Override
    public void onScanCompleted(Context context, List<AppProcessInfo> apps) {
        mAppProcessInfos.clear();

        Allmemory = 0;
        for (AppProcessInfo appInfo : apps) {
            if (!appInfo.isSystem) {
                mAppProcessInfos.add(appInfo);
                Allmemory += appInfo.memory;
            }
        }


        refeshTextCounter();

        mMemoryCleanAdapter.notifyDataSetChanged();
        showProgressBar(false);


        if (apps.size() > 0) {
            header.setVisibility(View.VISIBLE);
            bottom_lin.setVisibility(View.VISIBLE);


        } else {
            header.setVisibility(View.GONE);
            bottom_lin.setVisibility(View.GONE);
        }
//        mMemoryCleanAdapter = new MemoryCleanAdapter(mContext,
//                apps);  mMemoryCleanAdapter = new MemoryCleanAdapter(mContext,
//                apps);
//        swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(mMemoryCleanAdapter, MemoryCleanActivity.this));
//        swingBottomInAnimationAdapter.setAbsListView(mListView);
//        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
//        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);
//
//        mListView.setAdapter(swingBottomInAnimationAdapter);
        //clearMem.setText("200M");


    }

    private void refeshTextCounter() {
        mwaveView.setProgress(20);
        StorageSize mStorageSize = StorageUtil.convertStorageSize(Allmemory);
        textCounter.setStartValue(0f);
        textCounter.setEndValue(mStorageSize.value);
        sufix.setText(mStorageSize.suffix);
        //  textCounter.setSuffix(mStorageSize.suffix);
        textCounter.start();
    }

    @Override
    public void onCleanStarted(Context context) {

    }

    @Override
    public void onCleanCompleted(Context context, long cacheSize) {

    }


    public void onClickClear() {
        long killAppmemory = 0;


        for (int i = mAppProcessInfos.size() - 1; i >= 0; i--) {
            if (mAppProcessInfos.get(i).checked) {
                killAppmemory += mAppProcessInfos.get(i).memory;
                mMemoryCleanService.killBackgroundProcesses(mAppProcessInfos.get(i).processName);
                mAppProcessInfos.remove(mAppProcessInfos.get(i));
                mMemoryCleanAdapter.notifyDataSetChanged();
            }
        }
        Allmemory = Allmemory - killAppmemory;
        T.showLong(mContext, "共清理" + StorageUtil.convertStorage(killAppmemory) + "内存");
        if (Allmemory > 0) {
            refeshTextCounter();
        }


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
    public void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}
