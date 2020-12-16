package com.sensetif.sink.model.account;

import com.spicter.curtis.api.identity.HasIdentity;
import com.spicter.curtis.api.injection.scope.This;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.property.Property;


@Mixins( { Group.Mixin.class } )
public interface Group extends HasIdentity
{
    interface State
    {
        Property<String> name();    // TODO: remove sample property
    }

    abstract class Mixin
        implements Group
    {
        @This
        private State state;        // Reference to private State instance

    }
}
