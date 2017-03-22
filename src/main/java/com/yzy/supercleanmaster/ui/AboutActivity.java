package com.yzy.supercleanmaster.ui;

import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.base.BaseSwipeBackActivity;
import com.yzy.supercleanmaster.utils.AppUtil;

import butterknife.InjectView;

public class AboutActivity extends BaseSwipeBackActivity {

    TextView subVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        bindView(subVersion, R.id.subVersion);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle("关于");
        TextView tv = (TextView) findViewById(R.id.app_information);
        Linkify.addLinks(tv, Linkify.ALL);

        subVersion.setText("V"+ AppUtil.getVersion(mContext));

    }



    private void bindView(View view, int id) {
        view = findViewById(id);
    }
}
