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

package com.ochibooh.safaricom.mpesa;

import lombok.Getter;

@Getter
class MpesaConfig {
    /* mpesa base urls */
    private String sandboxBaseUrl = "https://sandbox.safaricom.co.ke/";
    private String productionBaseUrl = "https://api.safaricom.co.ke/";

    /* mpesa endpoints */
    private String endpointAuth = "oauth/v1/generate";
    private String endpointStkPush = "mpesa/stkpush/v1/processrequest";
    private String endpointStkPushStatus = "mpesa/stkpushquery/v1/query";
    private String endpointAccountBalance = "mpesa/accountbalance/v1/query";
    private String endpointTransactionStatus = "mpesa/transactionstatus/v1/query";
    private String endpointReversal = "mpesa/reversal/v1/request";

    /* http connections */
    private int maxConnections = 400;
    private int maxConnectionsPerHost = 400;
    private int requestTimeout = 50000;
    private boolean compressionEnabled = true;
    private int pooledConnectionIdleTimeout = 50000;
}
