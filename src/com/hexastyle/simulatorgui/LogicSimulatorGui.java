package com.hexastyle.simulatorgui;

public class LogicSimulatorGui {
    private static EditorViewer editorViewer;

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        Prop.readSettings();         // read default/saved ip, port
        editorViewer = new EditorViewer();
        editorViewer.createMenu();
    }
}
