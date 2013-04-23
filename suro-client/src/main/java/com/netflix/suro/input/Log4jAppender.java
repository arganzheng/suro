/*
 * Copyright 2013 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.suro.input;

import com.netflix.suro.ClientConfig;
import com.netflix.suro.client.SuroClient;
import com.netflix.suro.message.Message;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Log4jAppender extends AppenderSkeleton {
    static final Logger log = LoggerFactory.getLogger(Log4jAppender.class);

    private String formatterClass = JsonLog4jFormatter.class.toString();
    public void setFormatterClass(String formatterClass) {
        this.formatterClass = formatterClass;
    }
    public String getFormatterClass() {
        return formatterClass;
    }

    private String app = "default";
    public void setApp(String app) {
        this.app = app;
    }
    public String getApp() {
        return app;
    }

    private String dataType = "default";
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    private String getDataType() {
        return dataType;
    }

    private byte compression = 1;
    public void setCompression(byte compression) {
        this.compression = compression;
    }
    public byte getCompression() {
        return compression;
    }

    private String loadBalancerType = "eureka";
    public void setLoadBalancerType(String loadBalancerType) {
        this.loadBalancerType = loadBalancerType;
    }
    private String getLoadBalancerType() {
        return loadBalancerType;
    }

    private String loadBalancerServer;
    public void setLoadBalancerServer(String loadBalancerServer) {
        this.loadBalancerServer = loadBalancerServer;
    }
    private String getLoadBalancerServer() {
        return loadBalancerServer;
    }

    private String asyncQueueType = "memory";
    public void setAsyncQueueType(String asyncQueueType) {
        this.asyncQueueType = asyncQueueType;
    }
    public String getAsyncQueueType() {
        return asyncQueueType;
    }

    private int memoryQueueCapacity = 10000;
    public void setAsyncMemoryQueueCapacity(int memoryQueueCapacity) {
        this.memoryQueueCapacity = memoryQueueCapacity;
    }
    public int getAsyncMemoryQueueCapacity() {
        return memoryQueueCapacity;
    }

    private String fileQueuePath = "/logs/suroClient";
    public String getAsyncFileQueuePath() {
        return fileQueuePath;
    }
    public void setAsyncFileQueuePath(String fileQueuePath) {
        this.fileQueuePath = fileQueuePath;
    }

    private String clientType = "async";
    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
    public String getClientType() {
        return clientType;
    }

    private String routingKey = "default";
    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
    public String getRoutingKey() {
        return routingKey;
    }

    private Log4jFormatter formatter;
    private SuroClient client;

    @Override
    public void activateOptions() {
        client = new SuroClient(createProperties());

        try {
            formatter = (Log4jFormatter) Class.forName(formatterClass).newInstance();
        } catch (Exception e) {
            formatter = new JsonLog4jFormatter(client.getConfig());
        }
    }

    private Properties createProperties() {
        Properties properties = new Properties();
        properties.setProperty(ClientConfig.APP, app);
        properties.setProperty(ClientConfig.DATA_TYPE, dataType);
        properties.setProperty(ClientConfig.COMPRESSION, Byte.toString(compression));
        properties.setProperty(ClientConfig.LB_TYPE, loadBalancerType);
        properties.setProperty(ClientConfig.LB_SERVER, loadBalancerServer);
        properties.setProperty(ClientConfig.ASYNC_QUEUE_TYPE, asyncQueueType);
        properties.setProperty(ClientConfig.ASYNC_FILEQUEUE_PATH, fileQueuePath);
        properties.setProperty(ClientConfig.CLIENT_TYPE, clientType);
        properties.setProperty(ClientConfig.ROUTING_KEY, routingKey);

        return properties;
    }

    @Override
    public void doAppend(LoggingEvent event) {
        this.append(event);
    }

    @Override
    protected void append(LoggingEvent event) {
        String result = formatter.format(event);
        client.send(new Message(
                formatter.getRoutingKey() == null ? routingKey : formatter.getRoutingKey(),
                result.getBytes()));
    }

    @Override
    public void close() {
        client.shutdown();
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    public long getSentMessageCount() {
        return client.getSentMessageCount();
    }
}