package org.clarent.ivyidea.exception.ui;

import com.intellij.ui.components.labels.LinkListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines the behavior of a hyperlink label.
 */
public class LinkBehavior { // is behavior a good name? it has a presentation part as well...

    private final String linkText;
    private final LinkListener linkListener;
    private final Object data;

    /**
     * Constructs a new LinkBehavior instance.
     *
     * @param linkText      the text of the hyperlink label
     * @param linkListener  the listener that is invoked when the link is clicked
     * @param data          this is passed back to the linkListener as a parameter when the link is clicked; can
     *                      be used to store some data that is needed during the handling of the link-click
     */
    public LinkBehavior(@NotNull String linkText, @NotNull LinkListener linkListener, @Nullable Object data) {
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
