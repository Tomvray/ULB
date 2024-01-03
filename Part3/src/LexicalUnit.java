/**
 * A terminal symbol, a.k.a. a letter in the grammar.
 */
public enum LexicalUnit{
    /** [VarName] */
    VARNAME,
    /** [Number] */
    NUMBER,
    /** <code>begin</code> */
    BEG,
    /** <code>end</code> */
    END,
    /** <code>...</code> */
    DOTS,
    /** <code>:=</code> */
    ASSIGN,
    /** <code>(</code> */
    LPAREN,
    /** <code>)</code> */
    RPAREN,
    /** <code>-</code> */
    MINUS,
    /** <code>+</code> */
    PLUS,
    /** <code>*</code> */
    TIMES,
    /** <code>/</code> */
    DIVIDE,
    /** <code>if</code> */
    IF,
    /** <code>then</code> */
    THEN,
    /** <code>else</code> */
    ELSE,
    /** <code>and</code> */
    AND,
    /** <code>or</code> */
    OR,
    /** <code>{</code> */
    LBRACK,
    /** <code>}</code> */
    RBRACK,
    /** <code>=</code> */
    EQUAL,
    /** <code>&lt;</code> */
    SMALLER,
    /** <code>while</code> */
    WHILE,
    /** <code>do</code> */
    DO,
    /** <code>print</code> */
    PRINT,
    /** <code>read</code> */
    READ,
    /** End Of Stream */
    EOS, // End of stream
    /** &epsilon; */
    EPSILON; // Epsilon: not actually scanned but detected by the parser
    
    /**
     * Returns the representation the terminal.
     * 
     * @return a String containing the terminal type (word or abstract expression).
     */
     @Override
    public String toString() {
        String n=this.name();
        switch (this) {
            case VARNAME:
                n="[VarName]";
                break;
            case NUMBER:
                n="[Number]";
                break;
            case BEG:
                n="begin";
                break;
            case END:
                n="end";
                break;
            case DOTS:
                n="..";
                break;
            case ASSIGN:
                n=":=";
                break;
            case LPAREN:
                n="(";
                break;
            case RPAREN:
                n=")";
                break;
            case MINUS:
                n="-";
                break;
            case PLUS:
                n="+";
                break;
            case TIMES:
                n="*";
                break;
            case DIVIDE:
                n="/";
                break;
            case IF:
                n="if";
                break;
            case THEN:
                n="then";
                break;
            case ELSE:
                n="else";
                break;
            case AND:
                n="and";
                break;
            case OR:
                n="or";
                break;
            case LBRACK:
                n="{";
                break;
            case RBRACK:
                n="}";
                break;
            case EQUAL:
                n="=";
                break;
            case SMALLER:
                n="<";
                break;
            case WHILE:
                n="while";
                break;
            case DO:
                n="do";
                break;
            case PRINT:
                n="print";
                break;
            case READ:
                n="read";
                break;
            case EOS:
                n="EOS";
                break;
            case EPSILON:
                n="/epsilon/";
                break;
        }
        return n;
    }
    
    
    /**
     * Returns the LaTeX code representing the terminal.
     * 
     * @return a String containing the LaTeX code for the terminal.
     */
    public String toTexString() {
        String n=this.name();
        switch (this) {
            case VARNAME:
                n="Var";
                break;
            case NUMBER:
                n="Num";
                break;
            case BEG:
                n="\\texttt{begin}";
                break;
            case END:
                n="\\texttt{end}";
                break;
            case DOTS:
                n="\\texttt{\\dots}";
                break;
            case ASSIGN:
                n="\\texttt{:=}";
                break;
            case LPAREN:
                n="\\texttt{(}";
                break;
            case RPAREN:
                n="\\texttt{)}";
                break;
            case MINUS:
                n="\\texttt{-}";
                break;
            case PLUS:
                n="\\texttt{+}";
                break;
            case TIMES:
                n="\\texttt{*}";
                break;
            case DIVIDE:
                n="\\texttt{/}";
                break;
            case IF:
                n="\\texttt{if}";
                break;
            case THEN:
                n="\\texttt{then}";
                break;
            case ELSE:
                n="\\texttt{else}";
                break;
            case AND:
                n="\\texttt{and}";
                break;
            case OR:
                n="\\texttt{or}";
                break;
            case LBRACK:
                n="\\texttt{\\{}";
                break;
            case RBRACK:
                n="\\texttt{\\}}";
                break;
            case EQUAL:
                n="\\texttt{=}";
                break;
            case SMALLER:
                n="\\texttt{<}";
                break;
            case WHILE:
                n="\\texttt{while}";
                break;
            case DO:
                n="\\texttt{do}";
                break;
            case PRINT:
                n="\\texttt{print}";
                break;
            case READ:
                n="\\texttt{read}";
                break;
            case EOS:
                n="EOS";
                break;
            case EPSILON:
                n="$\\varepsilon$";
                break;
        }
        return n;
    }
}
