package edu.lmu.cs.xlg.squid;

import java.util.ArrayList;
import java.util.List;

/**
 * A Squid string constant.  Each literal has a name, like s1, s2,
 * etc., and a list of codepoints for each of the characters in
 * the string.
 */
public class StringConstant {
    private String name;
    public List<Integer> values;

    /**
     * Constructs a string constant given a list of values.
     */
    public StringConstant(String name, List<Integer> values) {
        this.name = name;
        this.values = values;
    }

    /**
     * Constructs a string constant given a string.  The string
     * constant is made by extracting each of the characters
     * from the string and storing their codepoints in the
     * string constant's value list.
     */
    @SuppressWarnings("serial")
    public StringConstant(String name, final String text) {
        this(name, new ArrayList<Integer>() {{
            for (int i = 0, n = text.length(); i < n; i++) {
                add(new Integer(text.charAt(i)));
            }
        }});
    }

    /**
     * Returns the name of this literal (not the character values).
     */
    public String toString() {
        return name;
    }

    /**
     * Returns a comma-separated string of the character codepoints.
     */
    public String details() {
        String listView = values.toString();

        // HACK: Strip off the leading and trailing square brackets.
        return listView.substring(1, listView.length() - 1);
    }
}
