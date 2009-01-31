package org.clarent.ivyidea.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Guy Mahieu
 */
public class CollectionUtils {

    public static <T> List<T> createReversedList(List<T> inputList) {
        List<T> result = new ArrayList<T>(inputList); // avoid changing the input
        Collections.reverse(result);
        return result;
    }

}
