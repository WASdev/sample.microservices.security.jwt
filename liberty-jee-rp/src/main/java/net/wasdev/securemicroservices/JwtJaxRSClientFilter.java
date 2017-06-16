package net.wasdev.securemicroservices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import com.ibm.websphere.security.jwt.InvalidBuilderException;
import com.ibm.websphere.security.jwt.InvalidClaimException;
import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtBuilder;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtException;
import com.ibm.websphere.security.jwt.JwtToken;
import com.ibm.websphere.security.openidconnect.PropagationHelper;
import com.ibm.websphere.security.openidconnect.token.IdToken;

@Priority(value = Priorities.HEADER_DECORATOR)
public class JwtJaxRSClientFilter implements ClientRequestFilter {

	
	@Override
	public void filter(ClientRequestContext context) throws IOException, WebApplicationException {
		System.out.println("Filter invoked");
		//out.println("Writer interceptor invoked.<br>");
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

			//out.println("Writer Interceptor added token :: "+newJwt+"<br>");
			
			context.getHeaders().putSingle("jwt", newJwt);
			
		} catch (InvalidBuilderException | InvalidClaimException | JwtException e) {
			e.printStackTrace();
			//e.printStackTrace(out);
			throw new WebApplicationException(e);
		}
	}

}
