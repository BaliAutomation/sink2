package com.sensetif.sink.model.security;

import com.spicter.curtis.api.concern.ConcernOf;
import com.spicter.curtis.api.property.Property;
import com.spicter.curtis.api.injection.scope.Service;

public class EncryptedStringPropertyConcern extends ConcernOf<Property<String>>
    implements Property<String>
{
    @Service
    private CryptoService crypto;

    public String get() {
        String value = next.get();
        return crypto.decrypt( value );
    }

    public void set( String value ) {
        String encrypted = crypto.encrypt( value );
        next.set( value );
    }
}
