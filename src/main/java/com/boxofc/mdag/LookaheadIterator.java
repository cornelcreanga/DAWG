package com.boxofc.mdag;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class LookaheadIterator<E> implements Iterator<E> {
    private E current;
    private boolean called;
    private NoSuchElementException ex;
    
    @Override
    public boolean hasNext() {
        if (!called) {
            called = true;
            try {
                current = nextElement();
                return true;
            } catch (NoSuchElementException e) {
                ex = e;
                return false;
            }
        }
        return ex == null;
    }

    @Override
    public E next() throws NoSuchElementException {
        if (hasNext()) {
            called = false;
            return current;
        } else
            throw ex;
    }
    
    public abstract E nextElement() throws NoSuchElementException;
}