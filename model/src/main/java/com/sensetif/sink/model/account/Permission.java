package com.sensetif.sink.model.account;

import com.spicter.curtis.api.identity.HasIdentity;
import com.spicter.curtis.api.injection.scope.This;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.property.Property;


@Mixins( { Permission.Mixin.class } )
public interface Permission extends HasIdentity
{
    interface State
    {
        Property<String> name();    // TODO: remove sample property
    }

    abstract class Mixin
        implements Permission
    {
        @This
        private State state;        // Reference to private State instance

    }
}
