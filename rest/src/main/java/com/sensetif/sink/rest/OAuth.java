package com.sensetif.sink.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class OAuth
{
    private Server server = new Server( 8089 );
    private OAuthProvider provider = new GoogleDevOAuthProvider();
//    private OAuthProvider provider = new GitHubDevOAuthProvider();

    public static void main( String[] args ) throws Exception
    {
        new OAuth().startJetty();
    }

    public void startJetty() throws Exception
    {
        ServletContextHandler context = new ServletContextHandler( ServletContextHandler.SESSIONS );
        context.setContextPath( "/" );
        server.setHandler( context );

        context.addServlet( new ServletHolder( new SigninServlet( provider ) ), "/pink2web/signin" );
        context.addServlet( new ServletHolder( new CallbackServlet( provider ) ), "/pink2web/callback" );

        server.start();
        server.join();
    }

    class SigninServlet extends HttpServlet
    {
        private OAuthProvider provider;

        public SigninServlet( OAuthProvider provider )
        {
            this.provider = provider;
        }

        @Override
        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
        {

            // redirect to google for authorization
            StringBuilder oauthUrl = new StringBuilder().append( provider.authorizationApi() )
                .append( "?client_id=" ).append( provider.clientId() ) // the client id from the api console registration
                .append( "&response_type=code" )
                .append( "&scope=" + provider.userprofileScope() ) // scope is the api permissions we are requesting
                .append( "&redirect_uri=http://localhost:8089/pink2web/callback" ) // the servlet that google redirects to after authorization
                .append( "&state=this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id" )
                .append( "&access_type=offline" ) // here we are asking to access to user's data while they are not signed in
//                .append( "&approval_prompt=force" ) // this requires them to verify which account to use, if they are already signed in
            ;
            resp.sendRedirect( oauthUrl.toString() );
        }
    }

    class CallbackServlet extends HttpServlet
    {
        private OAuthProvider provider;

        public CallbackServlet( OAuthProvider provider )
        {
            this.provider = provider;
        }

        @Override
        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
        {
            if( req.getParameter( "error" ) != null )
            {
                resp.getWriter().println( req.getParameter( "error" ) );
                return;
            }
            String error = req.getParameter( "error" );
            System.out.println( "Error:" + error );
            String code = req.getParameter( "code" );
            System.out.println( "Code:" + code );
            String body = post( provider.tokenaccessApi(), ImmutableMap.<String, String>builder()
                .put( "code", code )
                .put( "client_id", provider.clientId() )
                .put( "client_secret", provider.clientSecret() )
                .put( "redirect_uri", "http://localhost:8089/pink2web/callback" )
                .put( "grant_type", "authorization_code" ).build() );
            System.out.println( body );
// body is something like
//   {
//       "access_token": "ya29.AHES6ZQS-BsKiPxdU_iKChTsaGCYZGcuqhm_A5bef8ksNoU",
//       "token_type": "Bearer",
//       "expires_in": 3600,
//       "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjA5ZmE5NmFjZWNkOGQyZWRjZmFiMjk0NDRhOTgyN2UwZmFiODlhYTYifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiZW1haWxfdmVyaWZpZWQiOiJ0cnVlIiwiZW1haWwiOiJhbmRyZXcucmFwcEBnbWFpbC5jb20iLCJhdWQiOiI1MDgxNzA4MjE1MDIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdF9oYXNoIjoieUpVTFp3UjVDX2ZmWmozWkNublJvZyIsInN1YiI6IjExODM4NTYyMDEzNDczMjQzMTYzOSIsImF6cCI6IjUwODE3MDgyMTUwMi5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImlhdCI6MTM4Mjc0MjAzNSwiZXhwIjoxMzgyNzQ1OTM1fQ.Va3kePMh1FlhT1QBdLGgjuaiI3pM9xv9zWGMA9cbbzdr6Tkdy9E-8kHqrFg7cRiQkKt4OKp3M9H60Acw_H15sV6MiOah4vhJcxt0l4-08-A84inI4rsnFn5hp8b-dJKVyxw1Dj1tocgwnYI03czUV3cVqt9wptG34vTEcV3dsU8",
//       "refresh_token": "1/Hc1oTSLuw7NMc3qSQMTNqN6MlmgVafc78IZaGhwYS-o"
//   }
            ObjectMapper mapper = new ObjectMapper();
            Map form = mapper.readValue( body, Map.class );
            String token = provider.validateAccess(form);
            req.getSession().setAttribute( "access_token", token );
            String json = get( provider.userProfileApi(), token );

            resp.getWriter().println( json );
        }
    }

    public String get( String url, String auth )
        throws ClientProtocolException, IOException
    {
        HttpGet httpGet = new HttpGet( url );
        httpGet.addHeader( "Authorization", auth );
        System.out.println(httpGet);
        System.out.println( Arrays.toString(httpGet.getAllHeaders()));
        return execute( httpGet );
    }

    public String post( String url, Map<String, String> formParameters )
        throws ClientProtocolException, IOException
    {
        HttpPost request = new HttpPost( url );
        request.addHeader( "Accept", "application/json, text/json" );
        List<NameValuePair> pairs = new ArrayList<>();
        for( String key : formParameters.keySet() )
        {
            pairs.add( new BasicNameValuePair( key, formParameters.get( key ) ) );
        }
        request.setEntity( new UrlEncodedFormEntity( pairs ) );
        return execute( request );
    }

    private String execute( HttpRequestBase request )
        throws ClientProtocolException, IOException
    {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = httpClient.execute( request );
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString( entity );
        if( response.getStatusLine().getStatusCode() != 200 )
        {
            throw new RuntimeException( "Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body );
        }
        return body;
    }

    interface OAuthProvider
    {
        String clientId();

        String clientSecret();

        String userprofileScope();

        String authorizationApi();

        String tokenaccessApi();

        String userProfileApi();

        String validateAccess( Map form );
    }

    class GitHubDevOAuthProvider
        implements OAuthProvider
    {
        @Override
        public String clientId()
        {
            return "3008c14a0cd0690865af";
        }

        @Override
        public String clientSecret()
        {
            return "1dd267e895f2555b6a49c8c6ddcaee9f9ec9af17";
        }

        @Override
        public String userprofileScope()
        {
            return "openid%20email";
        }

        @Override
        public String authorizationApi()
        {
            return "https://github.com/login/oauth/authorize";
        }

        @Override
        public String tokenaccessApi()
        {
            return "https://github.com/login/oauth/access_token";
        }

        @Override
        public String userProfileApi()
        {
            return "https://api.github.com/user";
        }

        @Override
        public String validateAccess( Map form )
        {
            String accessToken = (String) form.get( "access_token" );
            String tokenType = (String) form.get( "access_token" );
            String idToken = (String) form.get( "id_token" );
            System.out.println( idToken );
            String token = tokenType + " " + accessToken;
            System.out.println(token);
            return token;
        }
    }

    class GoogleDevOAuthProvider
        implements OAuthProvider
    {
        private String issuer;
        private String authorizationApi;
        private String tokenaccessApi;
        private String userInfoApi;

        public GoogleDevOAuthProvider()
        {
            try
            {
                String discovery = execute( new HttpGet( "https://accounts.google.com/.well-known/openid-configuration" ) );
                ObjectMapper mapper = new ObjectMapper();
                Map doc = mapper.readValue( discovery, Map.class );
                issuer = (String) doc.get("issuer");
                authorizationApi = (String) doc.get("authorization_endpoint");
                tokenaccessApi = (String) doc.get("token_endpoint");
                userInfoApi = (String) doc.get("userinfo_endpoint");
            }
            catch( IOException e )
            {
                issuer =  "https://accounts.google.com";
                authorizationApi = "https://accounts.google.com/o/oauth2/v2/auth";
                tokenaccessApi = "https://oauth2.googleapis.com/token";
                userInfoApi = "https://openidconnect.googleapis.com/v1/userinfo";
            }
        }

        @Override
        public String clientId()
        {
            return "673989973828-b389g7kpfis1gsu08epdb9b3qsgi9rhn.apps.googleusercontent.com";
        }

        @Override
        public String clientSecret()
        {
            return "Qar3vBIgYU0jT1SPplUp74ZF";
        }

        @Override
        public String userprofileScope()
        {
//            return "https://www.googleapis.com/auth/userinfo.profile";
            return "openid%20profile%20email";
        }

        @Override
        public String authorizationApi()
        {
            return authorizationApi;
        }

        @Override
        public String tokenaccessApi()
        {
            return tokenaccessApi;
        }

        @Override
        public String userProfileApi()
        {
            return userInfoApi;
        }

        @Override
        public String validateAccess( Map form )
        {
            try
            {
                String accessToken = (String) form.get( "access_token" );
                String tokenType = (String) form.get( "token_type" );
                String idToken = (String) form.get( "id_token" );
                System.out.println( idToken );
                HttpGet validateToken = new HttpGet( "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken );
                String validation = execute( validateToken );
                System.out.println( validation );
                String token = tokenType + " " + accessToken;
                System.out.println(token);
                return token;
            }
            catch( IOException e )
            {
                return null; // indicate not valid.
            }
        }
    }
}
