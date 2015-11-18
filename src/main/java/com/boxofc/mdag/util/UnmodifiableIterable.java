package com.boxofc.mdag.util;

import java.util.Iterator;

public class UnmodifiableIterable<T> implements Iterable<T> {
    private final Iterable<T> delegate;

    public UnmodifiableIterable(Iterable<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<T> it = delegate.iterator();
            
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                return it.next();
            }
        };
    }
}