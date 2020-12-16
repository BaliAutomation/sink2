package com.sensetif.sink.model.security;

import java.util.Collections;
import java.util.List;
import com.spicter.curtis.api.concern.Concerns;
import com.spicter.curtis.api.identity.Identity;
import com.spicter.curtis.api.identity.StringIdentity;
import com.spicter.curtis.api.injection.scope.Structure;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.unitofwork.UnitOfWorkFactory;
import com.spicter.curtis.api.unitofwork.concern.UnitOfWorkConcern;
import com.spicter.curtis.api.unitofwork.concern.UnitOfWorkPropagation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

@Concerns( UnitOfWorkConcern.class )
@Mixins( { SecurityRepository.ShiroBackedSecurityRepositoryMixin.class } )
public interface SecurityRepository
{
    @UnitOfWorkPropagation
    boolean verifyPassword( String user, String password );

    @UnitOfWorkPropagation
    List<String> findRoleNamesOfUser( String name );

    class ShiroBackedSecurityRepositoryMixin
        implements SecurityRepository
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        @UnitOfWorkPropagation
        public boolean verifyPassword( String userName, String password )
        {
            Subject currentUser = SecurityUtils.getSubject();
            return currentUser.isAuthenticated();
        }

        @UnitOfWorkPropagation
        public List<String> findRoleNamesOfUser( String name )
        {
            if( "admin".equals( name ) )
            {
                return Collections.singletonList("admin");
            }
            return Collections.singletonList("user");
        }
    }
}
