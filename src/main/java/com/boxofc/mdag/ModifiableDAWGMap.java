package com.boxofc.mdag;

public class ModifiableDAWGMap extends DAWGMap {
    public ModifiableDAWGMap() {
        super(new ModifiableDAWGSet());
    }
    
    ModifiableDAWGMap(ModifiableDAWGSet dawg) {
        super(dawg);
    }
    
    public CompressedDAWGMap compress() {
        return new CompressedDAWGMap(((ModifiableDAWGSet)dawg).compress());
    }
    
    /**
     * This method removes unused letters from the alphabet of this DAWG.<br>
     * Use it before compression if the removal of words was performed (or replacement by {@link #put} operation).
     */
    public void optimizeLetters() {
        ((ModifiableDAWGSet)dawg).optimizeLetters();
    }
}