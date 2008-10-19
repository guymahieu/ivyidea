package be.clarent.plugintest;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.SortedBag;

import java.util.Comparator;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: guy
 * Date: 28-apr-2008
 * Time: 18:09:00
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    public static void main(String[] args) {
        Bag bag =new SortedBag() {
            public Comparator comparator() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object first() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object last() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getCount(Object o) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean add(Object o) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean add(Object o, int i) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean remove(Object o) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean remove(Object o, int i) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Set uniqueSet() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int size() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean containsAll(Collection collection) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean addAll(Collection c) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean removeAll(Collection collection) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean retainAll(Collection collection) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void clear() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public Iterator iterator() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isEmpty() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean contains(Object o) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object[] toArray() {
                return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object[] toArray(Object[] a) {
                return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

}
