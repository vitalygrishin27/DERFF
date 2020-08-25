package app.services.impl;

import app.Models.Settings;
import app.repository.SettingsRepository;
import app.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsServiceImpl implements SettingsService {

    @Autowired
    SettingsRepository repository;

    @Override
    public void save(Settings settings) {
        repository.saveAndFlush(settings);
    }

    @Override
    public Settings findByKey(String key) {
        return repository.findSettingsByKey(key);
    }

    @Override
    public List<Settings> findAllSettings() {
        return repository.findAll();
    }

    @Override
    public void delete(Settings settings) {
        repository.delete(settings);
    }
}
