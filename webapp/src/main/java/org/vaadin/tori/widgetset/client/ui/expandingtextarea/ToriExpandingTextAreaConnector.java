package org.vaadin.tori.widgetset.client.ui.expandingtextarea;

import org.vaadin.hene.expandingtextarea.widgetset.client.ui.ExpandingTextAreaConnector;
import org.vaadin.tori.component.ToriExpandingTextArea;

import com.vaadin.shared.ui.Connect;

@Connect(ToriExpandingTextArea.class)
@SuppressWarnings("serial")
public class ToriExpandingTextAreaConnector extends ExpandingTextAreaConnector {
    @Override
    protected void init() {
        super.init();
        registerRpc(ToriExpandingTextAreaClientRpc.class,
                new ToriExpandingTextAreaClientRpc() {
                    @Override
                    public void blur() {
                        getWidget().setFocus(false);
                    }
                });
    }
}
