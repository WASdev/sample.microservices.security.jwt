package net.wasdev.securemicroservices;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

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

@WebServlet("/Test")
@ServletSecurity(@HttpConstraint(rolesAllowed = "OIDCUser"))
public class TestServletWithoutUsingFilter extends HttpServlet {

	@Resource(lookup = "rsJeeEndpoint")
	String RS_ENDPOINT;
	
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		out.println("<html><body><h2>RP Signed JWT App</h2><br>");

		// ask liberty for the id token from the oauth/oidc exchange protecting
		// this invocation.
		IdToken id_token = PropagationHelper.getIdToken();
		
		// lets extract the scopes from the access token, we can send them along to the rs.
		ArrayList<String> scopes=new ArrayList<>();
		out.println("<HR>Access Token Introspection<p>");
		out.println("AccessToken Type : "+PropagationHelper.getAccessTokenType()+"<br>");
		// process the access_token as a jwt to obtain scopes.	
		try {
			JwtConsumer jwtConsumer = JwtConsumer.create("oidcConsumer");
			JwtToken access_Token =  jwtConsumer.createJwt(PropagationHelper.getAccessToken());
			out.println("AccessToken: "+"<br>");
			for(Entry<String, Object> e : access_Token.getClaims().entrySet()){
				out.println(" - "+e.getKey()+" :: "+e.getValue()+"<br>");
			}
			scopes = access_Token.getClaims().getClaim("scope", ArrayList.class);
		} catch (InvalidConsumerException | InvalidTokenException e1) {
			e1.printStackTrace();
			e1.printStackTrace(out);
		}
		
		out.println("<HR>ID Token Introspection<p>");
		out.println("ID Token was : " + id_token + "<br>");
		out.println("Building new JWT to call RS with <br>");

		// use liberty to build the new jwt, 'rsBuilder' identifies the jwtBuilder 
		// defined in server.xml which already knows which keystore / key to use
		// to sign the jwt.
		JwtBuilder jwtBuilder;
		try {			
			jwtBuilder = JwtBuilder.create("rsBuilder");

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

			// print the new jwt out to the browser so we can follow along
			// with the progress =)
			out.println("Have built new JWT " + newJwt +"<br>");
		
			out.println("<HR>RS Invocation<p>");
			// call the RS with the jwt we just built.		
			out.println("Using RS Endpoint of : "+RS_ENDPOINT+"<br>");
			
			
			ClientBuilder cb = ClientBuilder.newBuilder();
			cb.property("com.ibm.ws.jaxrs.client.disableCNCheck", true);
			cb.property("com.ibm.ws.jaxrs.client.ssl.config", "defaultSSLConfig");
			
			Client c = cb.build();
			String result = "";
			try{
				result = c.target(RS_ENDPOINT)
						  .request()
						  .header("jwt", newJwt)
						  .get(String.class);
			}finally{
				c.close();
			}
			
			// in a real app, the response would likely be some json, or
			// info to use within the
			// app processing. But in this example, the response is just
			// some text for us to display
			// back to the user to show the processing performed by the RS.
			out.println("Response from RS was <br><br>[START_RESPONSE_FROM_RS]<br>" + result
					+ "<br>[END_RESPONSE_FROM_RS]");

		} catch (InvalidBuilderException | InvalidClaimException | JwtException e) {
			e.printStackTrace();
			e.printStackTrace(out);
		}
	}

}