/*
 * Copyright 2010 Guy Mahieu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
