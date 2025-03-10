package com.yoshione.fingen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.github.omadahealth.lollipin.lib.PinCompatActivity;

import net.xpece.android.support.preference.PreferenceScreenNavigationStrategy;

public class ActivitySettings extends PinCompatActivity  implements
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback,
        PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback,
        FragmentManager.OnBackStackChangedListener,
        PreferenceScreenNavigationStrategy.ReplaceFragment.Callbacks  {

    private Toolbar mToolbar;
    private TextSwitcher mTitleSwitcher;

    private CharSequence mTitle;

    private FragmentSettings mSettingsFragment;

    private PreferenceScreenNavigationStrategy.ReplaceFragment mReplaceFragmentStrategy;

    @SuppressLint("PrivateResource")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "0"))) {
            case ActivityMain.THEME_LIGHT : setTheme(R.style.AppThemeLight); break;
            case ActivityMain.THEME_DARK : setTheme(R.style.AppThemeDark); break;
            default: setTheme(R.style.AppThemeLight); break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mReplaceFragmentStrategy = new PreferenceScreenNavigationStrategy.ReplaceFragment(this, R.anim.abc_fade_in, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.abc_fade_out);

        if (savedInstanceState == null) {
            mSettingsFragment = FragmentSettings.newInstance(null);
            getSupportFragmentManager().beginTransaction().add(R.id.content, mSettingsFragment, "Settings").commit();
        } else {
            mSettingsFragment = (FragmentSettings) getSupportFragmentManager().findFragmentByTag("Settings");
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Cross-fading title setup.
        mTitle = getTitle();

        mTitleSwitcher = new TextSwitcher(mToolbar.getContext());
        mTitleSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView tv = new AppCompatTextView(mToolbar.getContext());
                TextViewCompat.setTextAppearance(tv, R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
                return tv;
            }
        });
        mTitleSwitcher.setCurrentText(mTitle);

        if (ab != null) {
            ab.setCustomView(mTitleSwitcher);
            ab.setDisplayShowCustomEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }

        // Add to hierarchy before accessing layout params.
//        int margin = Util.dpToPxOffset(this, 16);
//        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mTitleSwitcher.getLayoutParams();
//        lp.leftMargin = margin;
//        lp.rightMargin = margin;

        mTitleSwitcher.setInAnimation(this, R.anim.abc_fade_in);
        mTitleSwitcher.setOutAnimation(this, R.anim.abc_fade_out);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSettingsFragment.onActivityResult(requestCode, resultCode, data);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals("theme")) {
                        ActivitySettings.this.recreate(); // the function you want called
                    }
                }
            };

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);

        if (!mTitle.equals(title)) {
            mTitle = title;

            // Only switch if the title differs. Used for the first hook.
            mTitleSwitcher.setText(title);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.settings, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
//            case R.id.github: {
//                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/consp1racy/android-support-preference"));
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(i);
//                return true;
//            }
//            case R.id.spinner: {
//                Intent i = new Intent(this, SpinnerActivity.class);
//                startActivity(i);
//            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceStartScreen(final PreferenceFragmentCompat preferenceFragmentCompat, final PreferenceScreen preferenceScreen) {
        mReplaceFragmentStrategy.onPreferenceStartScreen(getSupportFragmentManager(), preferenceFragmentCompat, preferenceScreen);
        return true;
//        return false; // Turn off to try ReplaceRoot strategy.
    }

    @Override
    public void onBackStackChanged() {
        mSettingsFragment = (FragmentSettings) getSupportFragmentManager().findFragmentByTag("Settings");
    }

    @Override
    public PreferenceFragmentCompat onBuildPreferenceFragment(final String rootKey) {
        return FragmentSettings.newInstance(rootKey);
    }

    @Override
    public boolean onPreferenceDisplayDialog(PreferenceFragmentCompat preferenceFragmentCompat, Preference preference) {
//        final String key = preference.getKey();
//        DialogFragment f;
//        if (preference instanceof ColorPreference) {
//            f = XpColorPreferenceDialogFragment.newInstance(key);
//        } else {
            return false;
//        }
//
//        f.setTargetFragment(preferenceFragmentCompat, 0);
//        f.show(this.getSupportFragmentManager(), key);
//        return true;
    }

}
