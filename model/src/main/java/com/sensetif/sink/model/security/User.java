
package com.sensetif.sink.model.security;

import com.spicter.curtis.api.association.ManyAssociation;
import com.spicter.curtis.api.concern.Concerns;
import com.spicter.curtis.api.injection.scope.This;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.property.Property;
import com.spicter.curtis.library.shiro.domain.passwords.PasswordSecurable;
import com.spicter.curtis.library.shiro.domain.permissions.RoleAssignee;

@Mixins( { User.GroupsMixin.class } )
public interface User extends PasswordSecurable, RoleAssignee
{
    boolean isMemberOf( Group group );

    interface State
    {
        ManyAssociation<Group> memberOf();
    }

    abstract class GroupsMixin
        implements User
    {
        @This
        private State state;

        public boolean isMemberOf( Group group )
        {
            return state.memberOf().contains( group );
        }
    }
}
