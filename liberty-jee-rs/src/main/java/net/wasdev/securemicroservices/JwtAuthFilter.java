package net.wasdev.securemicroservices;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtToken;

@WebFilter(filterName="JWTFilter", urlPatterns = {"/*"})
public class JwtAuthFilter implements Filter{

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		
		//read the jwt header & reject if missing or empty.
		String jwt = request.getHeader("jwt");		
		if(jwt==null || jwt.isEmpty()){
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
	
		//read the jwt token (it will be validated at the same time)
		JwtConsumer jwtConsumer;
		try {
			//use the consumer 'rpConsumer' declared in server.xml
			jwtConsumer = JwtConsumer.create("rpConsumer");
			JwtToken jwt_Token =  jwtConsumer.createJwt(jwt);
			
			//put the parsed jwt object into the request for the app to use.
			request.setAttribute("jwt", jwt_Token);
			
			//invoke rest of chain
			chain.doFilter(req, resp);	
		} catch (InvalidConsumerException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		} catch (InvalidTokenException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}			
	}

	public void init(FilterConfig arg0) throws ServletException {
	}

}
