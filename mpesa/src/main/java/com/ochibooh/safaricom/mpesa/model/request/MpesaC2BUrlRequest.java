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

package com.ochibooh.safaricom.mpesa.model.request;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Setter
@Getter
@Builder
public class MpesaC2BUrlRequest implements Serializable {
    @SerializedName("ShortCode")
    private String shortCode;

    @SerializedName("ResponseType")
    private String responseType;

    @Builder.Default
    @SerializedName("ConfirmationURL")
    private String confirmationUrl = "";

    @Builder.Default
    @SerializedName("ValidationURL")
    private String validationUrl = "";
}
