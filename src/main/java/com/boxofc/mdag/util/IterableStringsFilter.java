package com.boxofc.mdag.util;

import com.boxofc.mdag.StringsFilter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class IterableStringsFilter implements StringsFilter {
    private final Iterable<String> delegate;

    public IterableStringsFilter(Iterable<String> delegate) {
        this.delegate = delegate;
    }

    public IterableStringsFilter(String... delegate) {
        this.delegate = Arrays.asList(delegate);
    }

    @Override
    public Iterable<String> getAllStrings() {
        return delegate;
    }

    private Iterable<String> getStringsByFilter(Predicate<String> check) {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new LookaheadIterator<String>() {
                    private final Iterator<String> it = delegate.iterator();

                    @Override
                    public String nextElement() {
                        while (it.hasNext()) {
                            String ret = it.next();
                            if (ret != null && check.test(ret))
                                return ret;
                        }
                        throw new NoSuchElementException();
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }
        };
    }

    @Override
    public Iterable<String> getStringsStartingWith(String prefix) {
        if (prefix == null)
            return delegate;
        return getStringsByFilter(ret -> ret.startsWith(prefix));
    }

    @Override
    public Iterable<String> getStringsWithSubstring(String substring) {
        if (substring == null)
            return delegate;
        return getStringsByFilter(ret -> ret.contains(substring));
    }

    @Override
    public Iterable<String> getStringsEndingWith(String suffix) {
        if (suffix == null)
            return delegate;
        return getStringsByFilter(ret -> ret.endsWith(suffix));
    }
}