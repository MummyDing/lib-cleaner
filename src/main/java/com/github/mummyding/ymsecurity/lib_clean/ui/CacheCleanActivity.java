package com.github.mummyding.ymsecurity.lib_clean.ui;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mummyding.ymbase.QuickReturnType;
import com.github.mummyding.ymbase.QuickReturnListViewOnScrollListener;
import com.githang.statusbar.StatusBarCompat;
import com.github.mummyding.ymsecurity.lib_clean.R;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.github.mummyding.ymsecurity.lib_clean.adapter.CacheCleanAdapter;
import com.github.mummyding.ymbase.base.BaseSwipeBackActivity;
import com.github.mummyding.ymsecurity.lib_clean.model.CacheListItemModel;
import com.github.mummyding.ymbase.model.StorageSize;
import com.github.mummyding.ymsecurity.lib_clean.service.CacheCleanService;
import com.github.mummyding.ymbase.util.StorageUtil;
import com.github.mummyding.ymbase.util.SystemBarTintManager;
import com.github.mummyding.ymbase.util.UIElementsHelper;
import com.github.mummyding.ymbase.widget.textcounter.CounterView;
import com.github.mummyding.ymbase.widget.textcounter.formatters.DecimalFormatter;

import java.util.ArrayList;
import java.util.List;


public class CacheCleanActivity extends BaseSwipeBackActivity implements OnDismissCallback, CacheCleanService.OnActionListener {

    private ActionBar ab;
    private Resources res;
    private int ptotal = 0;
    private int pprocess = 0;


    private CacheCleanService mCleanerService;

    private boolean mAlreadyScanned = false;
    private boolean mAlreadyCleaned = false;

    private ListView mListView;
    private TextView mEmptyView;
    private RelativeLayout header;
    private CounterView textCounter;
    private TextView sufix;
    private View mProgressBar;
    private TextView mProgressBarText;
    private CacheCleanAdapter cacheCleanAdapter;
    private List<CacheListItemModel> mCacheListItemModel = new ArrayList<>();
    private LinearLayout bottom_lin;
    private Button clearButton;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCleanerService = ((CacheCleanService.CleanerServiceBinder) service).getService();
            mCleanerService.setOnActionListener(CacheCleanActivity.this);

            //  updateStorageUsage();

            if (!mCleanerService.isScanning() && !mAlreadyScanned) {
                mCleanerService.scanCache();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCleanerService.setOnActionListener(null);
            mCleanerService = null;
        }
    };


    private void initView() {

        mListView = (ListView) bindView(R.id.listview);

        mEmptyView = (TextView) bindView(R.id.empty);

        header = (RelativeLayout) bindView(R.id.header);


        textCounter = (CounterView) bindView(R.id.textCounter);
        sufix = (TextView) bindView(R.id.sufix);

        mProgressBar = bindView(R.id.progressBar);
        mProgressBarText = (TextView) bindView(R.id.progressBarText);
        bottom_lin = (LinearLayout) bindView(R.id.bottom_lin);
        clearButton = (Button) bindView(R.id.clear_button);
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
        setContentView(R.layout.activity_rublish_clean);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setLogo(getResources().getDrawable(R.drawable.transparent));


        StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.title_bg), true);
        initView();
        applyKitKatTranslucency();

//        StikkyHeaderBuilder.stickTo(mListView).setHeader(header)
//                .minHeightHeaderPixel(0).build();
        res = getResources();


        int footerHeight = mContext.getResources().getDimensionPixelSize(R.dimen.footer_height);

        mListView.setEmptyView(mEmptyView);
        cacheCleanAdapter = new CacheCleanAdapter(mContext, mCacheListItemModel);
        mListView.setAdapter(cacheCleanAdapter);
        mListView.setOnItemClickListener(cacheCleanAdapter);
        mListView.setOnScrollListener(new QuickReturnListViewOnScrollListener(QuickReturnType.FOOTER, null, 0, bottom_lin, footerHeight));
        bindService(new Intent(mContext, CacheCleanService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
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

    @Override
    public void onDismiss(@NonNull ViewGroup viewGroup, @NonNull int[] ints) {

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
    public void onScanCompleted(Context context, List<CacheListItemModel> apps) {
        showProgressBar(false);
        mCacheListItemModel.clear();
        mCacheListItemModel.addAll(apps);
        cacheCleanAdapter.notifyDataSetChanged();
        header.setVisibility(View.GONE);
        if (apps.size() > 0) {
            header.setVisibility(View.VISIBLE);
            bottom_lin.setVisibility(View.VISIBLE);

            long medMemory = mCleanerService != null ? mCleanerService.getCacheSize() : 0;

            StorageSize mStorageSize = StorageUtil.convertStorageSize(medMemory);
            textCounter.setAutoFormat(false);
            textCounter.setFormatter(new DecimalFormatter());
            textCounter.setAutoStart(false);
            textCounter.setStartValue(0f);
            textCounter.setEndValue(mStorageSize.value);
            textCounter.setIncrement(5f); // the amount the number increments at each time interval
            textCounter.setTimeInterval(50); // the time interval (ms) at which the text changes
            sufix.setText(mStorageSize.suffix);
            textCounter.start();
        } else {
            header.setVisibility(View.GONE);
            bottom_lin.setVisibility(View.GONE);
        }

        if (!mAlreadyScanned) {
            mAlreadyScanned = true;
        }

    }

    @Override
    public void onCleanStarted(Context context) {
        if (isProgressBarVisible()) {
            showProgressBar(false);
        }

        if (!CacheCleanActivity.this.isFinishing()) {
            showDialogLoading();
        }
    }

    @Override
    public void onCleanCompleted(Context context, long cacheSize) {
        dismissDialogLoading();
        Toast.makeText(context, context.getString(R.string.cleaned, Formatter.formatShortFileSize(
                mContext, cacheSize)), Toast.LENGTH_LONG).show();
        header.setVisibility(View.GONE);
        bottom_lin.setVisibility(View.GONE);
        mCacheListItemModel.clear();
        cacheCleanAdapter.notifyDataSetChanged();
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


    public void onClickClear() {

        if (mCleanerService != null && !mCleanerService.isScanning() &&
                !mCleanerService.isCleaning() && mCleanerService.getCacheSize() > 0) {
            mAlreadyCleaned = false;

            mCleanerService.cleanCache();
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

    public void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }

}
