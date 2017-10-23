package com.hexastyle.simulatorgui;

import java.awt.*;
import java.io.IOException;

/**
 * Created by Hexastyle on 2017.07.08..
 */
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

