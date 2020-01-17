package app.services;

import app.Utils.ConfigurationKey;

public interface ConfigurationService {

    String getValue(ConfigurationKey key);
}
