import play.server.SSLEngineProvider;

import java.io.File;
import java.io.FileInputStream;
import javax.net.ssl.*;
import java.security.KeyStore;


public class CustomSSLEngineProvider implements SSLEngineProvider {

    private static final String KEYSTORE_LOCATION = "conf/selfsigned.keystore";
    private static final char[] PASSPHRASE = "".toCharArray();

    public CustomSSLEngineProvider() { }

    @Override
    public SSLEngine createSSLEngine() {
        // Note that not caching the SSL context makes the problem happen less often,
        // but does not eradicate it completely.
        return createSSLContext().createSSLEngine();
    }

    private SSLContext createSSLContext() {
        try {
            File keyStoreFile = new File(KEYSTORE_LOCATION);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (FileInputStream fis = new FileInputStream(keyStoreFile)) {
                keyStore.load(fis, PASSPHRASE);
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, PASSPHRASE);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}