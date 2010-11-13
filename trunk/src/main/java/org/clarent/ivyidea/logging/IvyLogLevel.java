package org.clarent.ivyidea.logging;

import com.intellij.execution.ui.ConsoleViewContentType;
import org.apache.ivy.util.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.intellij.execution.ui.ConsoleViewContentType.ERROR_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT;

public enum IvyLogLevel {

    None(Integer.MIN_VALUE, SYSTEM_OUTPUT),
    Error(Message.MSG_ERR, ERROR_OUTPUT),
    Warning(Message.MSG_WARN, SYSTEM_OUTPUT),
    Info(Message.MSG_INFO, SYSTEM_OUTPUT),
    Verbose(Message.MSG_VERBOSE, SYSTEM_OUTPUT),
    Debug(Message.MSG_DEBUG, SYSTEM_OUTPUT),
    All(Integer.MAX_VALUE, SYSTEM_OUTPUT);

    private static final Map<Integer, IvyLogLevel> loglevels;

    static {
        Map<Integer, IvyLogLevel> temp = new HashMap<Integer, IvyLogLevel>();
        for (IvyLogLevel ivyLogLevel : values()) {
            temp.put(ivyLogLevel.levelCode, ivyLogLevel);
        }
        loglevels = Collections.unmodifiableMap(temp);
    }

    private final int levelCode;
    private final ConsoleViewContentType contentType;

    public static IvyLogLevel fromLevelCode(int levelCode) {
        IvyLogLevel level = loglevels.get(levelCode);
        return level == null ? None : level;
    }

    public static IvyLogLevel fromName(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return None;
        }
    }

    IvyLogLevel(int levelCode, ConsoleViewContentType contentType) {
        this.levelCode = levelCode;
        this.contentType = contentType;
    }

    public boolean isMoreVerboseThan(IvyLogLevel otherLevel) {
        return otherLevel.levelCode <= levelCode;
    }

    public ConsoleViewContentType getContentType() {
        return contentType;
    }
}
