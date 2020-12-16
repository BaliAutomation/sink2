package com.sensetif.sink.model.security;

import com.spicter.curtis.api.configuration.ConfigurationComposite;
import com.spicter.curtis.api.property.Property;
import com.spicter.curtis.api.common.Optional;

public interface CryptoConfiguration extends ConfigurationComposite
{
    @Optional
    Property<String> digestAlgorithm();

    @Optional
    Property<String> encryptionAlgorithm();

    Property<String> secret1();

    Property<String> secret2();

}
