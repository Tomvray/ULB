/**
 * A non-terminal symbol, a.k.a. a variable in the grammar.
 */
public enum NonTerminal {
    /** &lt;Program&gt; */
    Program,
    /** &lt;Code&gt; */
    Code,
    /** &lt;InstList&gt; */
    InstList,
    /** &lt;InstListTail&gt; */
    InstListTail,
    /** &lt;Instruction&gt; */
    Instruction,
    /** &lt;Assign&gt; */
    Assign,
    /** &lt;ExprArith&gt; */
    ExprArith,
    /** &lt;ExprArith'&gt; */
    ExprArithPrime,
    /** &lt;Prod&gt; */
    Prod,
    /** &lt;Prod'&gt; */
    ProdPrime,
    /** &lt;Atom&gt; */
    Atom,
    /** &lt;If&gt; */
    If,
    /** &lt;IfTail&gt; */
    IfTail,
    /** &lt;Cond&gt; */
    Cond,
    /** &lt;Cond'&gt; */
    CondPrime,
    /** &lt;Conj&gt; */
    Conj,
    /** &lt;Conj'&gt; */
    ConjPrime,
    /** &lt;SimpleCond&gt; */
    SimpleCond,
    /** &lt;Comp&gt; */
    Comp,
    /** &lt;While&gt; */
    While,
    /** &lt;Print&gt; */
    Print,
    /** &lt;Read&gt; */
    Read;
    
    /**
     * Returns a string representation of the non-terminal (without the surrounding &lt;&nbsp;&gt;).
     * 
     * @return a String representing the non-terminal.
     */
    @Override
    public String toString() {
        String n=this.name();
        String realName=n;
        if (n.endsWith("Prime")) {
            realName=n.substring(0,n.length()-5)+"'";
        }
        return realName;
    }
    
    /**
     * Returns th LaTeX code to represent the non-terminal.
     * 
     * The non-terminal is in sans-serif font and surrounded by angle brackets.
     * 
     * @return a String representing LaTeX code for the non-terminal.
     */
    public String toTexString() {
        return "\\textsf{$\\langle$"+this.toString()+"$\\rangle$}";
    }
}
