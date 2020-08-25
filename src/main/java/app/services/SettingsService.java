package app.services;

import app.Models.Settings;
import java.util.List;

public interface SettingsService {
    void save(Settings settings);
    Settings findByKey(String key);
    List<Settings> findAllSettings();
    void delete(Settings settings);
}

