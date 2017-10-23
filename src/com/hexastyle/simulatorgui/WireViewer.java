package com.hexastyle.simulatorgui;

import java.awt.*;
import java.awt.geom.*;
import java.io.IOException;

/**
 * Created by Hexastyle on 3/17/17.
 */
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
