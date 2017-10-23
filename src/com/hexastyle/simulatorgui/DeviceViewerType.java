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
import java.util.ArrayList;
import java.util.LinkedList;

public class DeviceViewerType {
    Path2D.Double path;
    int minNumberOfIos;
    int maxNumberOfIos;
    Path2D.Double ioPath[][][];
    Path2D.Double segmentPath[];
    int numberOfSegments = 0;
    int xPos;
    int yPos;
    private int numberOfConstTexts = 0;
    int fontSize;

    class Label {
        int x;
        int y;
        String s;

        Label(int x, int y, String s) {
            this.x = x;
            this.y = y;
            this.s = s;
        }
    }

    ArrayList<Label> labels = new ArrayList<>();

    class ConstText {
        int x;
        int y;
        String s;

        ConstText(int x, int y, String s) {
            this.s = s;
            this.x = x;
            this.y = y;
        }
    }

    LinkedList<ConstText> constTexts;

    DeviceViewerType() {
    }

    private Shape getArc(Connection con) throws IOException {
        int arcleft = con.input.readInt();
        int arcbottom = con.input.readInt();
        int arcright = con.input.readInt();
        int arctop = con.input.readInt();
        int arcstartAngle = con.input.readInt();
        int arcAngle = con.input.readInt();
        return new Arc2D.Double(arcleft, arcbottom, arcright, arctop, arcstartAngle, arcAngle, Arc2D.OPEN);
    }

    private Shape getCircle(Connection con) throws IOException {
        int x = con.input.readInt();
        int y = con.input.readInt();
        int d = con.input.readInt();
        return new Ellipse2D.Double(x, y, d, d);
    }

    private Shape getLine(Connection con) throws IOException {
        int x0 = con.input.readInt();
        int y0 = con.input.readInt();
        int x1 = con.input.readInt();
        int y1 = con.input.readInt();
        return new Line2D.Double(new Point(x0, y0), new Point(x1, y1));
    }

    private Shape getPolygon(Connection con) throws IOException {
        int numberOfPoints = con.input.readInt();
        Polygon p = new Polygon();
        while (numberOfPoints-- > 0) {
            int x = con.input.readInt();
            int y = con.input.readInt();
            p.addPoint(x, y);
        }
        return p;
    }

    private Shape getRect(Connection con) throws IOException {
        int x0 = con.input.readInt();
        int y0 = con.input.readInt();
        int w = con.input.readInt();
        int h = con.input.readInt();
        return new Rectangle2D.Double(x0, y0, w, h);
    }

    private Shape getRoundRect(Connection con) throws IOException {
        int x0 = con.input.readInt();
        int y0 = con.input.readInt();
        int w = con.input.readInt();
        int h = con.input.readInt();
        int arcw = con.input.readInt();
        int arch = con.input.readInt();
        return new RoundRectangle2D.Double(x0, y0, w, h, arcw, arch);
    }

    private void getChar(Connection con) throws IOException {
        int x = con.input.readInt();
        int y = con.input.readInt();
        String s = con.input.readUTF();
        Label label = new Label(x, y, s);
        labels.add(label);
    }

    private void getBase(Connection con, int pathType, Path2D.Double path) throws IOException {
        Shape shape;
        while (pathType != ProtocolConstants.END) {
            switch (ProtocolConstants.A2PC.values()[pathType]) {
                case PATH_LINE:
                    shape = getLine(con);
                    path.append(shape, false);
                    break;
                case PATH_POLYGON:
                    shape = getPolygon(con);
                    path.append(shape, false);
                    break;
                case PATH_ARC:
                    shape = getArc(con);
                    path.append(shape, false);
                    break;
                case PATH_RECT:
                    shape = getRect(con);
                    path.append(shape, false);
                    break;
                case PATH_ROUND_RECT:
                    shape = getRoundRect(con);
                    path.append(shape, false);
                    break;
                case PATH_CIRCLE:
                    shape = getCircle(con);
                    path.append(shape, false);
                    break;
                case PATH_CHAR:
                    getChar(con);
                    break;
                default:
            }
            pathType = con.input.readInt();
        }
    }

    public void getOnePath(Connection con, int pathType, Path2D.Double path) throws IOException {
        Shape shape;
        switch (ProtocolConstants.A2PC.values()[pathType]) {
            case PATH_POLYGON:
                shape = getPolygon(con);
                path.append(shape, false);
                break;
            case PATH_LINE:
                shape = getLine(con);
                path.append(shape, false);
                break;
            case PATH_ARC:
                shape = getArc(con);
                path.append(shape, false);
                break;
            case PATH_RECT:
                shape = getRect(con);
                path.append(shape, false);
                break;
            case PATH_CIRCLE:
                shape = getCircle(con);
                path.append(shape, false);
                break;
            case PATH_CHAR:
                getChar(con);
                break;
            default:
                System.out.println(ProtocolConstants.A2PC.values()[pathType]);
        }
    }

    public void getPaths(Connection con) throws java.io.IOException {
//        System.out.println("called getPaths");
        path = new Path2D.Double();
        xPos = con.input.readInt();
        yPos = con.input.readInt();
        int pathType = con.input.readInt();
        getBase(con, pathType, path);
        minNumberOfIos = con.input.readInt();
        maxNumberOfIos = con.input.readInt();
        numberOfSegments = con.input.readInt();
        numberOfConstTexts = con.input.readInt();
        ioPath = new Path2D.Double[maxNumberOfIos - minNumberOfIos + 1][maxNumberOfIos][2];
        for (int i = minNumberOfIos; i <= maxNumberOfIos; i++) {
            for (int j = 0; j < i; j++) {
                pathType = con.input.readInt();
                ioPath[i - minNumberOfIos][j][0] = new Path2D.Double();
                ioPath[i - minNumberOfIos][j][1] = new Path2D.Double();
                while (pathType != ProtocolConstants.END) {
                    getOnePath(con, pathType, ioPath[i - minNumberOfIos][j][0]);
                    pathType = con.input.readInt();
                }
                pathType = con.input.readInt();
                while (pathType != ProtocolConstants.END) {
                    getOnePath(con, pathType, ioPath[i - minNumberOfIos][j][1]);
                    pathType = con.input.readInt();
                }
            }
        }
        if (numberOfSegments > 0) {
            segmentPath = new Path2D.Double[numberOfSegments];
            for (int i = 0; i < numberOfSegments; i++) {
                segmentPath[i] = new Path2D.Double();
                pathType = con.input.readInt();
                while (pathType != ProtocolConstants.END) {
                    getOnePath(con, pathType, segmentPath[i]);
                    pathType = con.input.readInt();
                }
            }
        }
        if (numberOfConstTexts > 0) {
            fontSize = con.input.readInt();
            constTexts = new LinkedList<ConstText>();
            for (int i = 0; i < numberOfConstTexts; i++) {
                int x = con.input.readInt();
                int y = con.input.readInt();
                String s = con.input.readUTF();
                constTexts.add(new ConstText(x, y, s));
            }
        }
    }

}
