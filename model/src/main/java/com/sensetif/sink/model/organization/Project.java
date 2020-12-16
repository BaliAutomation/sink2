package com.sensetif.sink.model.organization;

import com.spicter.curtis.api.identity.HasIdentity;
import com.spicter.curtis.api.injection.scope.This;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.property.Property;


@Mixins( { Project.Mixin.class } )
public interface Project extends HasIdentity
{
    interface State
    {
        Property<String> name();    // TODO: remove sample property
    }

    abstract class Mixin
        implements Project
    {
        @This
        private State state;        // Reference to private State instance

    }
}
