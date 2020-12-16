package com.sensetif.sink.model.organization;

import com.spicter.curtis.api.identity.HasIdentity;
import com.spicter.curtis.api.injection.scope.This;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.property.Property;
import java.time.Instant;


@Mixins( { Organization.Mixin.class } )
public interface Organization extends HasIdentity
{
    interface State
    {
        Property<String> name();
        Property<Instant> created();
    }

    abstract class Mixin
        implements Organization
    {
        @This
        private State state;        // Reference to private State instance

    }
}
