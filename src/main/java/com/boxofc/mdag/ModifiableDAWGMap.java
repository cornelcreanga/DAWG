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
    
    public void optimizeLetters() {
        ((ModifiableDAWGSet)dawg).optimizeLetters();
    }
}