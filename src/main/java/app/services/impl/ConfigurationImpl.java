package app.services.impl;

import app.Utils.ConfigurationKey;
import app.repository.ConfigurationRepository;
import app.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationImpl implements ConfigurationService {

    @Autowired
    ConfigurationRepository repository;

    @Override
    public String getValue(ConfigurationKey key){
        return repository.getValue(key.toString());
    }
}
