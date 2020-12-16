package com.sensetif.sink.model.account;

import com.sensetif.sink.model.HasName;
import com.sensetif.sink.model.IsOwner;
import com.spicter.curtis.api.common.Optional;
import com.spicter.curtis.api.identity.HasIdentity;
import com.spicter.curtis.api.injection.scope.This;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.property.Property;

@Mixins( { User.Mixin.class } )
public interface User extends HasIdentity, HasName, IsOwner
{
    String email();
    String lastLoginIp();

    interface State
    {
        Property<String> email();

        @Optional
        Property<String> country();

        @Optional
        Property<String> lastLoginIp();
    }

    abstract class Mixin
        implements User
    {
        @This
        private State state;        // Reference to private State instance

        @Override
        public String email()
        {
            return state.email().get();
        }

        @Override
        public String lastLoginIp()
        {
            return state.lastLoginIp().get();
        }
    }
}
