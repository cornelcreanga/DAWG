package com.boxofc.mdag;

public class ModifiableDAWGMultiValuedMap extends DAWGMapOfStringSets {
    public ModifiableDAWGMultiValuedMap() {
        super(new ModifiableDAWGSet());
    }
    
    public ModifiableDAWGMultiValuedMap(boolean withIncomingTransitions) {
        super(new ModifiableDAWGSet(withIncomingTransitions));
    }
    
    ModifiableDAWGMultiValuedMap(ModifiableDAWGSet dawg) {
        super(dawg);
    }
    
    public CompressedDAWGMultiValuedMap compress() {
        return new CompressedDAWGMultiValuedMap(((ModifiableDAWGSet)dawg).compress());
    }
}