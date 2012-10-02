package org.vaadin.tori.component;

import org.vaadin.hene.expandingtextarea.ExpandingTextArea;
import org.vaadin.hene.expandingtextarea.widgetset.client.ui.ExpandingTextAreaState;
import org.vaadin.tori.widgetset.client.ui.expandingtextarea.ToriExpandingTextAreaClientRpc;

@SuppressWarnings("serial")
public class ToriExpandingTextArea extends ExpandingTextArea {

    public void blur() {
        getRpcProxy(ToriExpandingTextAreaClientRpc.class).blur();
    }

    public void setMaxRows(final int maxRowsExpanded) {
        getState().maxRows = maxRowsExpanded;
    }

    @Override
    protected ExpandingTextAreaState getState() {
        return super.getState();
    }
}
