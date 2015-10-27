package com.boxofc.mdag;

interface DAWGNode {
    public static final int START = 0;
    public static final int END = 1;
    
    public int getId();
    public boolean isAcceptNode();
}