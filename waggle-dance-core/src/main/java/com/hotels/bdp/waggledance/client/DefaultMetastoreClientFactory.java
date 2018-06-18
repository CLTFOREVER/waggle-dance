/**
 * Copyright (C) 2016-2018 Expedia Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.bdp.waggledance.client;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.ClassUtils.getUserClass;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotels.hcommon.hive.metastore.client.api.CloseableIFace;
import com.hotels.hcommon.hive.metastore.client.api.ConditionalIFace;

public class DefaultMetastoreClientFactory implements MetaStoreClientFactory {

  static final Class<?>[] INTERFACES = new Class<?>[] { CloseableIFace.class };

  private static class ReconnectingMetastoreClientInvocationHandler implements InvocationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ReconnectingMetastoreClientInvocationHandler.class);

    private final ThriftMetastoreClient base;
    private final String name;
    private final int maxRetries;

    private ReconnectingMetastoreClientInvocationHandler(HiveConf hiveConf, String name, int maxRetries) {
      this.name = name;
      this.maxRetries = maxRetries;
      base = new ThriftMetastoreClient(hiveConf);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      int attempt = 0;
      // close() and isOpen() methods delegate to base HiveMetastoreClient
      switch (method.getName()) {
      case "isOpen":
        try {
          reconnectIfDisconnected();
          return base.isOpen();
        } catch (Exception e) {
          LOG.debug("Error re-opening client at isOpen(): {}", e.getMessage());
          return false;
        }
      case "close":
        if (base != null) {
          base.close();
        }
        return null;
      default:
        base.open();
        do {
          try {
            return method.invoke(base.getClient(), args);
          } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            if (TTransportException.class.isAssignableFrom(realException.getClass())) {
              if (attempt < maxRetries && shouldRetry(method)) {
                LOG.debug("TTransportException captured in client {}. Reconnecting... ", name);
                base.reconnect();
                continue;
              }
              throw new MetastoreUnavailableException("Client " + name + " is not available", realException);
            }
            throw realException;
          }
        } while (++attempt <= maxRetries);
        break;
      }
      throw new RuntimeException("Unreachable code");

    }

    private boolean shouldRetry(Method method) {
      switch (method.getName()) {
      case "shutdown":
        return false;
      default:
        return true;
      }
    }

    private void reconnectIfDisconnected() {
      try {
        if (!base.isOpen()) {
          base.reconnect();
        }
      } catch (Exception e) {
        throw new MetastoreUnavailableException("Client " + name + " is not available", e);
      }
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(DefaultMetastoreClientFactory.class);

  private final List<ConditionalIFace> iFaces;

  public DefaultMetastoreClientFactory(List<ConditionalIFace> iFaces) {
    this.iFaces = iFaces;
  }

  /*
   * (non-Javadoc)
   * @see com.hotels.bdp.waggledance.client.MetaStoreClientFactoryI#newInstance(org.apache.hadoop.hive.conf.HiveConf,
   * java.lang.String, int)
   */
  @Override
  public CloseableIFace newInstance(HiveConf hiveConf, String name, int reconnectionRetries) {
    CloseableIFace closeableIFace = getInstance(hiveConf, name, reconnectionRetries);
    LOG.info("Loaded CloseableIFace {} for Metastore {}", getUserClass(closeableIFace), name);
    return closeableIFace;
  }

  private CloseableIFace getInstance(HiveConf hiveConf, String name, int reconnectionRetries) {
    if (iFaces != null && !iFaces.isEmpty()) {
      String uri = hiveConf.get(HiveConf.ConfVars.METASTOREURIS.varname);
      if (isNotBlank(uri)) {
        for (ConditionalIFace iFace : iFaces) {
          if (iFace.accepts(uri)) {
            return iFace;
          }
        }
      }
    }

    ReconnectingMetastoreClientInvocationHandler reconnectingHandler = new ReconnectingMetastoreClientInvocationHandler(
        hiveConf, name, reconnectionRetries);
    CloseableIFace closeableIFace = (CloseableIFace) Proxy.newProxyInstance(getClass().getClassLoader(), INTERFACES,
        reconnectingHandler);
    return closeableIFace;
  }

}