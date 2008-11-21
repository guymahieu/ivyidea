package org.clarent.ivyidea.exception.ui;

import com.intellij.ui.components.labels.LinkListener;

public class LinkBehavior {
    private final String linkText;
    private final LinkListener linkListener;
    private final Object data;

    public LinkBehavior(String linkText, LinkListener linkListener, Object data) {
        this.linkText = linkText;
        this.linkListener = linkListener;
        this.data = data;
    }

    public String getLinkText() {
        return linkText;
    }

    public LinkListener getLinkListener() {
        return linkListener;
    }

    public Object getData() {
        return data;
    }
}
