package com.sensetif.sink.rest;

import com.sensetif.sink.model.dashboard.Dashboard;
import com.sensetif.sink.model.dashboard.DashboardService;
import com.sensetif.sink.rest.security.ProtectedResource;
import com.spicter.curtis.api.injection.scope.Service;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.unitofwork.concern.UnitOfWorkPropagation;
import com.spicter.curtis.library.shiro.concerns.RequiresPermissions;
import java.net.URI;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.lang.String.format;

@Mixins( DashboardsResource.Mixin.class )
@Path( "" )
@Singleton
@Produces( MediaType.APPLICATION_JSON )
public interface DashboardsResource extends ProtectedResource
{
    @GET
    @Path( "{name}" )
    Response fetch( @PathParam( "name" ) String dashboardName );

    @GET
    Response fetch();

    @POST
    Response create( String dashboardName );

    @PUT
    Response update( Dashboard dashboard );

    @Inject
    void setBaseUri( URI base );  // Injected by JAX-RS

    abstract class Mixin
        implements DashboardsResource
    {
        @Service
        private DashboardService service;
        private URI baseUri;

        public Mixin()
        {
            System.out.println( "NIclas" );
        }

        @Override
        public Response fetch()
        {
            String userName = currentUser();
            List<String> names = service.findAllDashboardNamesOfUser( userName );
            return Response.accepted( names ).build();
        }

        @Override
        @RequiresPermissions( "fetch-own-dashboard" )
        public Response fetch( String dashboardName )
        {
            String userName = currentUser();
            Dashboard dashboard = service.dashboard( userName, dashboardName );
            if( dashboard == null )
            {
                return Response
                    .status( Response.Status.NOT_FOUND )
                    .entity( format( "{ \"error\" : \"You don't have a dashboard named '%s'\" }", dashboardName ) )
                    .build();
            }
            return Response.accepted( dashboard ).build();
        }

        @Override
        public void setBaseUri( URI base )
        {
            this.baseUri = base;
        }

        @Override
        @UnitOfWorkPropagation
        public Response create( String dashboardName )
        {
            String userName = currentUser();
            if( service.create( userName, dashboardName ) )
            {
                URI location = baseUri.resolve( dashboardName );
                return Response.created( location ).build();
            }
            return Response.status( Response.Status.CONFLICT ).entity( format( "Dashboard '%s' already exists.", dashboardName ) ).build();
        }

        @Override
        @UnitOfWorkPropagation
        public Response update( Dashboard dashboard )
        {
            String userName = currentUser();
            service.update( userName, dashboard );
            return Response.accepted().build();
        }
    }
}
