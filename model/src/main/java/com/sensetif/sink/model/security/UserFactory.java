package com.sensetif.sink.model.security;

import com.spicter.curtis.api.entity.EntityBuilder;
import com.spicter.curtis.api.injection.scope.Structure;
import com.spicter.curtis.api.injection.scope.Service;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.unitofwork.UnitOfWorkFactory;

import org.apache.shiro.authc.credential.PasswordService;

@Mixins( UserFactory.Mixin.class )
public interface UserFactory
{

    User createNewUser( String username, String password );

    class Mixin
        implements UserFactory
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @Service
        private PasswordService passwordService;

        @Override
        public User createNewUser( String username, String password )
        {
            EntityBuilder<User> userBuilder = uowf.currentUnitOfWork().newEntityBuilder( User.class );
            User user = userBuilder.instance();
            user.subjectIdentifier().set( username );
            user.password().set( passwordService.encryptPassword( password ) );
            return userBuilder.newInstance();
        }
    }
}
