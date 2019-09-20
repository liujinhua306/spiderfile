package com.bluedon.spiderfile.configuration;

import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.*;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author liujh
 * @date 2019/9/20 9:18
 */
@Configuration
public class HttpAsyncClientConfig {

    @Autowired
    private HttpPoolProperties httpPoolProperties;

    static{
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,SSLv3");
    }

    @Bean
    public CloseableHttpAsyncClient createAsyncClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOReactorException {

        SSLContext sslcontext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            //信任所有
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        }).build();


        // 设置协议http和https对应的处理socket链接工厂的对象
        Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder
                .<SchemeIOSessionStrategy>create()
                .register("http", NoopIOSessionStrategy.INSTANCE)
                .register("https", new SSLIOSessionStrategy(sslcontext))
                .build();

        // 配置io线程
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setSoKeepAlive(false).setTcpNoDelay(true)
                .setIoThreadCount(Runtime.getRuntime().availableProcessors())
                .build();
        // 设置连接池大小
        ConnectingIOReactor ioReactor;
        ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        PoolingNHttpClientConnectionManager conMgr = new PoolingNHttpClientConnectionManager(
                ioReactor, null, sessionStrategyRegistry, null);

        if (httpPoolProperties.getMaxTotal() > 0) {
            conMgr.setMaxTotal(httpPoolProperties.getMaxTotal());
        }

        if (httpPoolProperties.getDefaultMaxPerRoute() > 0) {
            conMgr.setDefaultMaxPerRoute(httpPoolProperties.getDefaultMaxPerRoute());
        } else {
            conMgr.setDefaultMaxPerRoute(10);
        }

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8).build();

        Lookup<AuthSchemeProvider> authSchemeRegistry;
        authSchemeRegistry = RegistryBuilder
                .<AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                .register(AuthSchemes.DIGEST, new DigestSchemeFactory())
                .register(AuthSchemes.NTLM, new NTLMSchemeFactory())
                .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory())
                .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())
                .build();
        conMgr.setDefaultConnectionConfig(connectionConfig);


        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(httpPoolProperties.getConnectionRequestTimeout())
                .setConnectTimeout(httpPoolProperties.getConnectTimeout())
                .setSocketTimeout(httpPoolProperties.getSocketTimeout()).build();


        return HttpAsyncClients.custom().setConnectionManager(conMgr)
                //.setDefaultRequestConfig(requestConfig)
                .setDefaultAuthSchemeRegistry(authSchemeRegistry)
                .setDefaultCookieStore(new BasicCookieStore()).build();


    }

}
