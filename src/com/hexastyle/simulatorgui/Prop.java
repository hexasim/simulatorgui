package com.hexastyle.simulatorgui;

import java.io.*;
import java.util.Properties;

/**
 * Created by Hexastyle on 2017.10.12..
 */
public class Prop {
    static Properties prop = new Properties();
    static OutputStream output = null;
    static private final String configFileName = "config.properties";
    static String url = "127.0.0.1";
    static int port = 4000;

    static void readSettings() {
        InputStream input = null;
        try {
            input = new FileInputStream(configFileName);
            prop.load(input);
            String portString = prop.getProperty("port");
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException ne) {
            }
            url = prop.getProperty("url");
        } catch (IOException ex) {
//            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static void writeSettings() {
        try {
            output = new FileOutputStream(configFileName);
            prop.setProperty("url", url);
            prop.setProperty("port", Integer.toString(port));
            prop.store(output, null);
            output.flush();
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
