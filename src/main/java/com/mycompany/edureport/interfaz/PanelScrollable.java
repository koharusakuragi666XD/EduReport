package com.mycompany.edureport.interfaz;
import com.mycompany.edureport.*;

import javax.swing.*;
import java.awt.*;

/** Panel que se ajusta al ancho del JScrollPane y solo crece hacia abajo. */
class PanelScrollable extends JPanel implements Scrollable {

    PanelScrollable(LayoutManager layout) {
        super(layout);
    }

    @Override public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override public int getScrollableUnitIncrement(Rectangle r, int orientacion, int dir) {
        return 20;
    }

    @Override public int getScrollableBlockIncrement(Rectangle r, int orientacion, int dir) {
        return orientacion == SwingConstants.VERTICAL ? r.height - 20 : r.width - 20;
    }

    @Override public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
