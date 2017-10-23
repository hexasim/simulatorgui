package com.hexastyle.simulatorgui;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

/**
 * Created by Hexastyle on 2017.09.14..
 */
public class BBResistorLedViewer extends BBLedViewer {
    final static Stroke wideStroke = new BasicStroke(20);

    BBResistorLedViewer(long uniqueId) {
        path = new Path2D.Double();
        this.uniqueId = uniqueId;
        deviceViewerType = common0Type;
    }

    synchronized public void draw(Graphics2D g2d) {
        Color savedColor = g2d.getColor();
        g2d.setColor(color);
        synchronized (path) {
            AffineTransform transform = g2d.getTransform();
            float dox = (ox - EditorViewer.origoOnBreadboard.x) * EditorViewer.zoomFactor;
            float doy = (oy - EditorViewer.origoOnBreadboard.y) * EditorViewer.zoomFactor;
            float xs = startX - endX;
            float ys = startY - endY;
            float s = (float) Math.sqrt(xs * xs + ys * ys);
            final float x2 = ox + ((dox)) / z;
            final float y2 = oy + ((doy)) / z;
            final float x3 = ox + 50 * (startX - endX) / s + ((dox)) / z;
            final float y3 = oy + 50 * (startY - endY) / s + ((doy)) / z;
            final float x4 = ox + 80 * (startX - endX) / s + ((dox)) / z;
            final float y4 = oy + 80 * (startY - endY) / s + ((doy)) / z;
            final float xm = (x2 + x3) / 2;
            final float ym = (y2 + y3) / 2;
            Stroke w = g2d.getStroke();
            g2d.setStroke(basicStroke);
            drawDiode(g2d, x2, y2, x3, y3, xm, ym);
            g2d.setStroke(wideStroke);
            g2d.drawLine(Math.round(x4), Math.round(y4), Math.round(x3), Math.round(y3));
            g2d.setStroke(w);
            g2d.drawLine(Math.round(xm), Math.round(ym), Math.round(x3), Math.round(y3));
            g2d.drawLine(Math.round(endX), Math.round(endY), (int) Math.round(x2), (int) Math.round(y2));
            g2d.drawLine(Math.round(startX), Math.round(startY), (int) Math.round(x4), (int) Math.round(y4));
            g2d.setStroke(w);
        }
        g2d.setColor(savedColor);
    }
}