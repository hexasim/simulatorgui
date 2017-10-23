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
import java.awt.geom.QuadCurve2D;
import java.io.IOException;

public class BBWireViewer extends DeviceViewer {
    int startX;
    int startY;
    int endX;
    int endY;
    float ox;
    float oy;
    float z;
    Color color;
    final QuadCurve2D.Double path = new QuadCurve2D.Double();
    static BasicStroke basicStroke = new BasicStroke(4);

    BBWireViewer(long uniqueId) {
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
            Stroke stroke = g2d.getStroke();
            g2d.setStroke(basicStroke);
            dox = (ox - EditorViewer.origoOnBreadboard.x) * EditorViewer.zoomFactor;
            doy = (oy - EditorViewer.origoOnBreadboard.y) * EditorViewer.zoomFactor;
            final double x2 = ox + dox / z;
            final double y2 = oy + doy / z;
            path.setCurve(startX, startY, x2, y2, endX, endY);
            g2d.draw(path);
            g2d.setStroke(stroke);
        }
        g2d.setColor(savedColor);
    }

}
