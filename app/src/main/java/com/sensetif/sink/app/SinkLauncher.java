package com.sensetif.sink.app;

import com.sensetif.sink.model.account.UserService;
import com.sensetif.sink.model.dashboard.Dashboard;
import com.sensetif.sink.model.dashboard.DashboardService;
import com.sensetif.sink.model.dashboard.DataPointView;
import com.sensetif.sink.model.dashboard.UserDashboards;
import com.sensetif.sink.model.organization.Organization;
import com.sensetif.sink.model.account.AuthConfiguration;
import com.sensetif.sink.model.account.AuthService;
import com.sensetif.sink.model.account.Group;
import com.sensetif.sink.model.account.Permission;
import com.sensetif.sink.model.account.User;
import com.sensetif.sink.rest.AccountsResource;
import com.sensetif.sink.rest.DashboardsResource;
import com.sensetif.sink.rest.DataCollectionResource;
import com.sensetif.sink.rest.DataPushResource;
import com.spicter.curtis.api.common.Visibility;
import com.spicter.curtis.api.structure.Application;
import com.spicter.curtis.bootstrap.Assembler;
import com.spicter.curtis.service.application.BusinessApplicationBuilder;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.glassfish.jersey.message.internal.MessageBodyFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class SinkLauncher
{
    private static final String name = "Sink";
    private static final String version = "0.1";
    private Application.Mode mode = getApplicationMode();

    public static void main( String[] args )
        throws Exception
    {
        enableJulToSlf4j();
        System.out.println( System.getProperties().entrySet().stream().filter(e -> ((String) e.getKey()).startsWith( "java" ) ).collect( Collectors.toList() ));
        BusinessApplicationBuilder builder = new BusinessApplicationBuilder( name, version, getApplicationMode() );
        Application build = builder.withEntityStoreCassandra()
            .withMetrics()
            .withIndexingRdf()
            .withJetty( "localhost", 8080 )
            .withBusinessModule( "accounts", createAccountsModule() )
            .withBusinessModule( "dashboards", createDashboardsModule() )
            .withBusinessModule( "datacollection", createDataCollectionModule() )
            .withBusinessModule( "datapush", createDataPushModule() )
            .withRestResource( "accounts", AccountsResource.class )
            .withRestResource( "dashboards", DashboardsResource.class )
            .withRestResource( "datacollection", DataCollectionResource.class )
            .withRestResource( "datapush", DataPushResource.class )
            .build();
    }

    private static Application.Mode getApplicationMode()
    {
        String mode = System.getenv( "APP_MODE" );
        if( mode == null )
        {
            return Application.Mode.development;
        }
        return Application.Mode.valueOf( mode );
    }

    private static Assembler createAccountsModule()
    {
        return module ->
        {
            module.entities( User.class, Organization.class, Group.class, Permission.class);
            module.configurations( AuthConfiguration.class );
            module.services( AuthService.class, UserService.class ).visibleIn( Visibility.application );
        };
    }

    private static Assembler createDashboardsModule()
    {
        return module -> {
            module.services( DashboardService.class ).visibleIn( Visibility.application );
            module.entities( UserDashboards.class, Dashboard.class );
            module.values( DataPointView.class );
        };
    }

    private static Assembler createDataCollectionModule()
    {
        return module -> {};
    }
    private static Assembler createDataPushModule()
    {
        return module -> {};
    }


    private static void enableJulToSlf4j()
    {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.getLogger( "").setLevel( Level.FINEST);
    }
}
