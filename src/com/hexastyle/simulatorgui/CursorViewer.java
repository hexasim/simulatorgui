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
import java.io.IOException;

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
