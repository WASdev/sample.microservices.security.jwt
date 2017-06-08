package net.wasdev.securemicroservices;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class RsRestTemplate {

    private final RestTemplate restTemplate;

    public RsRestTemplate(RestTemplateBuilder builder){
        this.restTemplate = builder.build();
        this.restTemplate.setInterceptors(Collections.singletonList(new JWTAuthenticationInterceptor()));
    }

    public String getResponseFromRs(String url) {

        return this.restTemplate.getForObject(url, String.class);
    }
}
