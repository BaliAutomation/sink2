package com.sensetif.sink.rest.security;

import com.sensetif.sink.model.account.AuthService;
import com.spicter.curtis.api.injection.scope.Service;
import com.spicter.curtis.api.mixin.Mixins;

// TODO: Figure out how to use library-shiro to do proper logins
@Mixins( ProtectedResource.Mixin.class )
public interface ProtectedResource
{
    String currentUser();

    class Mixin
        implements ProtectedResource
    {
        @Service
        private AuthService auth;

        @Override
        public String currentUser()
        {
            return auth.currentUser().identity().get().toString().substring( 6 );
        }
    }
}


