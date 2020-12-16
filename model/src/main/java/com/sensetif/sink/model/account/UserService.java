package com.sensetif.sink.model.account;

import com.spicter.curtis.api.concern.Concerns;
import com.spicter.curtis.api.entity.EntityBuilder;
import com.spicter.curtis.api.identity.Identity;
import com.spicter.curtis.api.identity.StringIdentity;
import com.spicter.curtis.api.injection.scope.Structure;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.service.ServiceActivation;
import com.spicter.curtis.api.unitofwork.NoSuchEntityException;
import com.spicter.curtis.api.unitofwork.UnitOfWork;
import com.spicter.curtis.api.unitofwork.UnitOfWorkFactory;
import com.spicter.curtis.api.unitofwork.concern.UnitOfWorkConcern;
import com.spicter.curtis.api.unitofwork.concern.UnitOfWorkPropagation;
import com.spicter.curtis.api.usecase.UsecaseBuilder;
import com.spicter.curtis.spi.entitystore.EntityNotFoundException;

@Mixins( UserService.Mixin.class )
@Concerns( UnitOfWorkConcern.class )
public interface UserService extends ServiceActivation
{
    User findUserByName( String userName );

    class Mixin
        implements UserService
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        @UnitOfWorkPropagation
        public User findUserByName( String userName )
        {
            String identity = "_USER_" + userName;
            return uowf.currentUnitOfWork().get( User.class, StringIdentity.identityOf( identity ) );
        }

        @Override
        public void activateService() throws Exception
        {
            try( UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "initialization" ) ) )
            {
                Identity rootId = StringIdentity.identityOf( "_USER_root" );
                try
                {
                    User exists = uow.get( User.class, rootId );
                }
                catch( NoSuchEntityException e )
                {
                    EntityBuilder<User> builder = uow.newEntityBuilder( User.class, rootId );
                    builder.instance().name().set( "Administrator" );
                    User.State state = builder.instanceFor( User.State.class );
                    state.country().set( "Sweden" );
                    state.email().set( "niclas@bali.io" );
                    builder.newInstance();
                }
                uow.complete();
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }

        @Override
        public void passivateService() throws Exception
        {
        }
    }
}
