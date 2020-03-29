package com.yoshione.fingen;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.XpPreferenceFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.yoshione.fingen.fts.ActivityFtsLogin;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.utils.PrefUtils;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.widgets.CustomPinActivity;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.MultiSelectListPreference;
import net.xpece.android.support.preference.PreferenceDividerDecoration;
import net.xpece.android.support.preference.PreferenceScreenNavigationStrategy;
import net.xpece.android.support.preference.SharedPreferencesCompat;
import net.xpece.android.support.preference.SwitchPreference;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static com.yoshione.fingen.utils.RequestCodes.REQUEST_CODE_SELECT_MODEL;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentSettings extends XpPreferenceFragment implements ICanPressBack {
//private static final String TAG = FragmentSettings.class.getSimpleName();

    private static final int REQUEST_CODE_ENABLE = 11;
    //    private static final int REQUEST_CODE_UNLOCK = 12;
    private static final int REQUEST_CODE_DISABLE = 13;
    // These are used to navigate back and forth between subscreens.
//    private PreferenceScreenNavigationStrategy mPreferenceScreenNavigation;
    private static final Activity[] activities = {null};

    private final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else if (preference instanceof MultiSelectListPreference) {
                preference.setSummary(convertValuesToSummary((MultiSelectListPreference) preference));
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                if (preference.getKey().equals(FgConst.PREF_DEFAULT_DEPARTMENT)) {
                    preference.setSummary(PrefUtils.getDefaultDepartment(getActivity()).getFullName());
                } else {
                    preference.setSummary(stringValue);
                }
            }
            return true;
        }
    };


    public static FragmentSettings newInstance(String rootKey) {
        Bundle args = new Bundle();
        args.putString(FragmentSettings.ARG_PREFERENCE_ROOT, rootKey);
        FragmentSettings fragment = new FragmentSettings();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String[] getCustomDefaultPackages() {
        return new String[]{BuildConfig.APPLICATION_ID};
    }

    @Override
    public void onCreatePreferences2(final Bundle savedInstanceState, final String rootKey) {
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.prefs_general);

        activities[0] = getActivity();

        bindPreferenceSummaryToValue(findPreference(FgConst.PREF_START_TAB));
        bindPreferenceSummaryToValue(findPreference("theme"));
        bindPreferenceSummaryToValue(findPreference(FgConst.PREF_ACCOUNT_CLICK_ACTION));
        bindPreferenceSummaryToValue(findPreference("balance_compare_error"));
        bindPreferenceSummaryToValue(findPreference("autocreate_prerequisites"));
        bindPreferenceSummaryToValue(findPreference("payee_selection_style"));
        bindPreferenceSummaryToValue(findPreference(FgConst.PREF_PIN_LENGTH));
        bindPreferenceSummaryToValue(findPreference(FgConst.PREF_DEFAULT_DEPARTMENT));
        bindPreferenceSummaryToValue(findPreference(FgConst.PREF_FIRST_DAY_OF_WEEK));
        bindPreferenceSummaryToValue(findPreference(FgConst.PREF_PIN_LOCK_TIMEOUT));

        updateAndReturnPinLockState();
        findPreference(FgConst.PREF_PIN_LOCK_ENABLE).setOnPreferenceChangeListener((preference, value) -> {
            boolean isChecked = ((SwitchPreference) preference).isChecked();
            Intent intent = new Intent(activities[0], CustomPinActivity.class);
            intent.putExtra(AppLock.EXTRA_TYPE, !isChecked ? AppLock.ENABLE_PINLOCK : AppLock.DISABLE_PINLOCK);
            activities[0].startActivityForResult(intent, !isChecked ? REQUEST_CODE_ENABLE : REQUEST_CODE_DISABLE);
            return false;
        });
        findPreference("change_pin").setOnPreferenceClickListener(preference -> {
            if (((SwitchPreference) FragmentSettings.this.findPreference(FgConst.PREF_PIN_LOCK_ENABLE)).isChecked()) {
                Intent intent = new Intent(activities[0], CustomPinActivity.class);
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.CHANGE_PIN);
                activities[0].startActivity(intent);
                return true;
            } else {
                return false;
            }
        });
        findPreference(FgConst.PREF_TAB_ORDER).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FragmentTabOrderDialog dialog = new FragmentTabOrderDialog();
                dialog.show(getActivity().getSupportFragmentManager(),"fragment_tab_order");
                return true;
            }
        });
        findPreference(FgConst.PREF_TRANSACTION_EDITOR_CONSTRUCTOR).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FragmentTrEditConstructorDialog dialog = new FragmentTrEditConstructorDialog();
                dialog.show(getActivity().getSupportFragmentManager(),"fragment_tr_edit_constructor_dialog");
                return true;
            }
        });
        findPreference(FgConst.PREF_FTS_CREDENTIALS).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(
                        new Intent(getActivity(), ActivityFtsLogin.class),
                        RequestCodes.REQUEST_CODE_ENTER_FTS_LOGIN);
                return true;
            }
        });
        findPreference(FgConst.PREF_DEFAULT_DEPARTMENT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ActivityList.class);
                intent.putExtra("showHomeButton", false);
                intent.putExtra("model", PrefUtils.getDefaultDepartment(getActivity()));
                intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
                startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
                return true;
            }
        });
        findPreference(FgConst.PREF_RESET_DEFAULT_DEPARTMENT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove(FgConst.PREF_DEFAULT_DEPARTMENT).apply();
                return true;
            }
        });

        // Setup root preference title.
        getPreferenceScreen().setTitle(getActivity().getTitle());

        // Setup root preference key from arguments.
//        getPreferenceScreen().setKey(rootKey);

        PreferenceScreenNavigationStrategy.ReplaceFragment.onCreatePreferences(this, rootKey);

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        mPreferenceScreenNavigation.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Change activity title to preference title. Used with ReplaceFragment strategy.
        getActivity().setTitle(getPreferenceScreen().getTitle());
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference instanceof MultiSelectListPreference) {

            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, convertValuesToSummary((MultiSelectListPreference) preference));
        } else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView listView = getListView();

        // We're using alternative divider.
        listView.addItemDecoration(new PreferenceDividerDecoration(getContext()).drawBottom(true).drawBetweenCategories(false));
        setDivider(null);

        // We don't want this. The children are still focusable.
        listView.setFocusable(false);
    }

    // Here follows ReplaceRoot strategy stuff. ====================================================

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_ENABLE:
                if (updateAndReturnPinLockState())
                    Toast.makeText(getActivity(), getString(R.string.toast_pin_enabled), Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CODE_DISABLE:
                if (!updateAndReturnPinLockState())
                    Toast.makeText(getActivity(), getString(R.string.toast_pin_disabled), Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CODE_SELECT_MODEL:
                if (resultCode == RESULT_OK && data != null) {
                    IAbstractModel model = data.getParcelableExtra("model");
                    PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext())).edit().putString(FgConst.PREF_DEFAULT_DEPARTMENT, String.valueOf(model.getID())).apply();
                }
                break;
        }
    }

    private boolean updateAndReturnPinLockState() {
        LockManager lockManager = LockManager.getInstance();
        boolean pinEnabled = lockManager.getAppLock().isPasscodeSet();
        ((SwitchPreference) findPreference(FgConst.PREF_PIN_LOCK_ENABLE)).setChecked(pinEnabled);
        getPreferenceScreen().findPreference("change_pin").setVisible(pinEnabled);
        getPreferenceScreen().findPreference(FgConst.PREF_PIN_LENGTH).setVisible(!pinEnabled);
        return pinEnabled;
    }

    private String convertValuesToSummary(MultiSelectListPreference preference) {
        String summary = "";
        Set<String> values = SharedPreferencesCompat.getStringSet(
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()),
                preference.getKey(),
                new HashSet<String>());
        if (preference.getKey().equals("autocreate_prerequisites")) {
            Resources res = activities[0].getResources();
            for (String s : values) {
                if (!summary.isEmpty()) summary = summary + ", ";
                if (s.equals("account")) summary = summary + res.getString(R.string.ent_account);
                if (s.equals("amount")) summary = summary + res.getString(R.string.ent_amount);
                if (s.equals("type")) summary = summary + res.getString(R.string.ent_type);
                if (s.equals("payee"))
                    summary = summary + res.getString(R.string.ent_payee_or_payer);
                if (s.equals("category")) summary = summary + res.getString(R.string.ent_category);
            }
        } else {
            summary = values.toString();
            summary = summary.trim().substring(1, summary.length() - 1); // strip []
        }
        return summary;
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals("autocreate_prerequisites")) {
                        bindPreferenceSummaryToValue(findPreference(key));
                    }
                    if (key.equals(FgConst.PREF_DEFAULT_DEPARTMENT)) {
                        bindPreferenceSummaryToValue(findPreference(FgConst.PREF_DEFAULT_DEPARTMENT));
                    }
                    if (key.equals(FgConst.PREF_PIN_LOCK_TIMEOUT)) {
                        long timeout = Long.parseLong(prefs.getString(FgConst.PREF_PIN_LOCK_TIMEOUT, "10"))*1000;
                        LockManager lockManager = LockManager.getInstance();
                        lockManager.getAppLock().setTimeout(timeout);
                        lockManager.getAppLock().setOnlyBackgroundTimeout(true);
                        bindPreferenceSummaryToValue(findPreference(key));
                    }
                }
            };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

}

