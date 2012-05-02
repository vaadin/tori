package org.vaadin.tori.component;

import org.vaadin.hene.expandingtextarea.ExpandingTextArea;
import org.vaadin.tori.widgetset.client.ui.expandingtextarea.ToriExpandingTextAreaClientRpc;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

@SuppressWarnings("serial")
public class ToriExpandingTextArea extends ExpandingTextArea {

    private Integer maxRows = null;

    public void blur() {
        getRpcProxy(ToriExpandingTextAreaClientRpc.class).blur();
    }

    public void setMaxRows(final int maxRowsExpanded) {
        maxRows = maxRowsExpanded;
    }

    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        super.paintContent(target);
        if (maxRows != null) {
            target.addAttribute("maxRows", maxRows);
        }
    }
}
