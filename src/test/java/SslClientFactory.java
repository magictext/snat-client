import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;


public class SslClientFactory {
    public static SSLEngine prepareEngine(String host, int port) throws Exception {
        char[] passphrase = "changeit".toCharArray();

        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        KeyStore ks = ks = KeyStore.getInstance("JKS");

        String JAVA_HOME = System.getenv("JAVA_HOME");
        ks.load(new FileInputStream(JAVA_HOME + "/jre/lib/security/cacerts"), passphrase);

        kmf.init(ks, passphrase);
        ctx.init(kmf.getKeyManagers(), null, null);
        SSLEngine sslEngine = ctx.createSSLEngine(host, port);
        sslEngine.setUseClientMode(true);

        return sslEngine;
    }
}
