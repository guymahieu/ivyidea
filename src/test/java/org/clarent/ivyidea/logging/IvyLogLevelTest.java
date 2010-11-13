package org.clarent.ivyidea.logging;

import org.junit.Test;

import static junit.framework.Assert.*;
import static org.clarent.ivyidea.logging.IvyLogLevel.*;

/**
 *  @author Guy Mahieu
 */
public class IvyLogLevelTest {

    @Test
    public void test_isMoreVerboseThan() {
        assertTrue(Error.isMoreVerboseThan(None));
        assertTrue(Info.isMoreVerboseThan(None));
        assertFalse(Error.isMoreVerboseThan(Debug));
    }
}
