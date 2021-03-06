package br.com.androidpro.bollyfilmes;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_geral);

        bindPreferenceSummary(findPreference(getString(R.string.prefs_ordem_key))); //pega o valor da string a partir da key
        bindPreferenceSummary(findPreference(getString(R.string.prefs_idioma_key))); //pega o valor da string a partir da key
    }

    private void bindPreferenceSummary(Preference preference) {
        //adicionar configurações de mudanças de preferências
        preference.setOnPreferenceChangeListener(this);//adiciona a própria classe como listener

        onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String value = newValue.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(value);
        }

        return true;
    }
}