package com.provenlogic.mingle.Utils;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * This class is to securely connect android client to https chat server,
 * no need of this class if the chat server is http.
 *
 * Created by Anurag on 15/4/17.
 */

public class AndroidSslContext {

    public static SSLContext GetSslContext(Activity activity, String certificateName){
        try{
            AssetManager assetManager = activity.getAssets();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = assetManager.open(certificateName);

            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                Log.d("SslUtilsAndroid", "ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }


            KeyStore keyStore = null;
            String keyStoreType = KeyStore.getDefaultType();
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        }catch(Exception e){
            Log.d("SSL Error", "Failed to get SSL");
        }
        return null;
    }
}
