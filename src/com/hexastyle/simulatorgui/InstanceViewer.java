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

public class InstanceViewer extends DeviceViewer {

    InstanceViewer(long uniqueId) {
        this.uniqueId = uniqueId;
    }


    synchronized public void draw(Graphics2D g2d) {
        super.draw(g2d);
    }

    synchronized int receive(Connection con) throws IOException {
        deviceViewerType = new DeviceViewerType();
        deviceViewerType.getPaths(con);
        if (deviceViewerType.segmentPath != null) {
            sections = new Section[deviceViewerType.segmentPath.length];
            for (int i = 0; i < deviceViewerType.segmentPath.length; i++) {
                sections[i] = new Section();
            }
        }
        left = con.input.readInt();
        bottom = con.input.readInt();
        angleIndex = con.input.readInt();
        numberOfIos = con.input.readInt();
        ioColors = new Color[numberOfIos];
        inverted = new boolean[deviceViewerType.maxNumberOfIos];
        color = getColor(con.input.readInt());
        for (int i = 0; i < numberOfIos; i++) {
            ioColors[i] = getColor(con.input.readInt());
            inverted[i] = con.input.readBoolean();
        }
        ioIndex = numberOfIos - deviceViewerType.minNumberOfIos;
        int sym = con.input.readInt();
        while (sym == ProtocolConstants.A2PC.COLOR_PART.ordinal()) {
            int pos = con.input.readInt();
            int partColor = con.input.readInt();
            sections[pos].color = getColor(partColor);
            sym = con.input.readInt();
        }
        return sym;
    }

}

