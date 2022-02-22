/*
Copyright 2022 Airship and Contributors
*/

package com.urbanairship.connect.client.consume;

import com.google.common.util.concurrent.AbstractFuture;

public final class MobileEventStreamConnectFuture extends AbstractFuture<StatusAndHeaders> implements ConnectCallback {

    @Override
    public void error(Throwable e) {
        setException(e);
    }

    @Override
    public void connected(StatusAndHeaders statusAndHeaders) {
        set(statusAndHeaders);
    }

}
