package net.wasdev.securemicroservices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import com.ibm.websphere.security.jwt.*;
import com.ibm.websphere.security.openidconnect.PropagationHelper;
import com.ibm.websphere.security.openidconnect.token.IdToken;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class JWTAuthenticationInterceptor implements ClientHttpRequestInterceptor {

    private String getJwt(){
        // ask liberty for the id token from the oauth/oidc exchange protecting
        // this invocation.
        IdToken id_token = PropagationHelper.getIdToken();

        // use liberty to build the new jwt, 'rsBuilder' identifies the jwtBuilder
        // defined in server.xml which already knows which keystore / key to use
        // to sign the jwt.
        JwtBuilder jwtBuilder;
        try {
            jwtBuilder = JwtBuilder.create("rsBuilder");

            // add the subject, and scopes from the existing request.
            jwtBuilder.subject(id_token.getSubject());
            jwtBuilder.claim("email", id_token.getClaim("emailAddress"));

            // set a very short lifespan for the new jwt of 30 seconds.
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 30);
            jwtBuilder.expirationTime(calendar.getTime().getTime());

            // build the new encoded token
            JwtToken jwtToken = jwtBuilder.buildJwt();
            String newJwt = jwtToken.compact();

            return newJwt;

        } catch (InvalidBuilderException | InvalidClaimException | JwtException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        HttpHeaders headers = request.getHeaders();
        headers.add("jwt", getJwt());
        return execution.execute(request, body);
    }
}