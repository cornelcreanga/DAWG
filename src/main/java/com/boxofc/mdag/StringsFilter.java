package com.boxofc.mdag;

public interface StringsFilter {
    public Iterable<String> getAllStrings();
    public Iterable<String> getStringsStartingWith(String prefix);
    public Iterable<String> getStringsWithSubstring(String substring);
    public Iterable<String> getStringsEndingWith(String suffix);
}