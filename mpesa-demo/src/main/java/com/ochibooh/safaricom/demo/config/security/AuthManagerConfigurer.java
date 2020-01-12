/*
 * Copyright (c) 2019
 *     Phelix Ochieng(Ochibooh)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ochibooh.safaricom.demo.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
@Configuration
public class AuthManagerConfigurer implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        AtomicReference<Mono<Authentication>> res = new AtomicReference<>(Mono.empty());
        try {
            String token = authentication.getCredentials().toString();
            Claims claims = Jwts.parser().setSigningKey(getKey()).parseClaimsJws(token).getBody();
            if (claims != null && claims.getExpiration().after(Timestamp.from(Instant.now()))) {
                List<String> authorities = new ArrayList<>();
                authorities.add("test");
                return Mono.just(new UsernamePasswordAuthenticationToken("principal", "credential", authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())));
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, String.format("Auth manager error [ %s ]", e.getMessage()), e);
        }
        return res.get();
    }

    private Key getKey() {
        String jwtSecret = "BUs26Ke6H68sj1jV4l8GN26egFQ68aATHiIe0uD9UftsJp0CF49QEMzv7upR8wFqahOzjkIzQoAumA0HiWFzBaxkEF8Jcz2wACbAYiZpFPke1dpZR7kjyiV74ikbRDLfUOWbEkJDhTBhCbBAjvbYFRu69hUQ";
        return Keys.hmacShaKeyFor(Base64.getEncoder().encodeToString(jwtSecret.getBytes(StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8));
    }
}
