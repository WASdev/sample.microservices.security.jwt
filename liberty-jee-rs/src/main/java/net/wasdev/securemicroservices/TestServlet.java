package net.wasdev.securemicroservices;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.security.jwt.JwtToken;

@WebServlet("/Test")
public class TestServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();

		//added by the rp when it invoked us.
		String jwtParam = request.getHeader("jwt");			
		out.println("JEE RS App invoked with valid JWT<br>");

		//added by the auth filter after validating the jwt from the rp.
		JwtToken jwt_Token = (JwtToken) request.getAttribute("jwt");
		String email = jwt_Token.getClaims().getClaim("email", String.class);
		out.println("JEE RS JWT had email "+email);
	}
}