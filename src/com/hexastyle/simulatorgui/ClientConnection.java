package com.hexastyle.simulatorgui;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;

/**
 * Created by Hexastyle on 12/11/16.
 */
public class ClientConnection extends Connection {
    SSLSocket clientSocket;

    ClientConnection() {
    }


    public boolean connect(String url, int port, String password) {
        try {
            SSLContext ssl_ctx;
            InputStream trustStoreStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("simclient.jks");
            InputStream keyStoreStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("clientauth.jks");
            KeyStore trustStore = KeyStore.getInstance("JKS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustStore.load(trustStoreStream, "simclient".toCharArray());
            trustManagerFactory.init(trustStore);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyStore.load(keyStoreStream, "clientauth".toCharArray());
            keyManagerFactory.init(keyStore, "clientauth".toCharArray());
            ssl_ctx = SSLContext.getInstance("TLS");
            ssl_ctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
            SSLSocketFactory sslSocketFactory = ssl_ctx.getSocketFactory();
            clientSocket = (SSLSocket) sslSocketFactory.createSocket(url, port);
            clientSocket.startHandshake();
            output = new DataOutputStream(clientSocket.getOutputStream());
            output.writeUTF(password);
            input = new DataInputStream(clientSocket.getInputStream());
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

}