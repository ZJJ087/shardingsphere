/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.frontend.state.impl;

import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.infra.config.props.BackendExecutorType;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.executor.ConnectionThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.executor.UserExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.ProxyState;
import org.apache.shardingsphere.transaction.api.TransactionType;

import java.util.concurrent.ExecutorService;

/**
 * OK proxy state.
 */
public final class OKProxyState implements ProxyState {
    
    @Override
    public void execute(final ChannelHandlerContext context, final Object message, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final ConnectionSession connectionSession) {
        ExecutorService executorService = determineSuitableExecutorService(context, connectionSession);
        context.channel().config().setAutoRead(false);
        executorService.execute(new CommandExecutorTask(databaseProtocolFrontendEngine, connectionSession, context, message));
    }
    
    private ExecutorService determineSuitableExecutorService(final ChannelHandlerContext context, final ConnectionSession connectionSession) {
        if (requireOccupyThreadForConnection(connectionSession)) {
            return ConnectionThreadExecutorGroup.getInstance().get(connectionSession.getConnectionId());
        }
        return isPreferNettyEventLoop() ? context.executor() : UserExecutorGroup.getInstance().getExecutorService();
    }
    
    private boolean requireOccupyThreadForConnection(final ConnectionSession connectionSession) {
        return ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)
                || TransactionType.isDistributedTransaction(connectionSession.getTransactionStatus().getTransactionType());
    }
    
    private boolean isPreferNettyEventLoop() {
        return BackendExecutorType.OLTP == ProxyContext.getInstance()
                .getContextManager().getMetaDataContexts().getMetaData().getProps().<BackendExecutorType>getValue(ConfigurationPropertyKey.PROXY_BACKEND_EXECUTOR_SUITABLE);
    }
}
