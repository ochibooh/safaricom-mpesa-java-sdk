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

package com.ochibooh.safaricom.mpesa;

import lombok.extern.java.Log;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Level;

@Log
public class MpesaTests {
    @Before
    public void init() {
        Mpesa.init("K5D6AaX0DqIaFlysMJBhL8klGouPQpVg", "d3qquYAPsQlS2mEZ", Mpesa.Environment.SANDBOX);
    }

    @Test
    public void testStk() {
        try {
            Mpesa.getInstance().stkPush(
                    Mpesa.StkPushType.PAY_BILL,
                    "174379",
                    "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919",
                    "0718058057",
                    "https://7da4a70f.ngrok.io/collectionsRequestpay.php",
                    "test",
                    "one")
                    .thenApplyAsync(response -> {
                        try {
                            log.log(Level.INFO, response.toString());
                            Thread.sleep(45000);
                            log.log(Level.INFO, Mpesa.getInstance().stkPushStatus("174379", "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919", response.getCheckoutRequestId()).join().toString());
                        } catch (Exception e) {
                            log.log(Level.SEVERE, e.getMessage(), e);
                        }
                        return response;
                    })
                    .join();
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
