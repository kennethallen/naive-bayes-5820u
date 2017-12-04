package me.mostly.collection;

import java.util.*;
import java.util.function.Consumer;

/**
 * A collection that retains only the <em>n</em> greatest values added to it.  Elements are iterated in decreasing
 * order.
 *
 * @param <E> Element type
 */
public class Toplist<E> extends AbstractCollection<E> {
    final E[] top;
    int size = 0;
    final Comparator<? super E> comparator;

    public Toplist(int num) {
        this(num, (Comparator<E>) Comparator.naturalOrder());
    }

    public Toplist(int num, final Comparator<? super E> comparator) {
        top = (E[]) new Object[num];
        this.comparator = comparator;
    }

    public int maxSize() {
        return top.length;
    }

    public boolean isFull() {
        return size() == maxSize();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int idx = 0;
            boolean removed = false;

            @Override
            public boolean hasNext() {
                return idx < size;
            }

            @Override
            public E next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                removed = false;
                return top[idx++];
            }

            @Override
            public void remove() {
                if (idx == 0 || removed)
                    throw new IllegalStateException();
                System.arraycopy(top, idx + 1, top, idx, top.length - 1 - idx);
                size--;
                removed = true;
            }

            @Override
            public void forEachRemaining(Consumer<? super E> action) {
                for (; idx < size; idx++) {
                    action.accept(top[idx]);
                }
            }
        };
    }

    /**
     * If <code>e</code> exists in the toplist, return its index (<code>0</code> to <code>size() - 1</code>, inclusive).
     * If it isn't, return where it would be inserted as -(index + 1) (<code>-1</code> to <code>-size() - 1</code>).
     * @param e Element to search for
     * @return nonnegative existing index, or negative index after insertion (minus one)
     */
    public int indexOf(final E e) {
        int lo = 0, mid = size - 1, hi = size;
        while (lo < hi) {
            final int comp = comparator.compare(e, top[mid]);
            if (comp < 0) {
                lo = mid + 1;
            } else if (comp > 0) {
                hi = mid;
            } else {
                return mid;
            }
            mid = (lo + hi) >> 1;
        }
        return -(lo + 1);
    }

    @Override
    public boolean add(final E e) {
        // If idx < 0, just negate.  If idx >= 0, add 1 because we don't need to move equal element.
        int idx = Math.abs(indexOf(e) + 1);
        if (idx >= top.length)
            return false;
        System.arraycopy(top, idx, top, idx + 1, top.length - 1 - idx);
        top[idx] = e;
        if (!isFull())
            size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        final int idx = indexOf((E) o);
        if (idx < 0)
            return false;
        System.arraycopy(top, idx + 1, top, idx, top.length - 1 - idx);
        size--;
        return true;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf((E) o) >= 0;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (int srcIdx = 0; srcIdx < size; srcIdx++) {
            if (c.contains(top[srcIdx])) {
                int destIdx = srcIdx;
                while (++srcIdx < size) {
                    if (!c.contains(top[srcIdx])) {
                        top[destIdx++] = top[srcIdx];
                    }
                }
                size = destIdx + 1;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        for (int srcIdx = 0; srcIdx < size; srcIdx++) {
            if (!c.contains(top[srcIdx])) {
                int destIdx = srcIdx;
                while (++srcIdx < size) {
                    if (c.contains(top[srcIdx])) {
                        top[destIdx++] = top[srcIdx];
                    }
                }
                size = destIdx + 1;
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        for (int i = 0; i < size; i++) {
            action.accept(top[i]);
        }
    }
}