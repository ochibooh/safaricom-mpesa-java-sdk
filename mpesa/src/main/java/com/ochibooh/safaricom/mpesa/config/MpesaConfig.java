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

package com.ochibooh.safaricom.mpesa.config;

import lombok.Getter;

@Getter
public class MpesaConfig {
    /* mpesa base urls */
    private String sandboxBaseUrl = "";
    private String productionBaseUrl = "";

    /* http connections */
    private int maxConnections = 400;
    private int maxConnectionsPerHost = 400;
    private int requestTimeout = 50000;
    private boolean compressionEnabled = true;
    private int pooledConnectionIdleTimeout = 50000;
}
