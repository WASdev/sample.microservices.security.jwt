# BLUEMIX SSO BRANCH

**This is the bluemix-sso branch, it will only run in Bluemix as CF, running locally via docker is possible, but not covered.**

This project is the logical successor to the "Signed JWT" parts of [https://github.com/WASdev/sample.microservices.security]

This version shifts the build to maven, uses new Liberty APIs for OIDC and JWT, introduces a Spring version of the RP and RS, 
and can be deployed locally via docker compose, or remotely to Bluemix.

### TLDR: 

Run locally with docker toolbox... (Windows/Mac if using docker-toolbox)
```$xslt
git clone https://github.com/WASdev/sample.microservices.security.2.git jwtsample
cd jwtsample
mvn generate-resources
mvn package
docker-compose up
# Open a browser to https://192.168.99.100:9443/signed-jwt-jee-rp-application/Test
# Open a browser to https://192.168.99.100:9443/signed-jwt-jee-rp-application/Test2
# Open a browser to https://192.168.99.100:9444/signed-jwt-spring-rp-application/Test
```

Run locally with docker native (Linux, Mac, Win10 if using docker native)
```$xslt
git clone https://github.com/WASdev/sample.microservices.security.2.git jwtsample
cd jwtsample
mvn generate-resources
mvn package
# Edit docker-compose.yml, change 192.168.99.100 to 127.0.0.1
docker-compose up
# Open a browser to https://127.0.0.1:9443/signed-jwt-jee-rp-application/Test
# Open a browser to https://127.0.0.1:9443/signed-jwt-jee-rp-application/Test2
# Open a browser to https://127.0.0.1:9444/signed-jwt-spring-rp-application/Test
```

Run in Bluemix...
<br>(_assuming that `yourBlueMixOrg` is unique enough to allow the urls to be unique, 
and that you are using `US South`, with a space of '`dev`', if not, read below._)
```$xslt
git clone https://github.com/WASdev/sample.microservices.security.2.git jwtsample
cd jwtsample
mvn generate-resources
mvn -P bluemix -Dcf.username=your@bluemix.userid.com -Dcf.password=yourBluemixPassword -Dcf.org=yourBluemixOrg package
# Wait a while ;p
# Open a browser to https://yourBluemixOrg-jwtsample-liberty-jee-rp.mybluemix.net/signed-jwt-jee-rp-application/Test
# Open a browser to https://yourBluemixOrg-jwtsample-liberty-jee-rp.mybluemix.net/signed-jwt-jee-rp-application/Test2
# Open a browser to https://yourBluemixOrg-jwtsample-liberty-spring-rp.mybluemix.net/signed-jwt-spring-rp-application/Test
```

## More info... 

Still here? need to know a little more about how this all hangs together? 

Overview.. 
```
  User Browser <-> Spring Relying  <----> Spring Relying Service (RS)
                     Party (RP)    <------------       ^ 
                         ^                      \     /
                         |                       \   /
                         v                        \ /
                   OpenID Provider (OP)            X
                         ^                        / \
                         |                       /   \
                         V                      /     \
                     JEE Relying   <------------       v
  User Browser <--->  Party (RP)   <-------> JEE Relying Service (RS)
```

There are 2 RP's one using Spring, one using plain JEE, each RP talks to both RS's, using the 
same JWT for auth. Each RP is authe'd using the OP.

### Keystores..
The `mvn generate-resources` step builds the keystores that will be used by the other projects.
The keystores are all built using the `keytool-maven-plugin` which runs through the same basic
steps as the previous gradle build used to. 

A self-signed CA is generated, and used to sign a certificate that is then used as the https certificate.
The public key is imported to a trust store (and added with the contents of `cacerts` from the jdk).
This allows the various services to talk to each other using https without ssl complaints. Additionally
the certificate is used to sign the access_token/id_token, and the trust store is used by the rp to verify 
those jwts. Finally the same cert is also used to sign the JWT used as auth by the RS's, which use the 
trust store to verify that JWT too. Each of these use cases (HTTPS / OIDC / JWT) could have used different
public/private keys instead of all sharing the same. But one is sufficient for the purposes of this sample.

The keystores are awkawrd to integrate into a maven build, because you really don't want them being rebuilt
every time, else you'll have to agree to the exceptions in the browser each time, and you'd always have to 
rebuild everything, because if the keystores change, then everyone needs the updated trust stores for 
communications to work. 

So once the keystores are built, they create a marker file in the keystores project to remember they are 
built, and won't rebuild unless that's removed (`mvn clean` will do that).

Each child pom.xml reaches out & copies the generated keystores into itself during the `generate-resources`
phase, they assume they can do this safely because they depend upon the keystores project, although I'm not
 entirely sure how sane that is, it's worked reliably for me for weeks now.
 
### Docker images..
The docker images are built using the fabric8 maven docker plugin, which neatly avoids needing to have a
Dockerfile laying around anywhere. The images are intentionally named to make uploading them to Bluemix
easier, although that step isn't covered in this sample.

If you do want to follow that route, edit pom.xml and alter "yourBluemixNamespace" to be your Bluemix
namespace (and also update the registry URL if you are not using US South)

### Running in Bluemix.
The `-P bluemix` activates the `bluemix` profile in the maven poms, that will build a Liberty server directory,
and use the maven cf plugin to push it to Bluemix as a CF app.

The defaults are good for US South, with a space called 'dev', and when your org name is unique enough
to allow for unique urls to be constructed. (Eg, if your org is called 'project-9658-phase1' this will
be fine, but if it's just called `fred` then it's more likely another fred might follow this tutorial)
 
To update the way they are set, just look in the child pom.xml's and update the `properties` block that
builds `cf.host`, `cf.org`, `cf.target` etc. 

### Liberty OIDC & JWT API

Added to the projects with...

```$xslt
        <dependency>
            <groupId>com.ibm.websphere.appserver.api</groupId>
            <artifactId>com.ibm.websphere.appserver.api.basics</artifactId>
            <version>1.2.9</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.ibm.websphere.appserver.api</groupId>
            <artifactId>com.ibm.websphere.appserver.api.jwt</artifactId>
            <version>1.0.16</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.ibm.websphere.appserver.api</groupId>
            <artifactId>com.ibm.websphere.appserver.api.oidc</artifactId>
            <version>1.0.16</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.ibm.websphere.appserver.api</groupId>
            <artifactId>com.ibm.websphere.appserver.api.oauth</artifactId>
            <version>1.2.16</version>
            <scope>provided</scope>
```

This allows the RP to obtain the id token from the OIDC exchange with the simple line.. 
```
    IdToken id_token = PropagationHelper.getIdToken();
```
Then the id token can be queried for information like `id_token.getSubject()`

Because the `PropagationHelper` needs no arguments, it's ideal to embed into a filter,
that can query the result of the OIDC auth, and embed it into a JWT to pass to the RS's. 

In JEE, this can be achieved with a JAXRS ClientRequestFilter, and in Spring a ClientHttpRequestInterceptor
does much the same.

This sample is using RS256 signed JWT's, many examples for JWT's just use shared-secret HMAC based
signatures, which would allow anyone who can verify the JWT to also alter it. In a scenario where the 
JWT is being used to control access to multiple recieving API's, this is less desirable. RS256 prevents
this by allowing the APIs to verify the JWT with the public key, but are not able to alter it as they 
have no access to the private key to create a new signature. 

RS256 based JWTs present another challenge, that of configuring and loading the keystore for signing
and verification purposes (many samples use HMAC because it's a lot easier to show a shared secret as
a string being used for JWT signing, than it is to show loading a key from a keystore). Liberty's JWT
api allows us to define `JwtBuilder` and `JwtConsumer` in the server.xml, that already has configuration
for keystores & trust stores. Then in the application, we can reference the builders & consumers by id, 
and use them to generate & verify JWT's. 

In this sample, we generate a JWT with a claim carrying the scopes agreed from the OIDC auth. 

```$xslt
    JwtBuilder jwtBuilder = JwtBuilder.create("rsBuilder");

    // add the subject, and scopes from the existing request.
    jwtBuilder.subject(id_token.getSubject());				
    jwtBuilder.claim("scopes", scopes);
    
    // set a very short lifespan for the new jwt of 30 seconds.
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, 30);
    jwtBuilder.expirationTime(calendar.getTime().getTime());

    // build the new encoded token
    JwtToken jwtToken = jwtBuilder.buildJwt();
    String newJwt = jwtToken.compact();
```
The corresponding server.xml definition for the builder looks like.
```$xslt
	<jwtBuilder id="rsBuilder" 
				issuer="https://thesamplerp/"
	  			keyStoreRef="defaultKeyStore"
	  			keyAlias="jwtsampleapp"
	/>
```
Using the jwtBuilder in this way avoids all the code related to loading, 
opening, and unlocking the key from the keystore that's to be used for the
signing over the JWTs. 

We validate the token when received with code looking like.. 

```$xslt
        JwtConsumer jwtConsumer;
        try {
            //use the consumer 'rpConsumer' declared in server.xml
            jwtConsumer = JwtConsumer.create("rpConsumer");
            JwtToken jwt_Token = jwtConsumer.createJwt(jwt);
            
            System.out.println("jwt was valid");
                
            ...
        } catch (InvalidConsumerException | InvalidTokenException e) {
            ...
        }
```

Over in the server.xml we define the builder 
```$xslt
    <jwtConsumer id="rpConsumer"
                 signatureAlgorithm="RS256"
                 issuer="https://thesamplerp/"
                 trustStoreRef="defaultTrustStore"
                 trustedAlias="jwtsampleapp"
    />
```
Where we are specifying a trust store (because we only need to verify the signature, so we 
don't need the private key part from a keystore), and the alias to use. We also hard-set the 
algorithm expected to be used for the JWT here, to avoid any nonsense with JWT headers requesting
encryption of type 'none'.

### Spring..

New to this example are the Spring RP and RS. These are written to serve the same functional purpose
as the RP & RS using JEE, except using the Spring framework for security, rest invocation, token management
etc.

The Spring RP uses Spring's JEE container managed authentication to allow Liberty to continue to 
handle the OIDC authentication, and have the Spring security context be appropriately initialized 
based on the result of that auth. This is a bit of a cheat, and a pure Spring implementation would 
have used Spring's OAuth client with some OIDC customization to talk to the OP. In this case though, 
it's a lot simpler to let Liberty handle that side, as it would also be if you were deploying an app
to Bluemix where you wanted to take advantage of existing Liberty integration for the remote auth
service.

```$xslt
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
            .jee()
                .mappableRoles("OIDCUser");
    }
}
```

The configuration works in conjunction with a `web.xml` in `WEB-INF` that defines the roles
for the war file.

```$xslt
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
		 http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">

    <security-role>
        <role-name>OIDCUser</role-name>
    </security-role>
    <security-constraint>
        <web-resource-collection>
            <web-resource-name>Secured Areas</web-resource-name>
            <url-pattern>/*</url-pattern>
        </web-resource-collection>
        <auth-constraint>
            <role-name>OIDCUser</role-name>
        </auth-constraint>
    </security-constraint>
</web-app>
```

And a little glue over in server.xml to bind the OIDCUser role to ALL_AUTHENTICATED_USERS.. 
```$xslt
	<application type="war" id="signed-jwt-spring-rp-application"
		name="signed-jwt-spring-rp-application" location="${server.config.dir}/apps/signed-jwt-spring-rp-application.war">
		<application-bnd>
			<security-role name="OIDCUser">
				<special-subject type="ALL_AUTHENTICATED_USERS" />
			</security-role>
		</application-bnd>
	</application>
```

The RP uses a Spring ClientHttpRequestInterceptor to add the JWT to the outbound request to the RS,
the interceptor is manually added to the RestTemplate during it's build phase. If we wanted the JWT 
to be added to all requests, we could have registered it globally, and avoided this step, but in this 
case it was neater to be able to show adding the JWT for a particular request. (Useful if you needed
a different bearer token when talking to other services from the RP, and didn't want to send them the
JWT).

The RS uses a GenericFilterBean added to the security configuration as a Filter, where it's able to 
check for the presence of, and verify, the JWT sent in a Http header. 

In each case, we're using Liberty's JWT API to generate & validate the JWT, because that allows 
very easy access to the keystores already defined within the server.xml.



















