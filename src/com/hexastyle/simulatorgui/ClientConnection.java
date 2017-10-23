/*
 * MIT License
 *
 * Copyright (c)  2017 Hexastyle
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hexastyle.simulatorgui;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;

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