package ru.company.connector.efrosdo.config;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

class TrustAllRequestFactory extends SimpleClientHttpRequestFactory {

    private final SSLContext sslContext = trustAllSslContext();

    TrustAllRequestFactory(Duration connectTimeout, Duration readTimeout) {
        setConnectTimeout(connectTimeout);
        setReadTimeout(readTimeout);
    }

    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        if (connection instanceof HttpsURLConnection https) {
            https.setSSLSocketFactory(sslContext.getSocketFactory());
            https.setHostnameVerifier((hostname, session) -> true);
        }
        super.prepareConnection(connection, httpMethod);
    }

    private static SSLContext trustAllSslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new TrustAllManager()}, new SecureRandom());
            return sslContext;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Не удалось создать доверяющий всем сертификатам SSLContext", e);
        }
    }

    private static final class TrustAllManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
