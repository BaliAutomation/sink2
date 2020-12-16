package com.sensetif.sink.model.security;

import com.spicter.curtis.api.concern.Concerns;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.service.ServiceActivation;

import com.spicter.curtis.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;

@Mixins( RealmService.Mixin.class )
public interface RealmService
        extends Realm, ServiceActivation
{
    class Mixin extends SimpleAccountRealm
            implements ServiceActivation
    {

        private final PasswordService passwordService;

        public Mixin()
        {
            super();
            passwordService = new DefaultPasswordService();
            PasswordMatcher matcher = new PasswordMatcher();
            matcher.setPasswordService( passwordService );
            setCredentialsMatcher( matcher );
        }

        public void activateService()
                throws Exception
        {
            // Create a test account
            addAccount( "foo", passwordService.encryptPassword( "bar" ) );
        }

        public void passivateService()
                throws Exception
        {
        }
    }
}