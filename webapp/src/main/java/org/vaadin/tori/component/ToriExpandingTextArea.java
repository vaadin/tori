package org.vaadin.tori.component;

import org.vaadin.hene.expandingtextarea.ExpandingTextArea;
import org.vaadin.tori.widgetset.client.ui.expandingtextarea.VToriExpandingTextArea;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.ClientWidget;

/**
 * Extends the {@link ExpandingTextArea} by providing {@link #blur()} method.
 */
@SuppressWarnings("serial")
@ClientWidget(VToriExpandingTextArea.class)
public class ToriExpandingTextArea extends ExpandingTextArea {

    private boolean blurRequested;

    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        super.paintContent(target);

        target.addAttribute(VToriExpandingTextArea.ATTR_BLUR_REQUESTED,
                blurRequested);
        blurRequested = false;
    }

    public void blur() {
        blurRequested = true;
        requestRepaint();
    }

}
