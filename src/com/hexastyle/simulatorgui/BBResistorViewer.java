package com.hexastyle.simulatorgui;

import java.awt.*;
import java.awt.geom.Path2D;
import java.io.IOException;

/**
 * Created by Hexastyle on 2017.09.11..
 */
public class BBResistorViewer extends DeviceViewer {
    int startX;
    int startY;
    int endX;
    int endY;
    float ox;
    float oy;
    float z;
    Color color;
    Path2D.Double path;
    final static Stroke wideStroke = new BasicStroke(20);

    BBResistorViewer(long uniqueId) {
        path = new Path2D.Double();
        this.uniqueId = uniqueId;
        deviceViewerType = common0Type;
    }

    synchronized int receive(Connection con) throws IOException {
        int sym;
        sym = con.input.readInt();
        color = getColor(sym);
        sym &= 0xaaFFFFFF;
        color = getColor(sym);
        startX = con.input.readInt();
        startY = con.input.readInt();
        endX = con.input.readInt();
        endY = con.input.readInt();
        ox = con.input.readFloat();
        oy = con.input.readFloat();
        z = con.input.readFloat();
        sym = con.input.readInt();
        return sym;
    }

    synchronized public void draw(Graphics2D g2d) {
        Color savedColor = g2d.getColor();
        g2d.setColor(color);
        synchronized (path) {
            double dox;
            double doy;
            dox = (ox - EditorViewer.origoOnBreadboard.x) * EditorViewer.zoomFactor;
            doy = (oy - EditorViewer.origoOnBreadboard.y) * EditorViewer.zoomFactor;
            float xs = startX - endX;
            float ys = startY - endY;
            float s = (float) Math.sqrt(xs * xs + ys * ys);
            final double x2 = ox + ((dox)) / z;
            final double y2 = oy + ((doy)) / z;
            final double x3 = ox + 30 * (startX - endX) / s + ((dox)) / z;
            final double y3 = oy + 30 * (startY - endY) / s + ((doy)) / z;
            Stroke w = g2d.getStroke();
            g2d.setStroke(wideStroke);
            g2d.drawLine((int) Math.round(x2), (int) Math.round(y2), (int) Math.round(x3), (int) Math.round(y3));
            g2d.setStroke(basicStroke);
            g2d.drawLine(Math.round(endX), Math.round(endY), (int) Math.round(x2), (int) Math.round(y2));
            g2d.drawLine(Math.round(startX), Math.round(startY), (int) Math.round(x3), (int) Math.round(y3));
            g2d.setStroke(w);
      }
        g2d.setColor(savedColor);
    }


}
