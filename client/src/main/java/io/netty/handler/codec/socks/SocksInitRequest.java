/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.socks;


import java.util.Collections;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * An socks init request.
 *
 * @see SocksInitResponse
 * @see SocksInitRequestDecoder
 */
public final class SocksInitRequest extends SocksRequest {
    private final List<AuthScheme> authSchemes;

    public SocksInitRequest(List<AuthScheme> authSchemes) {
        super(SocksRequestType.INIT);
        if (authSchemes == null) {
            throw new NullPointerException("authSchemes");
        }
        this.authSchemes = authSchemes;
    }

    /**
     * Returns the List<{@link AuthScheme}> of this {@link SocksInitRequest}
     *
     * @return The List<{@link AuthScheme}> of this {@link SocksInitRequest}
     */
    public List<AuthScheme> getAuthSchemes() {
        return Collections.unmodifiableList(authSchemes);
    }

    @Override
    public void encodeAsByteBuf(ChannelBuffer byteBuf) {
        byteBuf.writeByte(getProtocolVersion().getByteValue());
        byteBuf.writeByte(authSchemes.size());
        for (AuthScheme authScheme : authSchemes) {
            byteBuf.writeByte(authScheme.getByteValue());
        }
    }
}
