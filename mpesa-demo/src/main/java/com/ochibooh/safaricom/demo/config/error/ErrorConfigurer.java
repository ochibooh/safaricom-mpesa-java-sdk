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

package com.ochibooh.safaricom.demo.config.error;

import com.google.gson.Gson;
import com.ochibooh.safaricom.demo.model.response.ErrorResponse;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@Configuration
public class ErrorConfigurer {

    @Bean
    @Order(-1)
    public WebExceptionHandler exceptionHandler() {
        return (exchange, t) -> {
            DataBuffer buffer = exchange.getResponse().bufferFactory().allocateBuffer();
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            AtomicReference<ErrorResponse> response = new AtomicReference<>(ErrorResponse.builder().build());
            Gson gson = new Gson();
            try {
                if (t.getMessage().contains(String.valueOf(HttpStatus.UNAUTHORIZED.value()))) {
                    this.setHttpErrorResponse(exchange.getResponse(), response, HttpStatus.UNAUTHORIZED);
                } else if (t.getMessage().contains(String.valueOf(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))) {
                    this.setHttpErrorResponse(exchange.getResponse(), response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                } else if (t.getMessage().contains(String.valueOf(HttpStatus.NOT_FOUND.value()))) {
                    this.setHttpErrorResponse(exchange.getResponse(), response, HttpStatus.NOT_FOUND);
                } else if (t.getMessage().contains(String.valueOf(HttpStatus.METHOD_NOT_ALLOWED.value()))) {
                    this.setHttpErrorResponse(exchange.getResponse(), response, HttpStatus.METHOD_NOT_ALLOWED);
                } else if (t.getMessage().contains(String.valueOf(HttpStatus.BAD_REQUEST.value()))) {
                    this.setHttpErrorResponse(exchange.getResponse(), response, HttpStatus.BAD_REQUEST);
                } else {
                    log.log(Level.WARNING, String.format("Global http error handler not found [ %s ]", t.getMessage()));
                    this.setHttpErrorResponse(exchange.getResponse(), response, HttpStatus.BAD_REQUEST);
                }

                log.log(Level.INFO, String.format("Global http error response [ path=%s, response=%s ]", exchange.getRequest().getPath(), gson.toJson(response.get())));
                return exchange.getResponse().writeWith(Mono.justOrEmpty(buffer.write(gson.toJson(response.get()).getBytes())));
            } catch (Exception e) {
                log.log(Level.SEVERE, String.format("Global http error exception [ %s ]", e.getMessage()));
                this.setHttpErrorResponse(exchange.getResponse(), response, HttpStatus.INTERNAL_SERVER_ERROR);

                log.log(Level.INFO, String.format("Global http error response [ path=%s, response=%s ]", exchange.getRequest().getPath(), gson.toJson(response.get())));
                return exchange.getResponse().writeWith(Mono.justOrEmpty(buffer.write(gson.toJson(response.get()).getBytes())));
            }
        };
    }

    private void setHttpErrorResponse(ServerHttpResponse serverHttpResponse, AtomicReference<ErrorResponse> response, HttpStatus httpStatus) {
        response.get().setStatusCode(httpStatus.value());
        response.get().setStatusMessage(httpStatus.getReasonPhrase());
        serverHttpResponse.setStatusCode(httpStatus);
    }
}
