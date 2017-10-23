package com.hexastyle.simulatorgui;

import java.awt.*;
import java.io.IOException;

/**
 * Created by Hexastyle on 3/22/17.
 */
public class AnnotationViewer extends Viewer {
    private String id;
    private float x;
    private float y;
    int fontSize;
    Color color;

    AnnotationViewer(long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void draw(Graphics2D g2d) {
        Color savedCcolor = g2d.getColor();
        g2d.setColor(color);
        g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
        g2d.drawString(id, x, y);
        g2d.setColor(savedCcolor);
    }

    int receive(Connection con) throws IOException {
        x = con.input.readInt();
        y = con.input.readInt();
        color = getColor(con.input.readInt());
        fontSize = con.input.readInt();
        id = con.input.readUTF();
        int sym = con.input.readInt();
        return sym;
    }


}
