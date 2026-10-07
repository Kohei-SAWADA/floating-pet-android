package com.dot.floatingpet;

import android.app.LocaleManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;
import java.util.Locale;

/** Explicit first-run English, with a persistent user choice and Android 13+ locale integration. */
final class AppLanguage {
    private static final String LANGUAGE = "app_language";
    private static final String INITIALIZED = "app_language_initialized";
    private static final String PLATFORM_INITIALIZED = "platform_language_initialized";
    private AppLanguage() { }
    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("pet", Context.MODE_PRIVATE);
    }
    static Context wrap(Context base) {
        SharedPreferences preferences = prefs(base);
        if (Build.VERSION.SDK_INT >= 33) {
            // Once initialized, the platform owns locale state, including changes made in Settings.
            if (preferences.getBoolean(PLATFORM_INITIALIZED, false) || !Api33.selected(base).isEmpty()) return base;
        }
        String language = preferences.getString(LANGUAGE, "en");
        if (language == null || language.isEmpty()) return base;
        Configuration config = new Configuration(base.getResources().getConfiguration());
        Locale locale = Locale.forLanguageTag(language);
        config.setLocales(new LocaleList(locale));
        config.setLayoutDirection(locale);
        return base.createConfigurationContext(config);
    }
    static void initialize(Context context) {
        SharedPreferences preferences = prefs(context);
        boolean modern = Build.VERSION.SDK_INT >= 33;
        String initializedKey = modern ? PLATFORM_INITIALIZED : INITIALIZED;
        if (preferences.getBoolean(initializedKey, false)) return;
        if (modern && !Api33.selected(context).isEmpty()) {
            // Respect a language chosen in Android Settings before first launch.
            preferences.edit().putBoolean(INITIALIZED, true).putBoolean(PLATFORM_INITIALIZED, true).apply();
            return;
        }
        String language = preferences.getString(LANGUAGE, "en");
        if (language == null) language = "en";
        preferences.edit().putString(LANGUAGE, language).putBoolean(initializedKey, true).apply();
        if (modern) {
            try { Api33.set(context, language); }
            catch (RuntimeException unavailable) {
                preferences.edit().remove(PLATFORM_INITIALIZED).apply();
            }
        }
    }
    static String selected(Context context) {
        if (Build.VERSION.SDK_INT >= 33) return Api33.selected(context);
        String language = prefs(context).getString(LANGUAGE, "en");
        return language == null ? "" : language;
    }
    static void set(Context context, String language) {
        if (!language.isEmpty() && !language.equals("en") && !language.equals("ja")) {
            throw new IllegalArgumentException("Unsupported app language");
        }
        if (Build.VERSION.SDK_INT >= 33) Api33.set(context, language);
        prefs(context).edit().putString(LANGUAGE, language).putBoolean(INITIALIZED, true)
                .putBoolean(PLATFORM_INITIALIZED, Build.VERSION.SDK_INT >= 33).apply();
    }
    private static final class Api33 {
        static String selected(Context context) {
            if (Build.VERSION.SDK_INT < 33) return "";
            LocaleManager manager = context.getSystemService(LocaleManager.class);
            if (manager == null) return "";
            LocaleList locales = manager.getApplicationLocales();
            return locales.isEmpty() ? "" : locales.get(0).getLanguage();
        }
        static void set(Context context, String language) {
            if (Build.VERSION.SDK_INT < 33) throw new IllegalStateException("App language API requires Android 13");
            LocaleManager manager = context.getSystemService(LocaleManager.class);
            if (manager == null) throw new IllegalStateException("App language service unavailable");
            manager.setApplicationLocales(language.isEmpty() ? LocaleList.getEmptyLocaleList() : LocaleList.forLanguageTags(language));
        }
    }
}
