package net.wasdev.securemicroservices;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/CheckEnv")
public class CheckEnv extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = response.getWriter();

        out.println("AUTH_ENDPOINT_URL :: "+System.getenv("AUTH_ENDPOINT_URL"));
        out.println("TOKEN_ENDPOINT_URL :: "+System.getenv("TOKEN_ENDPOINT_URL"));
        out.println("RS_ENDPOINT_URL  :: "+System.getenv("RS_ENDPOINT_URL"));

        out.flush();
        out.close();

    }
}
