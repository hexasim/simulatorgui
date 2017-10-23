package com.hexastyle.simulatorgui;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Hexastyle on 3/19/17.
 */
public class Viewer {
    static public HashMap<Integer, Color> colorCache = new HashMap<>();
    long uniqueId;

    public static Color getColor(int intColor) {
        Color color = colorCache.get(intColor);
        if (color == null) {
            color = new Color(intColor, true);
            colorCache.put(intColor, color);
        }
        return color;
    }

    int receive(Connection con) throws IOException {
        return 0;
    }

    public void draw(Graphics2D g2d) {
    }

    public void clear() {
    }
}
