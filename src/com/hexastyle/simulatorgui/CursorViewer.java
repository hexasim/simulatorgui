package com.hexastyle.simulatorgui;

import java.awt.*;
import java.io.IOException;

/**
 * Created by Hexastyle on 2017.07.04..
 */
public class CursorViewer extends Viewer {
    private String id;
    private int x;
    private int y;
    Color color;
    final static int size = 20;

    CursorViewer(long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void draw(Graphics2D g2d) {
        Color savedCcolor = g2d.getColor();
        g2d.setColor(color);
        float zoom = (float) g2d.getTransform().getScaleX();
        int s = Math.round(size / zoom);
        g2d.drawLine(x - s, y, x + s, y);
        g2d.drawLine(x, y - s, x, y + s);
        g2d.setColor(savedCcolor);
    }

    int receive(Connection con) throws IOException {
        x = con.input.readInt();
        y = con.input.readInt();
        color = getColor(con.input.readInt());
        int sym = con.input.readInt();
        return sym;
    }


}
