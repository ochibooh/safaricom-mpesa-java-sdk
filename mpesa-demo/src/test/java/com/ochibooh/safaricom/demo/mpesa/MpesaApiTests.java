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

package com.ochibooh.safaricom.demo.mpesa;

import com.ochibooh.safaricom.mpesa.Mpesa;
import lombok.extern.java.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.logging.Level;

@Log
@RunWith(SpringRunner.class)
@SpringBootTest
public class MpesaApiTests {

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
                    "https://51efb1a9.ngrok.io/stk/callback",
                    "test",
                    "one")
                    .thenApplyAsync(response -> {
                        try {
                            log.log(Level.INFO, response.toString());
                            Thread.sleep(70000);
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

    @Test
    public void testAccountBalance() {
        try {
            Mpesa.getInstance().balance(
                    new File("/media/ochibooh/data/projects/open-source/safaricom-mpesa/misc/safaricom-mpesa-public-key.cer"),
                    "testapi113",
                    "Safaricom007@",
                    "601514",
                    Mpesa.IdentifierType.ORGANISATION_SHORT_CODE,
                    "https://51efb1a9.ngrok.io/account/timeout",
                    "https://51efb1a9.ngrok.io/account/result",
                    "This is just test")
                    .thenApplyAsync(response -> {
                        log.log(Level.INFO, response.toString());
                        return response;
                    })
                    .join();
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
