package com.hexastyle.simulatorgui;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.IOException;

/**
 * Created by Hexastyle on 2017.09.10..
 */
public class BBLedViewer extends DeviceViewer {
    public static final int R = 20;
    int startX;
    int startY;
    int endX;
    int endY;
    float ox;
    float oy;
    float z;
    Color color;
    //    Color colorWithAlpha;
    Path2D.Double path;

    enum LedState {
        ON, OFF, UNDEFINED, BURNING
    }

    protected int state;

    BBLedViewer() {
    }

    BBLedViewer(long uniqueId) {
        path = new Path2D.Double();
        this.uniqueId = uniqueId;
//        System.out.println("BBWireViewer");
        deviceViewerType = common0Type;
    }

    synchronized int receive(Connection con) throws IOException {
        int sym;
        sym = con.input.readInt();
        color = getColor(sym);
        sym &= 0xaaFFFFFF;
        color = getColor(sym);
//        colorWithAlpha = new Color(sym, true);
        startX = con.input.readInt();
        startY = con.input.readInt();
        endX = con.input.readInt();
        endY = con.input.readInt();
        ox = con.input.readFloat();
        oy = con.input.readFloat();
        z = con.input.readFloat();
        state = con.input.readInt();
        sym = con.input.readInt();
        return sym;
    }

    public static float calcRotationAngle(float centerX, float centerY, float targetX, float targetY) {
        double theta = Math.atan2(targetY - centerY, targetX - centerX) + Math.PI / 2;
        return (float) theta;
    }

    public void drawDiode(Graphics2D g2d, float x2, float y2, float x3, float y3, float xm, float ym) {
        AffineTransform transform = g2d.getTransform();

        Color savedColor = g2d.getColor();
        g2d.setColor(color);
        g2d.rotate(calcRotationAngle(xm, ym, x3, y3), xm, ym);
        int ixm = Math.round(xm);
        int iym = Math.round(ym);
        int ax = -R + ixm;
        int ay = iym;
        int bx = +R + ixm;
        int by = iym;

        g2d.drawLine(ixm, iym, ax, ay + R);
        g2d.drawLine(ixm, iym, bx, by + R);
        g2d.drawLine(ax, ay + R, bx, by + R);
        g2d.drawLine(ax, ay, bx, by);
//        g2d.setTransform(transform);
//        g2d.setColor(colorWithAlpha);
        if (state == LedState.ON.ordinal()) {
            g2d.setPaintMode();
            g2d.fillOval(ixm - R - 2, iym - R / 2 - 2, 2 * R + 4, 2 * R + 4);
        } else if (state == LedState.BURNING.ordinal()) {
            g2d.fillOval(ixm - 2 * R - 2, iym - 3 * R / 2 - 2, 4 * R + 4, 4 * R + 4);
        } else if (state == LedState.UNDEFINED.ordinal()) {
            g2d.drawOval(ixm - R - 2, iym - R / 2 - 2, 2 * R + 4, 2 * R + 4);
        }
        g2d.setTransform(transform);
        g2d.setColor(savedColor);
    }

    synchronized public void draw(Graphics2D g2d) {
        Color savedColor = g2d.getColor();
        g2d.setColor(color);
        synchronized (path) {
            Stroke w = g2d.getStroke();
            g2d.setStroke(basicStroke);
            float dox = (ox - EditorViewer.origoOnBreadboard.x) * EditorViewer.zoomFactor;
            float doy = (oy - EditorViewer.origoOnBreadboard.y) * EditorViewer.zoomFactor;
            float xs = startX - endX;
            float ys = startY - endY;
            float s = (float) Math.sqrt(xs * xs + ys * ys);
            final float x2 = ox + ((dox)) / z;
            final float y2 = oy + ((doy)) / z;
            final float x3 = ox + 40 * (startX - endX) / s + ((dox)) / z;
            final float y3 = oy + 40 * (startY - endY) / s + ((doy)) / z;
            final float xm = (x2 + x3) / 2;
            final float ym = (y2 + y3) / 2;
            drawDiode(g2d, x2, y2, x3, y3, xm, ym);
            g2d.drawLine(Math.round(endX), Math.round(endY), (int) Math.round(x2), (int) Math.round(y2));
            g2d.drawLine(Math.round(startX), Math.round(startY), (int) Math.round(xm), (int) Math.round(ym));
            g2d.setStroke(w);
        }
        g2d.setColor(savedColor);
    }

}
