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

import com.ochibooh.safaricom.mpesa.config.MpesaConfig;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.util.logging.Level;

@Log
public class Mpesa {

    public enum Environment {
        SANDBOX, PRODUCTION
    }

    private static Environment environment = Environment.SANDBOX;

    private static String key = null;
    private static String secret = null;
    
    private static Mpesa mpesa = null;

    private MpesaConfig config = new MpesaConfig();

    private io.netty.handler.ssl.SslContext sc = null;

    public Mpesa(@NonNull String consumerKey, @NonNull String consumerSecret, Environment environment) {
        Mpesa.setConfigs(consumerKey, consumerSecret, environment);
    }

    public Mpesa(@NonNull String consumerKey, @NonNull String consumerSecret) {
        Mpesa.setConfigs(consumerKey, consumerSecret, null);
    }

    public static void init(@NonNull String consumerKey, @NonNull String consumerSecret, @NonNull Environment environment) {
        Mpesa.mpesa = new Mpesa(consumerKey, consumerSecret, environment);
    }

    public static void init(@NonNull String consumerKey, @NonNull String consumerSecret) {
        Mpesa.mpesa = new Mpesa(consumerKey, consumerSecret, Environment.SANDBOX);
    }

    private static void setConfigs(@NonNull String consumerKey, @NonNull String consumerSecret, Environment environment) {
        Mpesa.environment = environment != null ? environment : Environment.SANDBOX;
        Mpesa.key = consumerKey;
        Mpesa.secret = consumerSecret;
    }

    public static Mpesa getInstance() {
        if (Mpesa.mpesa == null) {
            Mpesa.mpesa = Mpesa.key != null && !Mpesa.key.isEmpty() && Mpesa.secret != null && !Mpesa.secret.isEmpty() ? new Mpesa(Mpesa.key, Mpesa.secret, Mpesa.environment) : new Mpesa(null, null);
        }
        return Mpesa.mpesa;
    }

    private AsyncHttpClientConfig httpClientConfig() {
        if (this.sc == null) {
            try {
                this.sc = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            } catch (Exception e) {
                log.log(Level.WARNING, String.format("Ssl default context error [ %s ]", e.getMessage()), e);
            }
        }
        return new DefaultAsyncHttpClientConfig.Builder()
                .setMaxConnections(Mpesa.getInstance().config.getMaxConnections())
                .setMaxConnectionsPerHost(Mpesa.getInstance().config.getMaxConnectionsPerHost())
                .setRequestTimeout(Mpesa.getInstance().config.getRequestTimeout())
                .setCompressionEnforced(Mpesa.getInstance().config.isCompressionEnabled())
                .setPooledConnectionIdleTimeout(Mpesa.getInstance().config.getPooledConnectionIdleTimeout())
                .setSslContext(this.sc)
                .build();
    }

    public Mpesa setSslContext(@NonNull io.netty.handler.ssl.SslContext sslContext) {
        try {
            this.sc = sslContext;
        } catch (Exception e) {
            log.log(Level.SEVERE, String.format("Ssl context error [ %s ]", e.getMessage()), e);
        }
        return getInstance();
    }

    private String authenticate() {
        return "";
    }

    public String c2b() {
        return "";
    }

    public String b2c() {
        return "";
    }

    public String b2b() {
        return "";
    }

    public String stkPush() {
        return "";
    }

    public String stkPushStatus() {
        return "";
    }

    public String reversal() {
        return "";
    }

    public String balance() {
        return String.format("[ consumerKey=%s, consumerSecret=%s ]", Mpesa.key, Mpesa.secret);
    }

    public String registerUrl() {
        return "";
    }
}
