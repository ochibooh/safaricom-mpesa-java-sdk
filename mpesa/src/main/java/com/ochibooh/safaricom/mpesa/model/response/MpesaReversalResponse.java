/*
 * Copyright (c) 2020
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

package com.ochibooh.safaricom.mpesa.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.io.Serializable;

@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MpesaReversalResponse implements Serializable {
    @SerializedName("MerchantRequestID")
    private String merchantRequestId;
}
