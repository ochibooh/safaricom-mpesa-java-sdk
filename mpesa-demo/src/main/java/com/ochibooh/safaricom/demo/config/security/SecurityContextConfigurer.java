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

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@Configuration
public class SecurityContextConfigurer implements ServerSecurityContextRepository {

    @Autowired
    private AuthManagerConfigurer authManagerConfigurer;

    @Override
    public Mono<Void> save(ServerWebExchange serverWebExchange, SecurityContext securityContext) {
        throw new UnsupportedOperationException("This is not supported.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange serverWebExchange) {
        AtomicReference<Mono<SecurityContext>> res = new AtomicReference<>(Mono.empty());
        try {
            String header = serverWebExchange.getRequest().getHeaders().getFirst("Authorization");
            if (header != null && !header.isEmpty()) {
                if (header.startsWith("Bearer")) {
                    String authToken = header.replaceAll(String.format("%s ", "Bearer"), "").trim();
                    Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);
                    res.set(authManagerConfigurer.authenticate(auth).map(SecurityContextImpl::new));
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, String.format("Auth jwt load error [ %s ]", e.getMessage()));
        }
        return res.get();
    }
}
