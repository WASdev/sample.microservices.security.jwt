package net.wasdev.securemicroservices;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@RestController
public class GreetingController {

    @RequestMapping("/Test")
    public String greeting(@AuthenticationPrincipal JwtUserDetails userDetails,
                           @RequestParam(value="name", required=false, defaultValue="World") String name) {

        String email = userDetails.getJwt().getClaims().getClaim("email", String.class);

        return "Spring RS App invoked with valid JWT "+name+
                "<br>You authenticated to me as "+userDetails.getUsername()+
                " and had an email of "+email;
    }

}
