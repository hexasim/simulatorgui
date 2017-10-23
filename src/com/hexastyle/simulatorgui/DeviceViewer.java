package com.hexastyle.simulatorgui;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static java.awt.Font.MONOSPACED;

/**
 * Created by Hexastyle on 3/10/17.
 */
public class DeviceViewer extends Viewer {
    int left;
    int bottom;
    int angleIndex;
    int numberOfIos;
    int pcIndex;
    DeviceViewerType deviceViewerType;
    int ioIndex;
    Color ioColors[];
    boolean[] inverted;
    static DeviceViewerType common0Type;
    static BasicStroke basicStroke = new BasicStroke(4);

    static {
        common0Type = new DeviceViewerType();
        common0Type.numberOfSegments = 0;
        common0Type.minNumberOfIos = 0;
    }


    class Section {
        Color color;
        int rotate;
        int x;
        int y;
        int fontSize;
        String string;
        Path2D.Double path;
    }

    Section sections[];
    Color color;

    DeviceViewer() {
    }

    DeviceViewer(int pcIndex, ArrayList<DeviceViewerType> deviceViewerTypes, long uniqueId) {
        this.pcIndex = pcIndex;
        this.uniqueId = uniqueId;
        deviceViewerType = deviceViewerTypes.get(pcIndex);
        ioColors = new Color[deviceViewerType.maxNumberOfIos];
        inverted = new boolean[deviceViewerType.maxNumberOfIos];
        if (deviceViewerType.segmentPath != null) {
            sections = new Section[deviceViewerType.segmentPath.length];
            for (int i = 0; i < deviceViewerType.segmentPath.length; i++) {
                sections[i] = new Section();
            }
        }
    }

    synchronized int receive(Connection con) throws IOException {
        left = con.input.readInt();
        bottom = con.input.readInt();
        angleIndex = con.input.readInt();
        numberOfIos = con.input.readInt();
        color = getColor(con.input.readInt());
        for (int i = 0; i < numberOfIos; i++) {
            ioColors[i] = getColor(con.input.readInt());
            inverted[i] = con.input.readBoolean();
        }
        ioIndex = numberOfIos - deviceViewerType.minNumberOfIos;
        if (ioIndex < 0) {
            System.out.println("receive error " + ioIndex + " numberOfIos= " + numberOfIos);
        }
        int sym = con.input.readInt();
        while ((sym == ProtocolConstants.A2PC.COLOR_PART.ordinal()) ||
                (sym == ProtocolConstants.A2PC.GRAPH_PART.ordinal()) ||
                (sym == ProtocolConstants.A2PC.STRING_PART.ordinal())) {
            if (sym == ProtocolConstants.A2PC.COLOR_PART.ordinal()) {
                int pos = con.input.readInt();
                int partColor = con.input.readInt();
                sections[pos].color = getColor(partColor);
                sym = con.input.readInt();
            } else if (sym == ProtocolConstants.A2PC.GRAPH_PART.ordinal()) {
                int sectionIndex = con.input.readInt();
                sections[sectionIndex].path = new Path2D.Double();
                int pathType = con.input.readInt();
                while (pathType != ProtocolConstants.END) {
                    deviceViewerType.getOnePath(con, pathType, sections[sectionIndex].path);
                    pathType = con.input.readInt();
                }
                sym = con.input.readInt();
            } else if (sym == ProtocolConstants.A2PC.STRING_PART.ordinal()) {
                int sectionIndex = con.input.readInt();
                sections[sectionIndex].rotate = con.input.readInt();
                sections[sectionIndex].x = con.input.readInt();
                sections[sectionIndex].y = con.input.readInt();
                sections[sectionIndex].fontSize = con.input.readInt();
                sections[sectionIndex].string = con.input.readUTF();
                sym = con.input.readInt();
            }
        }
        return sym;
    }

    synchronized private void draw0(Graphics2D g2d) {
        Color savedColor = g2d.getColor();
        g2d.setColor(color);
        g2d.draw(deviceViewerType.path);
        for (DeviceViewerType.Label label : deviceViewerType.labels) {
            g2d.drawString(label.s, label.x, label.y);
        }
        for (int i = 0; i < numberOfIos; i++) {
            g2d.setColor(ioColors[i]);
            if (ioIndex < 0) {
                System.out.println("draw0 error");
            }
            g2d.draw(deviceViewerType.ioPath[ioIndex][i][inverted[i] ? 1 : 0]);
        }
        for (int i = 0; i < deviceViewerType.numberOfSegments; i++) {
            g2d.setColor(sections[i].color);
            g2d.draw(deviceViewerType.segmentPath[i]);
            g2d.fill(deviceViewerType.segmentPath[i]);
            synchronized (sections) {
                if (sections[i].path != null) {
                    g2d.draw(sections[i].path);
                }
            }
            if (sections[i].string != null) {
                AffineTransform orig = g2d.getTransform();
                switch (sections[i].rotate) {
                    case 0:
                        break;
                    case 2:
                        g2d.translate(0, 0);
                        g2d.rotate(Math.PI / 2, deviceViewerType.xPos, deviceViewerType.yPos);
                        break;
                    case 4:
                        g2d.translate(0, 0);
                        g2d.rotate(Math.PI, deviceViewerType.xPos, deviceViewerType.yPos);
                        break;
                    case 6:
                        g2d.translate(0, 0);
                        g2d.rotate(3 * Math.PI / 2, deviceViewerType.xPos, deviceViewerType.yPos);
                        break;
                    default:
                }
                g2d.setFont(new Font(MONOSPACED, Font.PLAIN, sections[i].fontSize));
                g2d.drawString(sections[i].string, sections[i].x, sections[i].y);
                g2d.setTransform(orig);
            }
        }

        g2d.setColor(savedColor);
    }

    synchronized public void drawText(Graphics2D g2d) {
        Iterator<DeviceViewerType.ConstText> iterator = deviceViewerType.constTexts.iterator();
        while (iterator.hasNext()) {
            g2d.setFont(new Font(MONOSPACED,
                    Font.PLAIN, deviceViewerType.fontSize));
            DeviceViewerType.ConstText constText = iterator.next();
            g2d.drawString(constText.s, constText.x, constText.y);
        }
    }

    public void draw(Graphics2D g2d) {
        AffineTransform orig = g2d.getTransform();
        switch (angleIndex) {
            case 0:
                g2d.translate(left, bottom);
                draw0(g2d);
                break;
            case 2:
                g2d.translate(left, bottom);
                g2d.rotate(Math.PI / 2, deviceViewerType.xPos, deviceViewerType.yPos);
                draw0(g2d);
                break;
            case 4:
                g2d.translate(left, bottom);
                g2d.rotate(Math.PI, deviceViewerType.xPos, deviceViewerType.yPos);
                draw0(g2d);
                break;
            case 6:
                g2d.translate(left, bottom);
                g2d.rotate(3 * Math.PI / 2, deviceViewerType.xPos, deviceViewerType.yPos);
                draw0(g2d);
                break;
            default:
        }
        if (deviceViewerType.constTexts != null) {
            drawText(g2d);
        }
        g2d.setTransform(orig);
    }
}
