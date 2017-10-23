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

import java.awt.*;
import java.awt.geom.Path2D;
import java.io.IOException;

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
