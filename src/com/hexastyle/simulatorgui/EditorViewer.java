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


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.hexastyle.simulatorgui.Viewer.getColor;
import static java.awt.event.MouseEvent.*;

public class EditorViewer extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {
    private static final int GUI_VERSION = 2;
    static float zoomFactor = 1;
    static int xOrig;
    static int yOrig;
    static double xOffset = 0;
    static double yOffset = 0;
    static Point windowOrigo = new Point();
    static Point origoOnBreadboard = new Point();
    Connection con;
    JTextArea output;
    JScrollPane scrollPane;
    String newline = "\n";
    private float prevZoomFactor = 1;
    private double xPos = 0;
    private double yPos = 0;
    private ArrayList<ArrayList<DeviceViewerType>> symbolTypes;
    private ArrayList<DeviceViewerType> deviceViewerTypes;
    private ConcurrentHashMap<Long, Viewer> viewers;
    private Panel panel;
    private double translateX = 0;
    private double translateY = 0;
    private AffineTransform transform = new AffineTransform();
    private Point2D.Double p1 = new Point2D.Double();
    private Point2D.Double p2 = new Point2D.Double();
    private GetSchematic getSchematic;
    private Color backgroundColor;
    private Color foregroundColor;
    private boolean mouseMoved = false;
    private boolean leftButtonPressed = false;
    private boolean middleButtonPressed = false;
    private boolean rightButtonPressed = false;

    EditorViewer() {
    }


    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
        return classString.substring(dotIndex + 1);
    }

    boolean clientConnect(String url, int port, String password) {
        con = new ClientConnection();
        if (con != null) {
            return ((ClientConnection) con).connect(url, port, password);
        }
        return false;
    }

    private Point2D.Double transformPoint(Point p1) throws NoninvertibleTransformException {
        AffineTransform inverse = transform.createInverse();
        inverse.transform(p1, p2);
        return p2;
    }

    public void mouseClicked(MouseEvent e) {
    }

    private void sendMouseEvent(double xPos, double yPos, int ordinal2) {
        try {
            con.output.writeInt(ProtocolConstants.PC2A.REMOTE_MOUSE_EVENT.ordinal());
            con.output.writeInt(ordinal2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendPoint(xPos, yPos);
    }

    private void sendMouseEvent(double oldX, double oldY, double newX, double newY, int ordinal2) {
        try {
            con.output.writeInt(ProtocolConstants.PC2A.REMOTE_MOUSE_EVENT.ordinal());
            con.output.writeInt(ordinal2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendPoint(oldX, oldY);
        sendPoint(newX, newY);
    }

    private void sendPoint(double oldX, double oldY) {
        p1.setLocation(oldX, oldY);
        try {
            transform.inverseTransform(p1, p2);
        } catch (NoninvertibleTransformException e1) {
            e1.printStackTrace();
        }
        try {
            con.output.writeFloat((float) p2.getX());
            con.output.writeFloat((float) p2.getY());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void mousePressed(MouseEvent e) {
        mouseMoved = false;
        xPos = e.getX();
        yPos = e.getY();
        switch (e.getButton()) {
            case BUTTON1:
                leftButtonPressed = true;
                sendMouseEvent(xPos, yPos, ProtocolConstants.PC2A.MOUSE_LEFT_PRESSED.ordinal());
                break;
            case BUTTON2:
                middleButtonPressed = true;
                sendMouseEvent(xPos, yPos, ProtocolConstants.PC2A.MOUSE_MIDDLE_PRESSED.ordinal());
                break;
            case BUTTON3:
                rightButtonPressed = true;
                sendMouseEvent(xPos, yPos, ProtocolConstants.PC2A.MOUSE_RIGHT_PRESSED.ordinal());
                break;
            default:
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (!(rightButtonPressed)) {
            sendMouseEvent(xPos, yPos, ProtocolConstants.PC2A.MOUSE_RELEASED.ordinal());
        }
        leftButtonPressed = false;
        middleButtonPressed = false;
        rightButtonPressed = false;
        mouseMoved = false;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        mouseMoved = true;
        double oldXPos = xPos;
        double oldYPos = yPos;
        xPos = e.getX();
        yPos = e.getY();
        if (leftButtonPressed) {
            sendMouseEvent(oldXPos, oldYPos, xPos, yPos, ProtocolConstants.PC2A.MOUSE_DRAGGED.ordinal());
        } else if (middleButtonPressed) {
        } else if (rightButtonPressed) {
            xOffset += -oldXPos + xPos;
            yOffset += -oldYPos + yPos;
            transform.translate((-oldXPos + xPos) / zoomFactor, (-oldYPos + yPos) / zoomFactor);
        }
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            if (e.getUnitsToScroll() > 0) {
                if (zoomFactor > 0.1) {
                    doZoom(e, 0.9);
                }
            } else {
                if (zoomFactor < 32) {
                    doZoom(e, 1.1);
                }
            }
        } else { // scroll type == MouseWheelEvent.WHEEL_BLOCK_SCROLL
        }
    }

    private void showMatrix(AffineTransform at) {
        double[] matrix = new double[6];
        at.getMatrix(matrix);  // { m00 m10 m01 m11 m02 m12 }
        int[] loRow = {0, 0, 1};
        for (int i = 0; i < 3; i++) {
            System.out.print("[ ");
            for (int j = i; j < matrix.length; j += 2) {
                System.out.printf("%5.1f ", matrix[j]);
            }
            System.out.print("]\n");
        }
        System.out.print("]\n");
    }

    private void doZoom(MouseWheelEvent e, double dz) {
        prevZoomFactor = zoomFactor;
        translateX = e.getX();
        translateY = e.getY();

        p1.setLocation(translateX, translateY);
        try {
            transform.inverseTransform(p1, p2);
        } catch (NoninvertibleTransformException e1) {
            e1.printStackTrace();
        }

        transform.setToIdentity();

        zoomFactor *= dz;
        transform.translate((p1.getX()), (p1.getY()));
        transform.scale(zoomFactor, zoomFactor);
        transform.translate((-p2.getX()), (-p2.getY()));
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }

    private JPopupMenu loginMenu;
    private JLabel passwordLabel = new JLabel("Password:");
    JButton loginButton = new JButton("Login");
    JButton undoButton = new JButton("UNDO");
    JButton redoButton = new JButton("REDO");
    JButton runButton = new JButton("RUN");
    JButton editButton = new JButton("EDIT");
    JButton cancelButton = new JButton("CANCEL");
    JButton leftButton = new JButton("LEFT");
    JButton rightButton = new JButton("RIGHT");
    JButton delButton = new JButton("DEL");
    JButton cloneButton = new JButton("CLONE");
    JButton lockButton = new JButton("LOCK");
    JButton unlockButton = new JButton("UNLOCK");
    JButton horizontalButton = new JButton("HORIZONTAL");
    JButton verticalButton = new JButton("VERTICAL");
    JButton rotateButton = new JButton("ROTATE");
    JButton addInputButton = new JButton("IN++");
    JPasswordField passwordField = new JPasswordField();
    JMenu addMenu = new JMenu("ADD");

    public void createMenu() {
        JFrame frame = new JFrame("V2 Simulator GUI");
        frame.setSize(1600, 1024);
        frame.setLayout(new BorderLayout());
        frame.setVisible(true);
        frame.toFront();
        JTextField ipTextField = new JTextField(Prop.url);
        JTextField portTextField = new JTextField(Integer.toString(Prop.port));

        addMouseWheelListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(loginButton);
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Prop.url = ipTextField.getText();
                String portNumberString = portTextField.getText();
                try {
                    Prop.port = Integer.parseInt(portNumberString);
                } catch (NumberFormatException ne) {

                }
                Prop.writeSettings();
                String pwd = new String(passwordField.getPassword());
                if (clientConnect(Prop.url, Prop.port, pwd)) {
                    if (checkVersion()) {
                        askDeviceViewerTypes();
                        frame.invalidate();
                        System.out.println("Connected");
                    } else {
                        System.out.println("Simulator GUI version missmatch. Please download the latest versions.");
                    }
                } else {
                    System.out.println("Connection/Password error");
                }
            }
        });
        menuBar.add(passwordLabel);
        menuBar.add(passwordField);
        menuBar.add(ipTextField);
        menuBar.add(portTextField);
        menuBar.add(addMenu);
        menuBar.add(editButton);
        menuBar.add(cancelButton);
        addMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.EDIT.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(runButton);
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.RUN.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.CANCEL.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(undoButton);
        undoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.UNDO.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(redoButton);
        redoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.REDO.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(leftButton);
        leftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.LEFT.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(rightButton);
        rightButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.RIGHT.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(delButton);
        delButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.DEL.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(cloneButton);
        cloneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.CLONE.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(lockButton);
        lockButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.LOCK.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(unlockButton);
        unlockButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.UNLOCK.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(horizontalButton);
        horizontalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.HORIZONTAL.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(verticalButton);
        verticalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.VERTICAL.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(rotateButton);
        rotateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.ROTATE.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menuBar.add(addInputButton);
        addInputButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                    con.output.writeInt(ProtocolConstants.PC2A.ADD_INPUT.ordinal());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        frame.setJMenuBar(menuBar);
        frame.add(this);
        frame.setVisible(true);
    }

    private boolean checkVersion() {
        try {
            con.output.writeInt(ProtocolConstants.PC2A.ASK_VERSION.ordinal());
            con.output.writeInt(GUI_VERSION);
            int cmd = con.input.readInt();
            if (cmd != ProtocolConstants.A2PC.ANSWER_VERSION.ordinal()) {
                System.out.println("The simulator version is older than GUI version");
                return false;
            }
            int version = con.input.readInt();
            if (version != GUI_VERSION) {
                System.out.println("Simulator version:" + version + " GUI version:" + GUI_VERSION);
                return false;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return true;
    }

    class SubActionListener implements ActionListener {
        int group;
        int member;

        SubActionListener(int group, int member) {
            this.group = group;
            this.member = member;
        }

        public void actionPerformed(ActionEvent evt) {
            try {
                con.output.writeInt(ProtocolConstants.PC2A.REMOTE_BUTTON_EVENT.ordinal());
                con.output.writeInt(ProtocolConstants.PC2A.ADD.ordinal());
                con.output.writeInt(group);
                con.output.writeInt(member);
                con.output.writeUTF(evt.getActionCommand());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void getDeviceGroupMenu() throws java.io.EOFException {
        String s;
        addMenu.removeAll();
        try {
            int num = con.input.readInt();
            while (num-- > 0) {
                s = con.input.readUTF();
                JMenu subMenu = new JMenu(s);
                addMenu.add(subMenu);
                int group = con.input.readInt();
                int subNum = con.input.readInt();
                while (subNum-- > 0) {
                    s = con.input.readUTF();
                    int element = con.input.readInt();
                    JMenuItem mi = new JMenuItem(s);
                    subMenu.add(mi);
                    mi.addActionListener(new SubActionListener(group, element));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        repaint();
    }

    public void getDeviceViewerTypes() throws java.io.EOFException {
        try {
            int symbolType = con.input.readInt();
            while (symbolType != ProtocolConstants.END) {
                deviceViewerTypes = new ArrayList<DeviceViewerType>();
                symbolTypes = new ArrayList<ArrayList<DeviceViewerType>>();
                symbolTypes.add(deviceViewerTypes);
                int index = con.input.readInt();
                while (index != ProtocolConstants.END) {
                    DeviceViewerType deviceViewerType = new DeviceViewerType();
                    deviceViewerTypes.add(deviceViewerType);
                    deviceViewerType.getPaths(con);
                    index = con.input.readInt();
                }
                symbolType = con.input.readInt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void askDeviceViewerTypes() {
        int sym;
        try {
            con.output.writeInt(ProtocolConstants.PC2A.DEVICE_VIEWERS.ordinal());
            sym = con.input.readInt();
            if (sym != ProtocolConstants.A2PC.DEVICE_VIEWERS.ordinal()) {
                return;
            }
            getDeviceViewerTypes();
            getSchematic = new GetSchematic();
            Thread getSchematicThread = new Thread(getSchematic);
            getSchematicThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.transform(transform);
        windowOrigo.x = EditorViewer.xOrig;
        windowOrigo.y = EditorViewer.yOrig;
        try {
            transform.inverseTransform(windowOrigo, origoOnBreadboard);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
        if (viewers != null) {
            setBackground(backgroundColor);
            g2d.setColor(foregroundColor);
            xOrig = getWidth() / 2;
            yOrig = getHeight() / 2;
            for (Map.Entry<Long, Viewer> entry : viewers.entrySet()) {
                entry.getValue().draw(g2d);
            }
        }
    }

    class GetSchematic implements Runnable {

        synchronized public boolean get() throws java.io.EOFException {
            try {
                int sym = con.input.readInt();
                if ((sym >= ProtocolConstants.A2PC.LAST_A2PC.ordinal()) || (sym < 0)) {
                    return false;
                }
                ProtocolConstants.A2PC cmd = ProtocolConstants.A2PC.values()[sym];
                switch (cmd) {
                    case ANSWER_VERSION:
                        int version = con.input.readInt();
                        break;
                    case SCHEMATIC:
                        backgroundColor = getColor(con.input.readInt());
                        foregroundColor = getColor(con.input.readInt());
                        viewers = new ConcurrentHashMap<>();
                        sym = con.input.readInt();
                        while (sym != ProtocolConstants.END) {
                            if ((sym >= ProtocolConstants.A2PC.LAST_A2PC.ordinal()) || (sym < 0)) {
                                return false;
                            }
                            cmd = ProtocolConstants.A2PC.values()[sym];
                            switch (cmd) {
                                case DEVICE_VIEWER_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    int pcIndex = con.input.readInt();
                                    DeviceViewer deviceViewer = new DeviceViewer(pcIndex, symbolTypes.get(0), uniqueId);
                                    sym = deviceViewer.receive(con);
                                    viewers.put(uniqueId, deviceViewer);
                                }
                                break;
                                case WIRE_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    WireViewer wireViewer = new WireViewer(uniqueId);
                                    sym = wireViewer.receive(con);
                                    viewers.put(uniqueId, wireViewer);
                                }
                                break;
                                case BB_WIRE_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    BBWireViewer bbWireViewer = new BBWireViewer(uniqueId);
                                    sym = bbWireViewer.receive(con);
                                    viewers.put(uniqueId, bbWireViewer);
                                }
                                break;
                                case BB_LED_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    BBLedViewer bbLedViewer = new BBLedViewer(uniqueId);
                                    sym = bbLedViewer.receive(con);
                                    viewers.put(uniqueId, bbLedViewer);
                                }
                                break;
                                case BB_RESISTOR_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    BBResistorViewer bbResistorViewer = new BBResistorViewer(uniqueId);
                                    sym = bbResistorViewer.receive(con);
                                    viewers.put(uniqueId, bbResistorViewer);
                                }
                                break;
                                case BB_RESISTOR_LED_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    BBResistorLedViewer bbResistorLedViewer = new BBResistorLedViewer(uniqueId);
                                    sym = bbResistorLedViewer.receive(con);
                                    viewers.put(uniqueId, bbResistorLedViewer);
                                }
                                break;
                                case ANNOTATION_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    AnnotationViewer annotationViewer = new AnnotationViewer(uniqueId);
                                    sym = annotationViewer.receive(con);
                                    viewers.put(uniqueId, annotationViewer);
                                }
                                break;
                                case CURSOR: {
                                    long uniqueId = con.input.readLong();
                                    CursorViewer cursorViewer = new CursorViewer(uniqueId);
                                    sym = cursorViewer.receive(con);
                                    viewers.put(uniqueId, cursorViewer);
                                }
                                break;
                                case INSTANCE_WITH_TYPE: {
                                    long uniqueId = con.input.readLong();
                                    InstanceViewer instanceViewer = new InstanceViewer(uniqueId);
                                    sym = instanceViewer.receive(con);
                                    viewers.put(uniqueId, instanceViewer);
                                }
                                break;
                                case DELETE: {
                                    long uniqueId = con.input.readLong();
                                    viewers.remove(uniqueId);
                                    sym = con.input.readInt();
                                }
                                break;
                                case DEVICE_GROUP_MENU:
                                    getDeviceGroupMenu();
                                    sym = con.input.readInt();
                                    break;
                                default:
                                    sym = con.input.readInt();
                            }
                        }
                        break;
                    case DIFF_SCHEMATIC:
                        sym = con.input.readInt();
                        while (sym != ProtocolConstants.END) {
                            if ((sym >= ProtocolConstants.A2PC.LAST_A2PC.ordinal()) || (sym < 0)) {
                                return false;
                            }
                            cmd = ProtocolConstants.A2PC.values()[sym];
                            switch (cmd) {
                                case DEVICE_VIEWER_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    int pcIndex = con.input.readInt();
                                    Viewer deviceViewer = viewers.get(uniqueId);
                                    if (deviceViewer == null) {
                                        deviceViewer = new DeviceViewer(pcIndex, symbolTypes.get(0), uniqueId);
                                        viewers.put(uniqueId, deviceViewer);
                                    }
                                    sym = deviceViewer.receive(con);
                                }
                                break;
                                case WIRE_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    Viewer wireViewer = viewers.get(uniqueId);
                                    if (wireViewer == null) {
                                        wireViewer = new WireViewer(uniqueId);
                                        viewers.put(uniqueId, wireViewer);
                                    }
                                    wireViewer.clear();
                                    sym = wireViewer.receive(con);
                                }
                                break;
                                case BB_WIRE_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    BBWireViewer bbWireViewer = new BBWireViewer(uniqueId);
                                    sym = bbWireViewer.receive(con);
                                    viewers.put(uniqueId, bbWireViewer);
                                }
                                break;
                                case BB_LED_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    BBLedViewer bbLedViewer = new BBLedViewer(uniqueId);
                                    sym = bbLedViewer.receive(con);
                                    viewers.put(uniqueId, bbLedViewer);
                                }
                                break;
                                case BB_RESISTOR_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    BBResistorViewer bbResistorViewer = new BBResistorViewer(uniqueId);
                                    sym = bbResistorViewer.receive(con);
                                    viewers.put(uniqueId, bbResistorViewer);
                                }
                                break;
                                case BB_RESISTOR_LED_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    BBResistorLedViewer bbResistorLedViewer = new BBResistorLedViewer(uniqueId);
                                    sym = bbResistorLedViewer.receive(con);
                                    viewers.put(uniqueId, bbResistorLedViewer);
                                }
                                break;
                                case ANNOTATION_INSTANCE: {
                                    long uniqueId = con.input.readLong();
                                    Viewer annotationViewer = viewers.get(uniqueId);
                                    if (annotationViewer == null) {
                                        annotationViewer = new AnnotationViewer(uniqueId);
                                        viewers.put(uniqueId, annotationViewer);
                                    }
                                    sym = annotationViewer.receive(con);
                                }
                                break;
                                case INSTANCE_WITH_TYPE: {
                                    long uniqueId = con.input.readLong();
                                    InstanceViewer instanceViewer = new InstanceViewer(uniqueId);
                                    sym = instanceViewer.receive(con);
                                    viewers.put(uniqueId, instanceViewer);
                                }
                                break;
                                case CURSOR: {
                                    long uniqueId = con.input.readLong();
                                    Viewer cursorViewer = viewers.get(uniqueId);
                                    if (cursorViewer == null) {
                                        cursorViewer = new AnnotationViewer(uniqueId);
                                        viewers.put(uniqueId, cursorViewer);
                                    }
                                    sym = cursorViewer.receive(con);
                                }
                                break;
                                case DELETE: {
                                    long uniqueId = con.input.readLong();
                                    viewers.remove(uniqueId);
                                    sym = con.input.readInt();
                                }
                                break;
                                default:
                            }
                        }
                        break;
                    case DEVICE_VIEWERS:
                        getDeviceViewerTypes();
                        break;
                    default:
                        return false;
                }
                repaint();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public void run() {
            boolean stat = false;
            try {
                con.output.writeInt(ProtocolConstants.PC2A.SCHEMATIC.ordinal());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while (stat = get()) {
                }
            } catch (EOFException e) {
                e.printStackTrace();
            }
        }
    }

}
