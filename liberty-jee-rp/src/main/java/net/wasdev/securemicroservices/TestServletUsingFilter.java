package net.wasdev.securemicroservices;

import java.io.IOException;
import java.io.PrintWriter;

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

@WebServlet("/Test2")
@ServletSecurity(@HttpConstraint(rolesAllowed = "OIDCUser"))
public class TestServletUsingFilter extends HttpServlet {

	@Resource(lookup = "rsJeeEndpoint")
	String RS_JEE_ENDPOINT;


	@Resource(lookup = "rsSpringEndpoint")
	String RS_SPRING_ENDPOINT;
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {		
		PrintWriter out = response.getWriter();
		out.println("<html><body><h2>RP Signed JWT App</h2><br>");

		out.println("<HR>RS Invocation<p>");
		// call the RS with the jwt we just built.
		out.println("Using RS JEE Endpoint of : " + RS_JEE_ENDPOINT + "<br>");
                out.println("Using RS SPRING Endpoint of : " + RS_SPRING_ENDPOINT + "<br>");

		ClientBuilder cb = ClientBuilder.newBuilder();
		cb.property("com.ibm.ws.jaxrs.client.disableCNCheck", true);
		cb.property("com.ibm.ws.jaxrs.client.ssl.config", "defaultSSLConfig");
		cb.register(JwtJaxRSClientFilter.class);
		
		Client c = cb.build();
		String result = "";
		try {
			result = c.target(RS_JEE_ENDPOINT).request().get(String.class);
			result = "<hr><br>[START_RESPONSE_FROM_JEE_RS]<br>" + result + "<br>[END_RESPONSE_FROM_JEE_RS]";
		} finally {
			c.close();
		}

		c = cb.build();
		try {
			String springresult = c.target(RS_SPRING_ENDPOINT).request().get(String.class);
			result = result + "<hr><br>[START_RESPONSE_FROM_SPRING_RS]<br>" + springresult + "<br>[END_RESPONSE_FROM_SPRING_RS]";
		} finally {
			c.close();
		}

		// in a real app, the response would likely be some json, or
		// info to use within the app processing. But in this example,
		// the response is just some text for us to display back to the 
		// user to show the processing performed by the RS.
		out.println(result);
	}

}
