package net.wasdev.securemicroservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@RestController
public class GreetingController {

    @Autowired
    RsRestTemplate restTemplate;

    @Value("${RS_JEE_ENDPOINT_URL}")
    String jeersurl;

    @Value("${RS_SPRING_ENDPOINT_URL}")
    String springrsurl;

    @RequestMapping("/Test")
    public String greeting(@RequestParam(value="name", required=false, defaultValue="World") String name) {

        System.out.println("Using jee rs url '"+jeersurl+"'");
        System.out.println("Using spring rs url '"+springrsurl+"'");

        String jeeRsResp = null;
        try {
            jeeRsResp = restTemplate.getResponseFromRs(jeersurl);
        }catch(HttpClientErrorException|HttpServerErrorException e){
            return "JEE RS Invoke failed : "+e.getStatusCode()+" :: "+e.getResponseBodyAsString();
        }
        String springRsResp = null;
        try {
            springRsResp = restTemplate.getResponseFromRs(springrsurl);
        }catch(HttpClientErrorException|HttpServerErrorException e){
            return "SPRING RS Invoke failed : "+e.getStatusCode()+" :: "+e.getResponseBodyAsString();
        }

        return "<html><body>Spring/JEE Authed RP Invoked JEE RS with Response<p><hr>" + jeeRsResp + "<hr>Spring/JEE Authed RP Invoked SPRING RS with Response<p><hr>" + springRsResp + "</body></html>";
    }

    @RequestMapping("/Test2")
    public String greeting2(@RequestParam(value="name", required=false, defaultValue="World") String name) {

        //String rsResp = restTemplate.getResponseFromRs(url);

        return "Greetings "+name;
    }

}
