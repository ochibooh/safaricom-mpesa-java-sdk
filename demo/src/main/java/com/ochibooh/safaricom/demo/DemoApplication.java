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

package com.ochibooh.safaricom.demo;

import com.ochibooh.safaricom.mpesa.Mpesa;
import lombok.extern.java.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Level;

@Log
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

        log.log(Level.INFO, Mpesa.init("init", "init").balance());

        Mpesa mpesa = new Mpesa("new", "new", Mpesa.Environment.PRODUCTION);
        log.log(Level.INFO, mpesa.balance());
        log.log(Level.INFO, Mpesa.getInstance().balance());
    }
}
