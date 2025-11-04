package com.github.moosethebrown.shipcontrol;

import android.os.Bundle;
import android.text.InputType;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    private String lastErrorKey = null;

    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        EditTextPreference passPreference = findPreference("brokerPassword");
        if (passPreference != null) {
            passPreference.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            });
        }

        EditTextPreference maxJoystickSpeedPreference = findPreference("maxJoystickSpeed");
        if (maxJoystickSpeedPreference != null) {
            maxJoystickSpeedPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String stringValue = newValue instanceof String ? (String) newValue : String.valueOf(newValue);
                try {
                    int value = Integer.parseInt(stringValue);
                    if (value >= 1 && value <= 10) {
                        lastErrorKey = null;
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }

                lastErrorKey = "maxJoystickSpeed";
                return false;
            });

            maxJoystickSpeedPreference.setOnBindEditTextListener(editText -> {
                if ("maxJoystickSpeed".equals(lastErrorKey)) {
                    editText.setError(getString(R.string.maxJoystickSpeedError));
                    lastErrorKey = null;
                } else {
                    editText.setError(null);
                }
            });
        }
    }
}