package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopTranslationSQLSettings;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;

import java.util.Optional;
import java.util.Properties;

public class OntopTranslationSQLSettingsImpl extends OntopTranslationSettingsImpl
        implements OntopTranslationSQLSettings {

    private static final String DEFAULT_FILE = "translation-sql-default.properties";
    private final OntopSQLCoreSettings sqlSettings;

    OntopTranslationSQLSettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
        sqlSettings = new OntopSQLCoreSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = OntopSQLCoreSettingsImpl.loadDefaultOBDAProperties();
        properties.putAll(loadDefaultQASQLProperties());
        properties.putAll(userProperties);
        return properties;
    }

    static Properties loadDefaultQASQLProperties() {
        return loadDefaultPropertiesFromFile(OntopTranslationSQLSettings.class, DEFAULT_FILE);
    }

    @Override
    public String getJdbcUrl() {
        return sqlSettings.getJdbcUrl();
    }

    @Override
    public String getJdbcName() {
        return sqlSettings.getJdbcName();
    }

    @Override
    public String getJdbcUser() {
        return sqlSettings.getJdbcUser();
    }

    @Override
    public String getJdbcPassword() {
        return sqlSettings.getJdbcPassword();
    }

    @Override
    public Optional<String> getJdbcDriver() {
        return sqlSettings.getJdbcDriver();
    }
}
