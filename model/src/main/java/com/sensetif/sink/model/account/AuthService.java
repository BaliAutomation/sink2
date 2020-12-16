package com.sensetif.sink.model.account;

import com.spicter.curtis.api.concern.Concerns;
import com.spicter.curtis.api.configuration.Configuration;
import com.spicter.curtis.api.injection.scope.Service;
import com.spicter.curtis.api.injection.scope.Structure;
import com.spicter.curtis.api.injection.scope.This;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.unitofwork.UnitOfWorkFactory;
import com.spicter.curtis.api.unitofwork.concern.UnitOfWorkConcern;
import com.spicter.curtis.api.unitofwork.concern.UnitOfWorkPropagation;
import com.spicter.curtis.api.value.ValueBuilderFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

@Mixins( { AuthService.Mixin.class } )
@Concerns( UnitOfWorkConcern.class )
public interface AuthService
{

    User currentUser();

    void login( String userName, String password, boolean remember );

    void logout();

    class Mixin
        implements AuthService
    {
        @Service
        private UserService userService;

        @This
        private Configuration<AuthConfiguration> configuration;

        @Structure
        private ValueBuilderFactory vbf;

        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        @UnitOfWorkPropagation
        public User currentUser()
        {
            Subject subject = SecurityUtils.getSubject();
            if( !subject.isAuthenticated() )
            {
                return null;
            }
            Object principal = subject.getPrincipal();
            if( principal == null )
            {
                return null;
            }
            if( principal instanceof String )
            {
                return userService.findUserByName( (String) principal );
            }
            throw new IllegalStateException( "Unknown Principal type: " + principal.getClass() + " => " + principal );
        }

        @Override
        @UnitOfWorkPropagation
        public void login( String username, String password, boolean remember )
        {
            UsernamePasswordToken token = new UsernamePasswordToken( username, password );
            token.setRememberMe( remember );
            Subject currentUser = SecurityUtils.getSubject();
            currentUser.login( token );
        }

        @Override
        @UnitOfWorkPropagation
        public void logout()
        {
            Subject subject = SecurityUtils.getSubject();
            subject.logout();
        }
    }
}

