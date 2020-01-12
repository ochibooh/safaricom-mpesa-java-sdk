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

import com.ochibooh.safaricom.demo.model.response.ErrorResponse;
import com.ochibooh.safaricom.demo.utils.HttpResponseUtils;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log
@RestController
public class DemoHandler {

    @Autowired
    private HttpResponseUtils httpResponseUtils;

    @RequestMapping(
            path = {"/", "/help"},
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<ErrorResponse>> help() {
        ResponseEntity<ErrorResponse> responseEntity = httpResponseUtils.setupErrorResponse(HttpStatus.BAD_REQUEST.value(), "For help and any other questions read the manual.", HttpStatus.BAD_REQUEST);
        return Mono.just(responseEntity);
    }

    @RequestMapping(
            path = {"/stk/callback"},
            method = {RequestMethod.GET},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<ErrorResponse>> errorGet() {
        ResponseEntity<ErrorResponse> responseEntity = httpResponseUtils.setupErrorResponse(HttpStatus.BAD_REQUEST.value(), "Only Http POST requests allowed. RTFM!!!", HttpStatus.BAD_REQUEST);
        return Mono.just(responseEntity);
    }
}
