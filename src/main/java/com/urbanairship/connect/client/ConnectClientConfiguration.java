package com.urbanairship.connect.client;

import org.apache.commons.configuration.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class.
 */
public class ConnectClientConfiguration {

    public static final String MES_URL_PROP = "connect.client.mes.url";
    public static final String MES_URL_DEFAULT = "http://localhost:5555/api/events/";
    public final String mesUrl;

    public static final String MES_CONNECT_TIMEOUT_PROP = "connect.client.mes.http.connectTimeoutMillis";
    public static final int MES_CONNECT_TIMEOUT_DEFAULT = (int) TimeUnit.SECONDS.toMillis(10L);
    public final int mesHttpConnectTimeout;

    public static final String MES_READ_TIMEOUT_PROP = "connect.client.mes.http.readTimeoutMillis";
    public static final int MES_READ_TIMEOUT_DEFAULT = (int) TimeUnit.SECONDS.toMillis(5L);
    public final int mesHttpReadTimeout;

    public static final String MES_STREAM_CONNECT_TIMEOUT_PROP = "connect.client.mes.stream.connectTimeoutMillis";
    public static final long MES_STREAM_CONNECT_TIMEOUT_DEFAULT = TimeUnit.SECONDS.toMillis(5L);
    public final long appStreamConnectTimeout;

    public static final String MAX_STREAM_CONSUME_TIME_PROP = "connect.client.mes.stream.maxAppStreamConsumeMillis";
    public static final long MAX_STREAM_CONSUME_TIME_DEFAULT = (int) TimeUnit.SECONDS.toMillis(30L);
    public final long maxAppStreamConsumeTime;

    public static final String MES_RECONNECT_BACKOFF_TIME_PROP = "connect.client.mes.stream.mesReconnectBackoffTime";
    public static final long MES_RECONNECT_BACKOFF_TIME_DEFAULT = (int) TimeUnit.SECONDS.toMillis(1L);
    public final long mesReconnectBackoffTime;

    public static final String MAX_CONNECTION_ATTEMPTS_PROP = "connect.client.mes.stream.maxConnectionAttempts";
    public static final int MAX_CONNECTION_ATTEMPTS_DEFAULT = 10;
    public final int maxConnectionAttempts;

    public ConnectClientConfiguration(Configuration config) {
        this.mesUrl = config.getString(MES_URL_PROP, MES_URL_DEFAULT);
        this.mesHttpConnectTimeout = config.getInt(MES_CONNECT_TIMEOUT_PROP, MES_CONNECT_TIMEOUT_DEFAULT);
        this.mesHttpReadTimeout = config.getInt(MES_READ_TIMEOUT_PROP, MES_READ_TIMEOUT_DEFAULT);
        this.appStreamConnectTimeout = config.getLong(MES_STREAM_CONNECT_TIMEOUT_PROP, MES_STREAM_CONNECT_TIMEOUT_DEFAULT);
        this.maxAppStreamConsumeTime = config.getLong(MAX_STREAM_CONSUME_TIME_PROP, MAX_STREAM_CONSUME_TIME_DEFAULT);
        this.mesReconnectBackoffTime = config.getLong(MES_RECONNECT_BACKOFF_TIME_PROP, MES_RECONNECT_BACKOFF_TIME_DEFAULT);
        this.maxConnectionAttempts = config.getInt(MAX_CONNECTION_ATTEMPTS_PROP, MAX_CONNECTION_ATTEMPTS_DEFAULT);
    }
}
