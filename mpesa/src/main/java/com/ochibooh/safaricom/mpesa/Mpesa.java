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

import com.google.gson.Gson;
import com.ochibooh.safaricom.mpesa.model.request.*;
import com.ochibooh.safaricom.mpesa.model.response.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.NonNull;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.RequestBuilder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class Mpesa {

    public enum Environment {
        SANDBOX, PRODUCTION
    }

    public enum StkPushType {
        BUY_GOODS, PAY_BILL
    }

    public enum IdentifierType {
        MSISDN, TILL_NUMBER, ORGANISATION_SHORT_CODE, RECEIVER_ORGANISATION_IDENTIFIER_ON_MPESA
    }

    public enum C2BUrlResponseType {
        CANCELED, COMPLETED
    }

    private static Environment environment = Environment.SANDBOX;

    private static String key = null;
    private static String secret = null;
    
    private static Mpesa mpesa = null;
    private static String token = null;
    private static Timestamp tokenExpiry = Timestamp.from(Instant.now());

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
                MpesaUtil.writeLog(Mpesa.environment, Level.WARNING, String.format("Ssl default context error [ %s ]", e.getMessage()), e);
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
            MpesaUtil.writeLog(Mpesa.environment, Level.SEVERE, String.format("Ssl context error [ %s ]", e.getMessage()), e);
        }
        return getInstance();
    }

    private boolean tokenExpired() {
        if (Mpesa.token == null) {
            return true;
        } else {
            return Mpesa.tokenExpiry.before(Timestamp.from(Instant.now()));
        }
    }

    private String getUrl(@NonNull String endpoint) {
        return String.format("%s%s", Mpesa.environment == Environment.PRODUCTION ? config.getProductionBaseUrl() : config.getSandboxBaseUrl(), endpoint);
    }

    private String authenticate() {
        AtomicReference<String> res = new AtomicReference<>(null);
        if (tokenExpired()) {
            if (Mpesa.key != null && !Mpesa.key.isEmpty() && Mpesa.secret != null && !Mpesa.secret.isEmpty()) {
                try (AsyncHttpClient client = asyncHttpClient(httpClientConfig())) {
                    Gson gson = new Gson();
                    RequestBuilder request = new RequestBuilder(HttpMethod.GET.name())
                            .setUrl(getUrl(config.getEndpointAuth()))
                            .addHeader("Authorization", String.format("Basic %s", Base64.getEncoder().encodeToString(String.format("%s:%s", Mpesa.key, Mpesa.secret).getBytes(StandardCharsets.ISO_8859_1))))
                            .addHeader("Cache-Control", "no-cache")
                            .addQueryParam("grant_type", "client_credentials");
                    client.executeRequest(request.build())
                            .toCompletableFuture()
                            .thenApplyAsync(response -> {
                                MpesaUtil.writeLog(Mpesa.environment, Level.INFO, String.format("Mpesa authenticate response [ statusCode=%s, statusMessage=%s, body=%s ]", response.getStatusCode(), response.getStatusText(), gson.toJson(gson.fromJson(response.getResponseBody(), Object.class))));
                                AtomicReference<MpesaAuthResponse> authResponse = new AtomicReference<>(MpesaAuthResponse.builder().build());
                                if (response.getStatusCode() == 200 && response.getResponseBody() != null && !response.getResponseBody().isEmpty()) {
                                    authResponse.set(gson.fromJson(response.getResponseBody(), MpesaAuthResponse.class));
                                    if (authResponse.get().getToken() != null && authResponse.get().getExpiry() > 20) {
                                        Mpesa.token = authResponse.get().getToken();
                                        Mpesa.tokenExpiry = Timestamp.from(Instant.now().plus((authResponse.get().getExpiry() - 20), ChronoUnit.SECONDS));
                                        res.set(Mpesa.token);
                                    }
                                }
                                return authResponse.get();
                            })
                            .thenAcceptAsync(u -> MpesaUtil.writeLog(Mpesa.environment, Level.FINE, String.valueOf(u))).join();
                } catch (Exception e) {
                    MpesaUtil.writeLog(Mpesa.environment, Level.SEVERE, String.format("Mpesa get authentication error [ %s ]", e.getMessage()), e);
                }
            }
        } else {
            res.set(Mpesa.token);
        }
        return res.get();
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

    /*
    * Stk push simulator
    *
    * NOTE :: CustomerPayBillOnline - reference to be account number ie phone number, CustomerBuyGoodsOnline - to be reference number
    * */
    public CompletableFuture<MpesaStkPushResponse> stkPush(
            @NonNull StkPushType type,
            @NonNull String shortCode,
            @NonNull String lipaOnlinePassKey,
            @NonNull String phone,
            @NonNull String callbackUrl,
            @NonNull String reference,
            String description) throws Exception {
        AtomicReference<MpesaStkPushResponse> res = new AtomicReference<>(MpesaStkPushResponse.builder().build());
        String token = authenticate();
        if (token != null && !token.isEmpty()) {
            try (AsyncHttpClient client = asyncHttpClient(httpClientConfig())) {
                Gson gson = new Gson();
                String pn = MpesaUtil.formatPhone("KE", phone);
                if (pn == null || pn.isEmpty()) {
                    throw new Exception(String.format("Invalid phone number [ country=KE, phone=%s ]", phone));
                }
                String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(Timestamp.from(Instant.now()));
                MpesaStkPushRequest body = MpesaStkPushRequest.builder()
                        .shortCode(shortCode)
                        .password(MpesaUtil.stkPushPassword(shortCode, lipaOnlinePassKey, timestamp))
                        .timestamp(timestamp)
                        .transactionType(type == StkPushType.PAY_BILL ? "CustomerPayBillOnline" : "CustomerBuyGoodsOnline")
                        .amount(1)
                        .partyA(pn)
                        .partyB(shortCode)
                        .phone(pn)
                        .callbackUrl(callbackUrl)
                        .reference(reference)
                        .description(description)
                        .build();
                RequestBuilder request = new RequestBuilder(HttpMethod.POST.name())
                        .setUrl(getUrl(config.getEndpointStkPush()))
                        .addHeader("Authorization", String.format("Bearer %s", token))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Cache-Control", "no-cache")
                        .setBody(gson.toJson(body));
                client.executeRequest(request.build())
                        .toCompletableFuture()
                        .thenApplyAsync(response -> {
                            MpesaUtil.writeLog(Mpesa.environment, Level.INFO, String.format("Mpesa Stk Push response [ statusCode=%s, statusMessage=%s, body=%s ]", response.getStatusCode(), response.getStatusText(),
                                    gson.toJson(gson.fromJson(response.getResponseBody(), Object.class))));
                            if (response.getStatusCode() == 200 && response.getResponseBody() != null && !response.getResponseBody().isEmpty()) {
                                res.set(gson.fromJson(response.getResponseBody(), MpesaStkPushResponse.class));
                            } else {
                                MpesaErrorResponse errorResponse = gson.fromJson(response.getResponseBody(), MpesaErrorResponse.class);
                                res.set(MpesaStkPushResponse.builder().merchantRequestId(errorResponse.getRequestId()).responseCode(errorResponse.getErrorCode() != null ? errorResponse.getErrorCode() : String.valueOf(response.getStatusCode())).responseDescription(errorResponse.getErrorMessage() != null ? errorResponse.getErrorMessage() : response.getStatusText()).build());
                            }
                            return res.get();
                        })
                        .thenAcceptAsync(u -> MpesaUtil.writeLog(Mpesa.environment, Level.FINE, String.valueOf(u))).join();
            }
        } else {
            throw new Exception("Invalid authorization token. Confirm if the consumer key and consumer secret provided are valid");
        }
        return CompletableFuture.supplyAsync(res::get);
    }

    public CompletableFuture<MpesaStkPushStatusResponse> stkPushStatus(
            @NonNull String shortCode,
            @NonNull String lipaOnlinePassKey,
            @NonNull String checkoutRequestId) throws Exception {
        AtomicReference<MpesaStkPushStatusResponse> res = new AtomicReference<>(MpesaStkPushStatusResponse.builder().build());
        String token = authenticate();
        if (token != null && !token.isEmpty()) {
            try (AsyncHttpClient client = asyncHttpClient(httpClientConfig())) {
                Gson gson = new Gson();
                String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(Timestamp.from(Instant.now()));
                MpesaStkPushStatusRequest body = MpesaStkPushStatusRequest.builder()
                        .shortCode(shortCode)
                        .password(MpesaUtil.stkPushPassword(shortCode, lipaOnlinePassKey, timestamp))
                        .timestamp(timestamp)
                        .checkoutRequestId(checkoutRequestId)
                        .build();
                RequestBuilder request = new RequestBuilder(HttpMethod.POST.name())
                        .setUrl(getUrl(config.getEndpointStkPushStatus()))
                        .addHeader("Authorization", String.format("Bearer %s", token))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Cache-Control", "no-cache")
                        .setBody(gson.toJson(body));
                client.executeRequest(request.build())
                        .toCompletableFuture()
                        .thenApplyAsync(response -> {
                            MpesaUtil.writeLog(Mpesa.environment, Level.INFO, String.format("Mpesa Stk Push Status response [ statusCode=%s, statusMessage=%s, body=%s ]", response.getStatusCode(), response.getStatusText(),
                                    gson.toJson(gson.fromJson(response.getResponseBody(), Object.class))));
                            if (response.getStatusCode() == 200 && response.getResponseBody() != null && !response.getResponseBody().isEmpty()) {
                                res.set(gson.fromJson(response.getResponseBody(), MpesaStkPushStatusResponse.class));
                            } else {
                                MpesaErrorResponse errorResponse = gson.fromJson(response.getResponseBody(), MpesaErrorResponse.class);
                                res.set(MpesaStkPushStatusResponse.builder().merchantRequestId(errorResponse.getRequestId()).responseCode(errorResponse.getErrorCode() != null ? errorResponse.getErrorCode() : String.valueOf(response.getStatusCode())).responseDescription(errorResponse.getErrorMessage() != null ? errorResponse.getErrorMessage() : response.getStatusText()).build());
                            }
                            return response.getResponseBody();
                        })
                        .thenAcceptAsync(u -> MpesaUtil.writeLog(Mpesa.environment, Level.FINE, String.valueOf(u))).join();
            }
        } else {
            throw new Exception("Invalid authorization token. Confirm if the consumer key and consumer secret provided are valid");
        }
        return CompletableFuture.supplyAsync(res::get);
    }

    public CompletableFuture<MpesaReversalResponse> reversal(
            @NonNull File mpesaPublicCertificate,
            @NonNull String initiatorName,
            @NonNull String initiatorPassword,
            @NonNull String transactionId,
            @NonNull Integer amount,
            @NonNull String partyA,
            @NonNull IdentifierType identifierType,
            @NonNull String queueTimeoutUrl,
            @NonNull String resultUrl,
            @NonNull String description,
            String occasion) throws Exception {
        AtomicReference<MpesaReversalResponse> res = new AtomicReference<>(MpesaReversalResponse.builder().build());
        String token = authenticate();
        if (token != null && !token.isEmpty()) {
            try (AsyncHttpClient client = asyncHttpClient(httpClientConfig())) {
                Gson gson = new Gson();
                MpesaReversalRequest body = MpesaReversalRequest.builder()
                        .initiator(initiatorName)
                        .credential(MpesaUtil.initiatorCredentials(mpesaPublicCertificate, initiatorPassword))
                        .commandId("TransactionReversal")
                        .transactionId(transactionId)
                        .amount(amount)
                        .identifierType(MpesaUtil.getIdentifierType(identifierType))
                        .description(description)
                        .queueTimeoutUrl(queueTimeoutUrl)
                        .resultUrl(resultUrl)
                        .build();
                if (occasion != null && !occasion.isEmpty()) {
                    body.setOccasion(occasion);
                }
                if (identifierType == IdentifierType.MSISDN) {
                    String pn = MpesaUtil.formatPhone("KE", partyA);
                    if (pn == null || pn.isEmpty()) {
                        throw new Exception(String.format("Invalid phone number [ country=KE, phone=%s ]", partyA));
                    }
                    body.setReceiverParty(pn);
                } else {
                    body.setReceiverParty(partyA);
                }
                RequestBuilder request = new RequestBuilder(HttpMethod.POST.name())
                        .setUrl(getUrl(config.getEndpointAccountBalance()))
                        .addHeader("Authorization", String.format("Bearer %s", token))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Cache-Control", "no-cache")
                        .setBody(gson.toJson(body));
                client.executeRequest(request.build())
                        .toCompletableFuture()
                        .thenApplyAsync(response -> {
                            MpesaUtil.writeLog(Mpesa.environment, Level.INFO, String.format("Mpesa Reversal response [ statusCode=%s, statusMessage=%s, body=%s ]", response.getStatusCode(), response.getStatusText(),
                                    gson.toJson(gson.fromJson(response.getResponseBody(), Object.class))));
                            /* todo :: check on response*/
                            return res.get();
                        })
                        .thenAcceptAsync(u -> MpesaUtil.writeLog(Mpesa.environment, Level.FINE, String.valueOf(u))).join();
            }

        } else {
            throw new Exception("Invalid authorization token. Confirm if the consumer key and consumer secret provided are valid");
        }
        return CompletableFuture.supplyAsync(res::get);
    }

    public CompletableFuture<MpesaAccountBalanceResponse> balance(
            @NonNull File mpesaPublicCertificate,
            @NonNull String initiatorName,
            @NonNull String initiatorPassword,
            @NonNull String shortCode,
            @NonNull IdentifierType identifierType,
            @NonNull String queueTimeoutUrl,
            @NonNull String resultUrl,
            @NonNull String description) throws Exception {
        AtomicReference<MpesaAccountBalanceResponse> res = new AtomicReference<>(MpesaAccountBalanceResponse.builder().build());
        String token = authenticate();
        if (token != null && !token.isEmpty()) {
            try (AsyncHttpClient client = asyncHttpClient(httpClientConfig())) {
                Gson gson = new Gson();
                MpesaAccountBalanceRequest body = MpesaAccountBalanceRequest.builder()
                        .initiator(initiatorName)
                        .credential(MpesaUtil.initiatorCredentials(mpesaPublicCertificate, initiatorPassword))
                        .commandId("AccountBalance")
                        .partyA(shortCode)
                        .identifierType(MpesaUtil.getIdentifierType(identifierType))
                        .description(description)
                        .queueTimeoutUrl(queueTimeoutUrl)
                        .resultUrl(resultUrl)
                        .build();
                RequestBuilder request = new RequestBuilder(HttpMethod.POST.name())
                        .setUrl(getUrl(config.getEndpointAccountBalance()))
                        .addHeader("Authorization", String.format("Bearer %s", token))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Cache-Control", "no-cache")
                        .setBody(gson.toJson(body));
                client.executeRequest(request.build())
                        .toCompletableFuture()
                        .thenApplyAsync(response -> {
                            MpesaUtil.writeLog(Mpesa.environment, Level.INFO, String.format("Mpesa Account Balance response [ statusCode=%s, statusMessage=%s, body=%s ]", response.getStatusCode(), response.getStatusText(),
                                    gson.toJson(gson.fromJson(response.getResponseBody(), Object.class))));
                            if (response.getStatusCode() == 200 && response.getResponseBody() != null && !response.getResponseBody().isEmpty()) {
                                res.set(gson.fromJson(response.getResponseBody(), MpesaAccountBalanceResponse.class));
                            } else {
                                MpesaErrorResponse errorResponse = gson.fromJson(response.getResponseBody(), MpesaErrorResponse.class);
                                res.set(MpesaAccountBalanceResponse.builder().responseCode(errorResponse.getErrorCode() != null ? errorResponse.getErrorCode() : String.valueOf(response.getStatusCode())).responseDescription(errorResponse.getErrorMessage() != null ? errorResponse.getErrorMessage() : response.getStatusText()).originatorConversationId(errorResponse.getRequestId()).build());
                            }
                            return res.get();
                        })
                        .thenAcceptAsync(u -> MpesaUtil.writeLog(Mpesa.environment, Level.FINE, String.valueOf(u))).join();
            }
        } else {
            throw new Exception("Invalid authorization token. Confirm if the consumer key and consumer secret provided are valid");
        }
        return CompletableFuture.supplyAsync(res::get);
    }

    public CompletableFuture<MpesaTransactionStatusResponse> transactionStatus(
            @NonNull File mpesaPublicCertificate,
            @NonNull String initiatorName,
            @NonNull String initiatorPassword,
            @NonNull String transactionId,
            @NonNull String partyA,
            @NonNull IdentifierType identifierType,
            @NonNull String queueTimeoutUrl,
            @NonNull String resultUrl,
            @NonNull String description,
            String occasion) throws Exception {
        AtomicReference<MpesaTransactionStatusResponse> res = new AtomicReference<>(MpesaTransactionStatusResponse.builder().build());
        String token = authenticate();
        if (token != null && !token.isEmpty()) {
            try (AsyncHttpClient client = asyncHttpClient(httpClientConfig())) {
                Gson gson = new Gson();
                MpesaTransactionStatusRequest body = MpesaTransactionStatusRequest.builder()
                        .initiator(initiatorName)
                        .credential(MpesaUtil.initiatorCredentials(mpesaPublicCertificate, initiatorPassword))
                        .commandId("TransactionStatusQuery")
                        .transactionId(transactionId)
                        .identifierType(MpesaUtil.getIdentifierType(identifierType))
                        .description(description)
                        .queueTimeoutUrl(queueTimeoutUrl)
                        .resultUrl(resultUrl)
                        .build();
                if (occasion != null && !occasion.isEmpty()) {
                    body.setOccasion(occasion);
                }
                if (identifierType == IdentifierType.MSISDN) {
                    String pn = MpesaUtil.formatPhone("KE", partyA);
                    if (pn == null || pn.isEmpty()) {
                        throw new Exception(String.format("Invalid phone number [ country=KE, phone=%s ]", partyA));
                    }
                    body.setPartyA(pn);
                } else {
                    body.setPartyA(partyA);
                }
                RequestBuilder request = new RequestBuilder(HttpMethod.POST.name())
                        .setUrl(getUrl(config.getEndpointTransactionStatus()))
                        .addHeader("Authorization", String.format("Bearer %s", token))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Cache-Control", "no-cache")
                        .setBody(gson.toJson(body));
                client.executeRequest(request.build())
                        .toCompletableFuture()
                        .thenApplyAsync(response -> {
                            MpesaUtil.writeLog(Mpesa.environment, Level.INFO, String.format("Mpesa Transaction Status response [ statusCode=%s, statusMessage=%s, body=%s ]", response.getStatusCode(), response.getStatusText(),
                                    gson.toJson(gson.fromJson(response.getResponseBody(), Object.class))));
                            if (response.getStatusCode() == 200 && response.getResponseBody() != null && !response.getResponseBody().isEmpty()) {
                                res.set(gson.fromJson(response.getResponseBody(), MpesaTransactionStatusResponse.class));
                            } else {
                                MpesaErrorResponse errorResponse = gson.fromJson(response.getResponseBody(), MpesaErrorResponse.class);
                                res.set(MpesaTransactionStatusResponse.builder().responseCode(errorResponse.getErrorCode() != null ? errorResponse.getErrorCode() : String.valueOf(response.getStatusCode())).responseDescription(errorResponse.getErrorMessage() != null ? errorResponse.getErrorMessage() : response.getStatusText()).originatorConversationId(errorResponse.getRequestId()).build());
                            }
                            return res.get();
                        })
                        .thenAcceptAsync(u -> MpesaUtil.writeLog(Mpesa.environment, Level.FINE, String.valueOf(u))).join();
            }
        } else {
            throw new Exception("Invalid authorization token. Confirm if the consumer key and consumer secret provided are valid");
        }
        return CompletableFuture.supplyAsync(res::get);
    }

    public CompletableFuture<MpesaC2BUrlResponse> registerC2BUrl(
            @NonNull String shortCode,
            @NonNull C2BUrlResponseType responseType,
            @NonNull String confirmationUrl,
            @NonNull String validationUrl) throws Exception {
        AtomicReference<MpesaC2BUrlResponse> res = new AtomicReference<>(MpesaC2BUrlResponse.builder().build());
        String token = authenticate();
        if (token != null && !token.isEmpty()) {
            try (AsyncHttpClient client = asyncHttpClient(httpClientConfig())) {
                Gson gson = new Gson();
                MpesaC2BUrlRequest body = MpesaC2BUrlRequest.builder()
                        .shortCode(shortCode)
                        .responseType(responseType == C2BUrlResponseType.COMPLETED ? "Completed" : responseType == C2BUrlResponseType.CANCELED ? "Canceled" : "")
                        .confirmationUrl(confirmationUrl != null && !confirmationUrl.isEmpty() ? confirmationUrl : "")
                        .validationUrl(validationUrl != null && !validationUrl.isEmpty() ? validationUrl : "")
                        .build();
                RequestBuilder request = new RequestBuilder(HttpMethod.POST.name())
                        .setUrl(getUrl(config.getEndpointC2BUrl()))
                        .addHeader("Authorization", String.format("Bearer %s", token))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Cache-Control", "no-cache")
                        .setBody(gson.toJson(body));
                client.executeRequest(request.build())
                        .toCompletableFuture()
                        .thenApplyAsync(response -> {
                            MpesaUtil.writeLog(Mpesa.environment, Level.INFO, String.format("Mpesa C2B Register URL response [ statusCode=%s, statusMessage=%s, body=%s ]", response.getStatusCode(), response.getStatusText(),
                                    gson.toJson(gson.fromJson(response.getResponseBody(), Object.class))));
                            if (response.getStatusCode() == 200 && response.getResponseBody() != null && !response.getResponseBody().isEmpty()) {
                                res.set(gson.fromJson(response.getResponseBody(), MpesaC2BUrlResponse.class));
                            } else {
                                MpesaErrorResponse errorResponse = gson.fromJson(response.getResponseBody(), MpesaErrorResponse.class);
                                res.set(MpesaC2BUrlResponse.builder().responseDescription(errorResponse.getErrorMessage() != null ? errorResponse.getErrorMessage() : response.getStatusText()).originatorConversationId(errorResponse.getRequestId()).build());
                            }
                            return res.get();
                        })
                        .thenAcceptAsync(u -> MpesaUtil.writeLog(Mpesa.environment, Level.FINE, String.valueOf(u))).join();
            }
        } else {
            throw new Exception("Invalid authorization token. Confirm if the consumer key and consumer secret provided are valid");
        }
        return CompletableFuture.supplyAsync(res::get);
    }
}
