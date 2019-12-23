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

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
class MpesaUtil {
    public static void writeLog(Mpesa.Environment environment, Level level, String message, Object param) {
        if (environment == Mpesa.Environment.PRODUCTION && level.intValue() >= Level.WARNING.intValue()) {
            log.log(level, message, param);
        } else {
            if (environment == Mpesa.Environment.SANDBOX) {
                log.log(level, message, param);
            }
        }
    }

    public static void writeLog(Mpesa.Environment environment, Level level, String message) {
        if (environment == Mpesa.Environment.PRODUCTION && level.intValue() >= Level.WARNING.intValue()) {
            log.log(level, message);
        } else {
            if (environment == Mpesa.Environment.SANDBOX) {
                log.log(level, message);
            }
        }
    }

    public static String formatPhone(@NonNull Mpesa.Country country, @NonNull String phone) throws Exception {
        AtomicReference<String> res = new AtomicReference<>(null);
        String cc;
        switch (country) {
            case TANZANIA: cc = "TZ"; break;
            case DRC: cc = "CD"; break;
            case GHANA: cc = "GH"; break;
            case EGYPT: cc = "EG"; break;
            case LESOTHO: cc = "LS"; break;
            case MOZAMBIQUE: cc = "MZ"; break;
            case KENYA:
            default: cc = "KE";
        }
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber pn = phoneUtil.parse(phone, cc);
        if (phoneUtil.isValidNumber(pn)) {
            res.set(phoneUtil.format(pn, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL).replace("+", "").replaceAll(" ", ""));
        }
        return res.get();
    }

    public static String stkPushPassword(@NonNull String shortCode, @NonNull String key, @NonNull String timestamp) {
        return Base64.getEncoder().encodeToString(String.format("%s%s%s", shortCode, key, timestamp).getBytes(StandardCharsets.ISO_8859_1));
    }
}
