package com.hexastyle.simulatorgui;

import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.io.IOException;

/**
 * Created by Hexastyle on 2017.09.09..
 */
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
