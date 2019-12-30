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

package com.ochibooh.safaricom.demo.handlers;

import com.ochibooh.safaricom.demo.model.request.stk.StkCallbackRequest;
import com.ochibooh.safaricom.demo.model.response.ErrorResponse;
import com.ochibooh.safaricom.demo.utils.HttpResponseUtils;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@RestController
public class MpesaCallbackHandler {

    @Autowired
    private HttpResponseUtils httpResponseUtils;

    @RequestMapping(
            path = {"/stk/callback"},
            method = {RequestMethod.POST},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<?>> stkCallback(
            @RequestBody(required = false) StkCallbackRequest body
    ) {
        AtomicReference<ResponseEntity<?>> res = new AtomicReference<>(httpResponseUtils.setupErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST));
        log.log(Level.INFO, String.format("MPESA Stk Callback body [ %s ]", body));
        if (body != null && body.getData() != null) {
            res.set(httpResponseUtils.setupSuccessResponse(ErrorResponse.builder().statusCode(HttpStatus.OK.value()).statusMessage("Success").build(), HttpStatus.OK));
        } else {
            res.set(httpResponseUtils.setupSuccessResponse(ErrorResponse.builder().statusCode(HttpStatus.BAD_REQUEST.value()).statusMessage("Invalid request body").build(), HttpStatus.BAD_REQUEST));
        }
        return Mono.just(res.get());
    }
}
