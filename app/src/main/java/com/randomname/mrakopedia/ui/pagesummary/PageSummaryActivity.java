package com.randomname.mrakopedia.ui.pagesummary;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.ui.settings.SettingsWorker;
import com.randomname.mrakopedia.ui.views.ToolbarHideRecyclerOnScrollListener;
import com.randomname.mrakopedia.utils.Utils;

import java.lang.reflect.Field;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import codetail.graphics.drawables.LollipopDrawablesCompat;

public class PageSummaryActivity extends AppCompatActivity implements  PageSummaryInterface{

    public static final String PAGE_NAME_EXTRA = "pageNameExtra";
    public static final String PAGE_ID_EXTRA = "pageIdExtra";
    private static final String PAGE_FRAGMENT_TAG = "summaryFragmentTag";

    private PageSummaryFragment fragment;
    private boolean isSelectedMode = false;

    public RecyclerView.OnScrollListener toolbarHideListener;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.copy_toolbar)
    Toolbar copyToolbar;
    @Bind(R.id.toolbarWrapper)
    RelativeLayout toolbarWrapper;
    @Bind(R.id.shadow_view_copy)
    View shadowViewCopy;

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_summary);
        ButterKnife.bind(this);

        String pageTitle = getIntent().getStringExtra(PAGE_NAME_EXTRA);
        String pageId = getIntent().getStringExtra(PAGE_ID_EXTRA);

        if (pageTitle == null && pageId == null) {
            pageTitle = getIntent().getData().getQueryParameter("pageTitle");
        }

        if (pageTitle != null) {
            pageTitle = pageTitle.replaceAll("_", " ");
        }

        setPageSummaryFragment(pageTitle, pageId);
        initToolbar(pageTitle);

        toolbarHideListener = new ToolbarHideRecyclerOnScrollListener(toolbarWrapper);

        if (SettingsWorker.getInstance(this).isKeepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public boolean toolbarIsHidden() {
        return ((ToolbarHideRecyclerOnScrollListener)toolbarHideListener).isHidden();
    }

    @Override
    public void startSelection() {
        setAlphaAnimation(copyToolbar, false);
        setAlphaAnimation(shadowViewCopy, false);

        isSelectedMode = true;
    }

    @Override
    public void stopSelection() {
        setAlphaAnimation(copyToolbar, true);
        setAlphaAnimation(shadowViewCopy, true);

        fragment.cancelSelection();
        isSelectedMode = false;
    }

    @Override
    public RecyclerView.OnScrollListener getToolbarHideListener() {
        return toolbarHideListener;
    }

    private void setAlphaAnimation(final View view, final boolean hide) {
        float from = 0f;
        float to = 0f;

        if (hide) {
            from = 1f;
        } else {
            to = 1f;
        }

        AlphaAnimation alphaAnimation = new AlphaAnimation(from, to);
        alphaAnimation.setDuration(300);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(hide ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.setVisibility(View.VISIBLE);
        view.clearAnimation();
        view.setAnimation(alphaAnimation);
        view.animate();

    }

    @Override
    public void onBackPressed() {
        if (isSelectedMode) {
            stopSelection();
        } else {

            if (fragment.onBackPressed()) {
                super.onBackPressed();
            }
        }
    }

    @OnClick(R.id.copy_btn)
    public void copyBtnClick() {
        fragment.copySelectedText();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initToolbar(String categoryTitle) {
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (categoryTitle != null) {
            getSupportActionBar().setTitle(categoryTitle);
        } else {
            getSupportActionBar().setTitle("");
        }

        copyToolbar.setTitle("Копирование");
        copyToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        copyToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelection();
            }
        });

        TextView titleTextView;

        try {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleTextView = (TextView) f.get(toolbar);

            titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleTextView.setFocusable(true);
            titleTextView.setFocusableInTouchMode(true);
            titleTextView.requestFocus();
            titleTextView.setSingleLine(true);
            titleTextView.setSelected(true);
            titleTextView.setMarqueeRepeatLimit(-1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            findViewById(R.id.copy_btn).setBackgroundDrawable(LollipopDrawablesCompat.getDrawable(getResources(), R.drawable.ripple, getTheme()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Utils.setRippleToToolbarIcon(toolbar, this);
        Utils.setRippleToToolbarIcon(copyToolbar, this);
    }

    private void setPageSummaryFragment(String pageTitle, String pageId) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(PAGE_FRAGMENT_TAG);

        if (frag != null) {
            fragment = (PageSummaryFragment) frag;
            return;
        }

        fragment = PageSummaryFragment.getInstance(pageTitle, pageId);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment, PAGE_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }
}
