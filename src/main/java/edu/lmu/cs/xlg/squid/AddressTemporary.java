package edu.lmu.cs.xlg.squid;

/**
 * A temporary that holds an address.  Since the temporary holds an
 * address, it is itself an integer-valued operand.  However, it
 * may reference a floating-point operand or an integer operand.
 */
public class AddressTemporary extends Temporary {
    public boolean referencesFloat;

    public AddressTemporary(String name, boolean referencesFloat) {
        super(name);
        this.referencesFloat = referencesFloat;
    }
}
