package com.cherish.settings.fragments;

import static com.cherish.settings.utils.Utils.handleOverlays;

import com.android.internal.logging.nano.MetricsProto;
import static android.os.UserHandle.USER_SYSTEM;
import android.app.UiModeManager;
import android.graphics.Color;
import android.os.Bundle;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.RemoteException;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceManager;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import com.cherish.settings.preferences.CustomSeekBarPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.display.OverlayCategoryPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import android.provider.Settings;
import com.android.settings.R;
import androidx.fragment.app.Fragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import com.android.internal.util.cherish.ThemesUtils;
import com.android.internal.util.cherish.CherishUtils;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class ThemeSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String BRIGHTNESS_SLIDER_STYLE = "brightness_slider_style";
    private static final String UI_STYLE = "ui_style";
    private static final String PREF_PANEL_BG = "panel_bg";
    private static final String PREF_THEME_SWITCH = "theme_switch";
    private static final String QS_HEADER_STYLE = "qs_header_style";
    private static final String ACCENT_COLOR = "accent_color";
    private static final String ACCENT_COLOR_PROP = "persist.sys.theme.accentcolor";
    private static final String GRADIENT_COLOR = "gradient_color";
    private static final String GRADIENT_COLOR_PROP = "persist.sys.theme.gradientcolor";
    static final int DEFAULT_QS_PANEL_COLOR = 0xffffffff;
	static final int DEFAULT = 0xff1a73e8;

    private IOverlayManager mOverlayService;
    private UiModeManager mUiModeManager;
    private ColorPickerPreference mThemeColor;
    private ColorPickerPreference mGradientColor;
    private ListPreference mThemeSwitch;
    private ListPreference mBrightnessSliderStyle;
    private ListPreference mUIStyle;
    private ListPreference mPanelBg;
    private ListPreference mQsHeaderStyle;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.cherish_settings_theme);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mUIStyle = (ListPreference) findPreference(UI_STYLE);
        int UIStyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.UI_STYLE, 0);
        int UIStyleValue = getOverlayPosition(ThemesUtils.UI_THEMES);
        if (UIStyleValue != 0) {
            mUIStyle.setValue(String.valueOf(UIStyle));
        }
        mUIStyle.setSummary(mUIStyle.getEntry());
        mUIStyle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference == mUIStyle) {
                    String value = (String) newValue;
                    Settings.System.putInt(getActivity().getContentResolver(), Settings.System.UI_STYLE, Integer.valueOf(value));
                    int valueIndex = mUIStyle.findIndexOfValue(value);
                    mUIStyle.setSummary(mUIStyle.getEntries()[valueIndex]);
                    String overlayName = getOverlayName(ThemesUtils.UI_THEMES);
                    if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayService);
                    }
                    if (valueIndex > 0) {
                        handleOverlays(ThemesUtils.UI_THEMES[valueIndex],
                                true, mOverlayService);
                    }
                    return true;
                }
                return false;
            }
       });

        mBrightnessSliderStyle = (ListPreference) findPreference(BRIGHTNESS_SLIDER_STYLE);
        int BrightnessSliderStyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BRIGHTNESS_SLIDER_STYLE, 0);
        int BrightnessSliderStyleValue = getOverlayPosition(ThemesUtils.BRIGHTNESS_SLIDER_THEMES);
        if (BrightnessSliderStyleValue != 0) {
            mBrightnessSliderStyle.setValue(String.valueOf(BrightnessSliderStyle));
        }
        mBrightnessSliderStyle.setSummary(mBrightnessSliderStyle.getEntry());
        mBrightnessSliderStyle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference == mBrightnessSliderStyle) {
                    String value = (String) newValue;
                    Settings.System.putInt(getActivity().getContentResolver(), Settings.System.BRIGHTNESS_SLIDER_STYLE, Integer.valueOf(value));
                    int valueIndex = mBrightnessSliderStyle.findIndexOfValue(value);
                    mBrightnessSliderStyle.setSummary(mBrightnessSliderStyle.getEntries()[valueIndex]);
                    String overlayName = getOverlayName(ThemesUtils.BRIGHTNESS_SLIDER_THEMES);
                    if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayService);
                    }
                    if (valueIndex > 0) {
                        handleOverlays(ThemesUtils.BRIGHTNESS_SLIDER_THEMES[valueIndex],
                                true, mOverlayService);
                    }
                    return true;
                }
                return false;
            }
       });

       mPanelBg = (ListPreference) findPreference(PREF_PANEL_BG);
        int mPanelValue = getOverlayPosition(ThemesUtils.PANEL_BG_STYLE);
        if (mPanelValue != -1) {
                mPanelBg.setValue(String.valueOf(mPanelValue + 2));
        } else {
                mPanelBg.setValue("1");
              }
        mPanelBg.setSummary(mPanelBg.getEntry());
        mPanelBg.setOnPreferenceChangeListener(this);

        mQsHeaderStyle = (ListPreference)findPreference(QS_HEADER_STYLE);
        int qsHeaderStyle = Settings.System.getInt(resolver,
                Settings.System.QS_HEADER_STYLE, 0);
        int qsvalueIndex = mQsHeaderStyle.findIndexOfValue(String.valueOf(qsHeaderStyle));
        mQsHeaderStyle.setValueIndex(qsvalueIndex >= 0 ? qsvalueIndex : 0);
        mQsHeaderStyle.setSummary(mQsHeaderStyle.getEntry());
        mQsHeaderStyle.setOnPreferenceChangeListener(this);

        mUiModeManager = getContext().getSystemService(UiModeManager.class);

        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        setupThemeSwitchPref();
        setupAccentPref();
        setupGradientPref();
        }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
         if (preference == mThemeColor) {
            int color = (Integer) objValue;
            String hexColor = String.format("%08X", (0xFFFFFFFF & color));
            SystemProperties.set(ACCENT_COLOR_PROP, hexColor);
            try {
                 mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
             } catch (RemoteException ignored) {
             }
        } else if (preference == mGradientColor) {
            int color = (Integer) objValue;
            String hexColor = String.format("%08X", (0xFFFFFFFF & color));
            SystemProperties.set(GRADIENT_COLOR_PROP, hexColor);
            try {
                 mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
             } catch (RemoteException ignored) {
             }
         } else if (preference == mThemeSwitch) {
            String theme_switch = (String) objValue;
            final Context context = getContext();
            switch (theme_switch) {
                case "1":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.CLEAR_SPRING);
                    break;
                case "2":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "3":
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "4":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "5":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "6":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "7":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "8":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "9":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
            }
            try {
                 mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
             } catch (RemoteException ignored) {
             }
            } else if (preference == mPanelBg) {
                String panelbg = (String) objValue;
                int panelBgValue = Integer.parseInt(panelbg);
                mPanelBg.setValue(String.valueOf(panelBgValue));
                String overlayName = getOverlayName(ThemesUtils.PANEL_BG_STYLE);
                    if (overlayName != null) {
                        handleOverlays(overlayName, false, mOverlayService);
                    }
                    if (panelBgValue > 1) {
                        CherishUtils.showSystemUiRestartDialog(getContext());
                        handleOverlays(ThemesUtils.PANEL_BG_STYLE[panelBgValue -2],
                                true, mOverlayService);
    
                }
                mPanelBg.setSummary(mPanelBg.getEntry());
            } else if (preference == mQsHeaderStyle) {
                String value = (String) objValue;
                Settings.System.putInt(resolver,
                    Settings.System.QS_HEADER_STYLE, Integer.valueOf(value));
                int newIndex = mQsHeaderStyle.findIndexOfValue(value);
                mQsHeaderStyle.setSummary(mQsHeaderStyle.getEntries()[newIndex]);
        }
        return true;
    }

    private void setupThemeSwitchPref() {
        mThemeSwitch = (ListPreference) findPreference(PREF_THEME_SWITCH);
        mThemeSwitch.setOnPreferenceChangeListener(this);
         if (CherishUtils.isThemeEnabled("com.android.theme.clearspring.system")) {
            mThemeSwitch.setValue("9");
        } else if (CherishUtils.isThemeEnabled("com.android.theme.darkgrey.system")) {
            mThemeSwitch.setValue("7");
        } else if (CherishUtils.isThemeEnabled("com.android.theme.pitchblack.system")) {
            mThemeSwitch.setValue("6");
        } else if (CherishUtils.isThemeEnabled("com.android.theme.materialocean.system")) {
            mThemeSwitch.setValue("8");
        } else if (CherishUtils.isThemeEnabled("com.android.theme.chocox.system")) {
            mThemeSwitch.setValue("5");
        } else if (CherishUtils.isThemeEnabled("com.android.theme.bakedgreen.system")) {
            mThemeSwitch.setValue("4");
        } else if (CherishUtils.isThemeEnabled("com.android.theme.solarizeddark.system")) {
            mThemeSwitch.setValue("3");
        } else if (mUiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES) {
            mThemeSwitch.setValue("2");
        } else {
            mThemeSwitch.setValue("1");
        }
    }

    private void setupAccentPref() {
        mThemeColor = (ColorPickerPreference) findPreference(ACCENT_COLOR);
        String colorVal = SystemProperties.get(ACCENT_COLOR_PROP, "-1");
        int color = "-1".equals(colorVal)
                ? DEFAULT
                : Color.parseColor("#" + colorVal);
        mThemeColor.setNewPreviewColor(color);
        mThemeColor.setOnPreferenceChangeListener(this);
    }

    private void setupGradientPref() {
        mGradientColor = (ColorPickerPreference) findPreference(GRADIENT_COLOR);
        String colorVal = SystemProperties.get(GRADIENT_COLOR_PROP, "-1");
        int color = "-1".equals(colorVal)
                ? DEFAULT
                : Color.parseColor("#" + colorVal);
        mGradientColor.setNewPreviewColor(color);
        mGradientColor.setOnPreferenceChangeListener(this);
    }

    private void handleBackgrounds(Boolean state, Context context, int mode, String[] overlays) {
        if (context != null) {
            Objects.requireNonNull(context.getSystemService(UiModeManager.class))
                    .setNightMode(mode);
        }
        for (int i = 0; i < overlays.length; i++) {
            String background = overlays[i];
            try {
                mOverlayService.setEnabled(background, state, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private String getOverlayName(String[] overlays) {
        String overlayName = null;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (CherishUtils.isThemeEnabled(overlay)) {
                overlayName = overlay;
            }
        }
        return overlayName;
    }

private int getOverlayPosition(String[] overlays) {
        int position = -1;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (CherishUtils.isThemeEnabled(overlay)) {
                position = i;
            }
        }
        return position;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CHERISH_SETTINGS;
    }
}