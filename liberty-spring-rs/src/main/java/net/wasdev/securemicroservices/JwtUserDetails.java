package net.wasdev.securemicroservices;

import com.ibm.websphere.security.jwt.JwtToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class JwtUserDetails implements UserDetails{

    private JwtToken jwt;
    private Set<GrantedAuthority> authorities;

    public JwtUserDetails(JwtToken jwt){
        this.jwt = jwt;

        //we'll use the scopes claim to init our authorities,
        //this is not required, but just shows one way to link the jwt
        //into spring.
        authorities = new HashSet<>();
        String scopes = jwt.getClaims().getClaim("scope", String.class);
        if(scopes!=null && !scopes.isEmpty()){
            for(String scope : scopes.split(",")){
                final boolean add = authorities.add(new SimpleGrantedAuthority(scope));
            }
        }
    }

    public JwtToken getJwt(){
        return jwt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return jwt.getClaims().getSubject();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        //consider the credentials as expired once the token expiry time has passed
        //remember to keep all times in UTC, and optionally allow an extra amount
        //to accomodate clock drift.

        Instant now = Instant.now();

        //for now, assume issued AND expiry are set, we do control the jwt creation =)
        Instant expiry = Instant.ofEpochSecond(jwt.getClaims().getExpiration());
        Instant issuedAt = Instant.ofEpochSecond(jwt.getClaims().getIssuedAt());

        return now.isAfter(issuedAt) && now.isBefore(expiry);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
