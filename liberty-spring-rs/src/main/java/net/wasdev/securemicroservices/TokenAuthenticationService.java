package net.wasdev.securemicroservices;

import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static java.util.Collections.emptyList;

public class TokenAuthenticationService {

    static Authentication getAuthentication(HttpServletRequest request)
            throws IOException, ServletException {

        System.out.println("Obtaining JWT for verification");

        //read the jwt header & reject if missing or empty.
        String jwt = request.getHeader("jwt");
        if (jwt == null || jwt.isEmpty()) {
            return null;
        }

        System.out.println("Obtained JWT "+jwt);

        //read the jwt token (it will be validated at the same time)
        JwtConsumer jwtConsumer;
        try {
            //use the consumer 'rpConsumer' declared in server.xml
            jwtConsumer = JwtConsumer.create("rpConsumer");
            JwtToken jwt_Token = jwtConsumer.createJwt(jwt);

            System.out.println("jwt was valid");
            JwtUserDetails jud = new JwtUserDetails(jwt_Token);
            return new UsernamePasswordAuthenticationToken(jud, "", jud.getAuthorities());

        } catch (InvalidConsumerException | InvalidTokenException e) {
            System.out.println("Unable to create jwt due to exception "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
