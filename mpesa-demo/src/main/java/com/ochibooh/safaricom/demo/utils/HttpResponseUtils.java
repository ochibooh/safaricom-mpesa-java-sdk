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

package com.ochibooh.safaricom.demo.utils;

import com.ochibooh.safaricom.demo.model.response.ErrorResponse;
import lombok.extern.java.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Log
@Service
public class HttpResponseUtils {
    public ResponseEntity<ErrorResponse> setupErrorResponse(@NonNull Integer statusCode, @NonNull String message, @NonNull HttpStatus status) {
        return new ResponseEntity<>(ErrorResponse.builder().statusCode(statusCode).statusMessage(message).build(), new HttpHeaders(), status);
    }

    public ResponseEntity<?> setupSuccessResponse(@NonNull Object response, @NonNull HttpStatus status) {
        return new ResponseEntity<>(response, status);
    }
}
