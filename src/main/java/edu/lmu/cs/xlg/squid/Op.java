package edu.lmu.cs.xlg.squid;

import java.text.MessageFormat;

/**
 * An operator of a Squid tuple.
 *
 * <p>Each operator is associated with
 * <ul>
 * <li>a message format pattern, so that the containing tuple can be
 * rendered neatly as a string.
 * <li>the index of the argument, if any, that is written to by this
 * operation.
 * </ul>
 */
public enum Op {
    COPY("copy {0}, {1}", 1),
    COPY_FROM_DEREF("copy [{0}], {1}", 1),
    COPY_TO_DEREF("copy {0}, [{1}]", 1),
    COPY_FROM_OFS("copy [{0}+{1}], {2}", 2),
    COPY_TO_OFS("copy {0}, [{1}+{2}]"),
    ADD("add {0}, {1}, {2}", 2),
    SUB("sub {0}, {1}, {2}", 2),
    MUL("mul {0}, {1}, {2}", 2),
    DIV("div {0}, {1}, {2}", 2),
    MOD("mod {0}, {1}, {2}", 2),
    REM("rem {0}, {1}, {2}", 2),
    SHL("shl {0}, {1}, {2}", 2),
    SHR("shr {0}, {1}, {2}", 2),
    SAR("sar {0}, {1}, {2}", 2),
    AND("and {0}, {1}, {2}", 2),
    OR("or {0}, {1}, {2}", 2),
    XOR("xor {0}, {1}, {2}", 2),
    NOT("not {0}, {1}", 1),
    NEG("neg {0}, {1}", 1),
    COMP("comp {0}, {1}", 1),
    ABS("abs {0}, {1}", 1),
    SIN("sin {0}, {1}", 1),
    COS("cos {0}, {1}", 1),
    ATAN("atan {0}, {1}, {2}", 2),
    LN("ln {0}, {1}", 1),
    SQRT("sqrt {0}, {1}", 1),
    INC("inc {0}", 0),
    DEC("dec {0}", 0),
    INC_DEREF("inc *{0}"),
    DEC_DEREF("dec *{0}"),
    LT("less {0}, {1}, {2}", 2),
    LE("less_or_equal {0}, {1}, {2}", 2),
    EQ("equal {0}, {1}, {2}", 2),
    NE("not_equal {0}, {1}, {2}", 2),
    GE("greater_or_equal {0}, {1}, {2}", 2),
    GT("greater {0}, {1}, {2}", 2),
    LABEL("{0}:"),
    JUMP("jump {0}"),
    JZERO("jz {0}, {1}"),
    JNZERO("jnz {0}, {1}"),
    JLT("jl {0}, {1}, {2}"),
    JLE("jle {0}, {1}, {2}"),
    JEQ("je {0}, {1}, {2}"),
    JNE("jne {0}, {1}, {2}"),
    JGE("jge {0}, {1}, {2}"),
    JGT("jg {0}, {1}, {2}"),
    STARTCALL("startcall {0}"),
    PARAM("param {0}"),
    CALLP("call {0}, {1}"),
    CALLF("call {0}, {1}, {2}", 2),
    RETP("ret"),
    RETF("ret {0}"),
    PRINT("print {0}"),
    INT_TO_STRING("to_string {0}, {1}", 1),
    FLOAT_TO_STRING("to_string {0}, {1}", 1),
    BOOL_TO_STRING("to_string {0}, {1}", 1),
    CHAR_TO_STRING("to_string {0}, {1}", 1),
    ALLOC("alloc {0}, {1}", 1),
    TO_FLOAT("to_float {0}, {1}", 1),
    NULL_CHECK("assert_not_null {0}"),
    ASSERT_POSITIVE("assert_positive {0}"),
    BOUND("assert_in_range {0}, {1}, {2}"),
    NO_OP("nop"),
    EXIT("exit");

    private String pattern;
    private Integer outputArgumentIndex;

    private Op(String pattern) {
        this(pattern, null);
    }

    private Op(String pattern, Integer outputArgumentIndex) {
        this.pattern = pattern;
        this.outputArgumentIndex = outputArgumentIndex;
    }

    /**
     * Returns the string produced by formatting the operator's pattern
     * with the given arguments.  Also clean up "+-" substrings; they
     * arise when adding negative offsets.
     */
    public String format(Object... args) {
        return (this != LABEL ? "\t" : "") +
            MessageFormat.format(pattern, args).replaceAll("\\+-", "-");
    }

    /**
     * Returns the index of the output argument.  For example, the
     * ADD operator is for tuples of the form (ADD, x, y, z) in which
     * z is getting written to, so this method would return 2.
     * For tuples without output arguments, such as EXIT, this method
     * returns null.
     */
    public Integer getOutputArgumentIndex() {
        return outputArgumentIndex;
    }
}
