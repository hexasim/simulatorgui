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

public class ProtocolConstants {
    public static final int END = -1;

    enum PC2A {
        MENU, SCHEMATIC, DIFF_SCHEMATIC, BREADBOARD, DIFF_BREADBOARD, DEVICE_VIEWERS, REMOTE_MOUSE_EVENT, MOUSE_LEFT_PRESSED, MOUSE_MIDDLE_PRESSED,
        MOUSE_RIGHT_PRESSED, MOUSE_RELEASED, MOUSE_MOVED, MOUSE_DRAGGED, REMOTE_BUTTON_EVENT,
        UNDO, REDO, RUN, EDIT, CANCEL, LEFT, RIGHT, DEL, CLONE, LOCK, UNLOCK, HORIZONTAL, VERTICAL, ROTATE, ADD,
    }

    enum A2PC {
        MENU, SCHEMATIC, DIFF_SCHEMATIC, BREADBOARD, DIFF_BREADBOARD, DEVICE_VIEWERS, SYMBOL_TYPE, PATH_LINE, PATH_ARC, PATH_RECT,
        PATH_ROUND_RECT, PATH_CIRCLE, PATH_CHAR, MIN_NUMBER_OF_IOS, MAX_NUMBER_OF_IOS, WIRE_INSTANCE, BB_WIRE_INSTANCE, DEVICE_VIEWER_INSTANCE, ANNOTATION_INSTANCE, INSTANCE_WITH_TYPE,
        COLOR_IO, COLOR_PART, STRING_PART, GRAPH_PART, DELETE, CURSOR, BB_RESISTOR_INSTANCE, BB_LED_INSTANCE, BB_RESISTOR_LED_INSTANCE,
        PATH_POLYGON, DEVICE_GROUP_MENU, LAST_A2PC,
    }

    enum SymbolType {
        ANSI, IEC, DIN
    }

}
