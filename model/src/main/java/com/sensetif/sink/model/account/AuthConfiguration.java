package com.sensetif.sink.model.account;

import com.spicter.curtis.api.property.Property;

public interface AuthConfiguration
{

    /**
     */
    Property<String> backend();

    /**
     */
    Property<String> connectString();
}
