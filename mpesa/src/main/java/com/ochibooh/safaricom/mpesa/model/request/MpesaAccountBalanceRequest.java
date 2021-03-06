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
public class MpesaAccountBalanceRequest implements Serializable {
    @SerializedName("Initiator")
    private String initiator;

    @SerializedName("SecurityCredential")
    private String credential;

    @SerializedName("CommandID")
    private String commandId;

    @SerializedName("PartyA")
    private String partyA;

    @SerializedName("IdentifierType")
    private Integer identifierType;

    @Builder.Default
    @SerializedName("Remarks")
    private String description = "";

    @SerializedName("QueueTimeOutURL")
    private String queueTimeoutUrl;

    @SerializedName("ResultURL")
    private String resultUrl;
}
