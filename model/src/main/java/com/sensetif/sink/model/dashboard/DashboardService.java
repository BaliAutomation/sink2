package com.sensetif.sink.model.dashboard;

import com.sensetif.sink.model.IsOwner;
import com.sensetif.sink.model.account.User;
import com.sensetif.sink.model.account.UserService;
import com.spicter.curtis.api.concern.Concerns;
import com.spicter.curtis.api.entity.EntityBuilder;
import com.spicter.curtis.api.identity.Identity;
import com.spicter.curtis.api.identity.StringIdentity;
import com.spicter.curtis.api.injection.scope.Service;
import com.spicter.curtis.api.injection.scope.Structure;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.unitofwork.NoSuchEntityException;
import com.spicter.curtis.api.unitofwork.UnitOfWork;
import com.spicter.curtis.api.unitofwork.UnitOfWorkFactory;
import com.spicter.curtis.api.unitofwork.concern.UnitOfWorkConcern;
import com.spicter.curtis.api.unitofwork.concern.UnitOfWorkPropagation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Mixins( DashboardService.Mixin.class )
@Concerns( UnitOfWorkConcern.class )
public interface DashboardService
{
    List<String> findAllDashboardNamesOfUser( String userName );

    Dashboard dashboard( String userName, String dashboardName );

    boolean create( String userName, String dashboardName );

    void update( String userName, Dashboard dashboard );

    class Mixin
        implements DashboardService
    {
        @Service
        private UserService userService;

        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        @UnitOfWorkPropagation
        public List<String> findAllDashboardNamesOfUser( String userName )
        {
            Stream<Dashboard> list = findUserDashboards( userName );
            return list.map( d -> d.name().get() ).collect( toList() );
        }

        @Override
        @UnitOfWorkPropagation
        public Dashboard dashboard( String userName, String dashboardName )
        {
            Stream<Dashboard> list = findUserDashboards( userName );
            Optional<Dashboard> optional = list.filter( d -> d.name().get().equals( dashboardName ) ).findAny();
            if( optional.isPresent() )
            {
                Dashboard dashboard = optional.get();
                return uowf.currentUnitOfWork().toValue( Dashboard.class, dashboard );
            }
            return null;
        }

        @Override
        public boolean create( String userName, String dashboardName )
        {
            Stream<Dashboard> list = findUserDashboards( userName );
            UnitOfWork uow = uowf.currentUnitOfWork();
            boolean existingDashboard = list.anyMatch( d -> d.name().get().equals( dashboardName ) );
            if( existingDashboard )
            {
                return false;
            }
            EntityBuilder<Dashboard> builder = uow.newEntityBuilder( Dashboard.class );
            Dashboard initialize = builder.instance();
            initialize.name().set( dashboardName );
            initialize.owner().set( (IsOwner) list );
            builder.newInstance();
            return true;
        }

        private Stream<Dashboard> findUserDashboards( String userName )
        {
            Identity identity = StringIdentity.identityOf( "_DASHBOARDS_" + userName );
            try
            {
                return uowf.currentUnitOfWork()
                    .get( UserDashboards.class, identity )
                    .dashboards()
                    .stream();
            }
            catch( NoSuchEntityException e )
            {
                UserDashboards dashboards = uowf.currentUnitOfWork().newEntity( UserDashboards.class, identity );
                return dashboards.dashboards().stream();
            }
        }

        @Override
        @UnitOfWorkPropagation
        public void update( String userName, Dashboard dashboard )
        {
            User user = userService.findUserByName( userName );
            UnitOfWork uow = uowf.currentUnitOfWork();
            Dashboard d = uow.get( Dashboard.class, dashboard.identity().get() ); // verify existence before modifying it.
            if( d != null && d.owner().get().equals( user ) )
            {
                uow.toEntity( Dashboard.class, dashboard );
            }
        }
    }
}
