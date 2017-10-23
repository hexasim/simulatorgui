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
import java.awt.geom.*;
import java.io.IOException;

public class WireViewer extends Viewer {
    Shape shape;
    Path2D.Double path;
    Color color;

    WireViewer(long uniqueId) {
        path = new Path2D.Double();
        this.uniqueId = uniqueId;
    }

    synchronized public void draw(Graphics2D g2d) {
        Color savedColor = g2d.getColor();
        g2d.setColor(color);
        synchronized (path) {
            g2d.draw(path);
        }
        g2d.setColor(savedColor);
    }

    private void getArc(Connection con) throws IOException {
        int arcleft = con.input.readInt();
        int arcbottom = con.input.readInt();
        int arcright = con.input.readInt();
        int arctop = con.input.readInt();
        int arcstartAngle = con.input.readInt();
        int arcAngle = con.input.readInt();
        shape = new Arc2D.Double(arcleft, arcbottom, arcright, arctop, arcstartAngle, arcAngle, Arc2D.CHORD);
    }

    private void getLine(Connection con) throws IOException {
        int x0 = con.input.readInt();
        int y0 = con.input.readInt();
        int x1 = con.input.readInt();
        int y1 = con.input.readInt();
        shape = new Line2D.Double(new Point(x0, y0), new Point(x1, y1));
    }

    public void clear() {
        path = new Path2D.Double();
    }

    synchronized int receive(Connection con) throws IOException {
        int sym;
        color = getColor(con.input.readInt());
        sym = con.input.readInt();
        if (sym == ProtocolConstants.A2PC.PATH_ARC.ordinal()) {
            getArc(con);
            path.append(shape, false);
            sym = con.input.readInt();
        }
        if (sym == ProtocolConstants.A2PC.PATH_ARC.ordinal()) {
            getArc(con);
            path.append(shape, false);
            sym = con.input.readInt();
        }
        while (sym != ProtocolConstants.END) {
            getLine(con);
            path.append(shape, false);
            sym = con.input.readInt();
        }
        sym = con.input.readInt();
        return sym;
    }

}
