import java.util.List;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * Parser for PascalMaisPresque.
 * 
 * The parser implements a recursive descend mimicking the run of the pushdown automaton: the call stack replacing the automaton stack.
 * 
 * @author Mathieu Sassolas, inspired from earlier versions of the project (exact authors not determined).
 *
 */
public class Parser{
    /**
     * Lexer object for the parsed file.
     */
    private LexicalAnalyzer scanner;
    /**
     * Current symbol at the head of the word to be read. This corresponds to the look-ahead (of length 1).
     */
    private Symbol current;
    /**
     * Option to print only the rule number (false) or the full rule (true).
     */
    private boolean fullRuleDisplay=false;
    /**
     * Width (in characters) of the widest left handside in a production rule.
     */
    private static final int widestNonTerm=14; // <InstListTail>
    /**
     * Width (in characters) of the highest rule number.
     */
    private static final int log10ruleCard=2; // 41 rules

    /**
     * Creates a Parser object for the provided file and initialized the look-ahead.
     * 
     * @param source a FileReader object for the parsed file.
     * @throws IOException in case the lexing fails (syntax error).
     */
    public Parser(FileReader source) throws IOException{
        this.scanner = new LexicalAnalyzer(source);
        this.current = scanner.nextToken();
    }
    
    /* Display of the rules */
    /**
     * Returns a string of several spaces.
     * 
     * @param n the number of spaces.
     * @return a String containing n spaces.
     */
    private static String multispace(int n) {
        String res="";
        for (int i=0;i<n;i++) {
            res+=" ";
        };
        return res;
    }
    
    /**
     * Outputs the rule used in the LL descent.
     * 
     * @param rNum the rule number.
     * @param ruleLhs the left hand-side of the rule as a String.
     * @param ruleRhs the right hand-side of the rule as a String.
     * @param full a boolean specifying whether to write only the rule number (false) or the full rule (true).
     */
    private static void ruleOutput(int rNum, String ruleLhs,String ruleRhs, boolean full) {
        if (full) {
            System.out.println("   ["+rNum+"]"+
                multispace(1+log10ruleCard-String.valueOf(rNum).length())+ // Align left hand-sides regardless of number of digits in rule number
                ruleLhs+multispace(2+widestNonTerm-ruleLhs.length())+ // Align right hand-sides regardless of length of the left hand-side
                "→  "+ruleRhs);
        } else {
            System.out.print(rNum+" ");
        }
    }
    
    /**
     * Outputs the rule used in the LL descent, using the fullRuleDisplay value to set the option of full display or not.
     * 
     * @param rNum the rule number.
     * @param ruleLhs the left hand-side of the rule as a String.
     * @param ruleRhs the right hand-side of the rule as a String.
     */
    private void ruleOutput(int rNum, String ruleLhs,String ruleRhs) {
        ruleOutput(rNum,ruleLhs,ruleRhs,this.fullRuleDisplay);
    }
    
    /**
     * Sets the display option to "Full rules".
     */
    public void displayFullRules() {
        this.fullRuleDisplay=true;
    }
    
    /**
     * Sets the display option to "Rule numbers only".
     */
    public void displayRuleNumbers() {
        this.fullRuleDisplay=false;
    }

    /* Matching of terminals */
    /**
     * Advances in the input stream, consuming one token.
     * 
     * @throws IOException in case the lexing fails (syntax error).
     */
    private void consume() throws IOException{
        current = scanner.nextToken();
    }

    /**
     * Matches a (terminal) token from the head of the word.
     * 
     * @param token then LexicalUnit (terminal) to be matched.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the matching fails (syntax error): the next tolen is not the one to be matched.
     * @return a ParseTree made of a single leaf (the matched terminal).
     */
    private ParseTree match(LexicalUnit token) throws IOException, ParseException{
        if(!current.getType().equals(token)){
            // There is a parsing error
            throw new ParseException(current, Arrays.asList(token));
        }
        else {
            Symbol cur = current;
            consume();
            return new ParseTree(cur);
        }
    }
    
    /* Applying grammar rules */
    /**
     * Parses the file.
     * 
     * @return a ParseTree containing the parsed file structured by the grammar rules.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    public ParseTree parse() throws IOException, ParseException{
        // Program is the initial symbol of the grammar
        ParseTree pt = program();
        if (!this.fullRuleDisplay) {System.out.println();} // New line at the end of list of rules
        return pt;
    }
    
    /**
     * Treats a &lt;Program&gt; at the top of the stack.
     * 
     * Tries to apply rule [1]&nbsp;&lt;Program&gt;&nbsp;&rarr;&nbsp;<code>begin</code> &lt;Code&gt; <code>end</code>
     * 
     * @return a ParseTree with a &lt;Program&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree program() throws IOException, ParseException{
        // [1] <Program>  ->  begin <Code> end
        ruleOutput(1,"<Program>","begin <Code> end");
        return new ParseTree(NonTerminal.Program, Arrays.asList(
            match(LexicalUnit.BEG),
            code(),
            match(LexicalUnit.END)
        ));
    }
    
    /**
     * Treats a &lt;Code&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[2]&nbsp;&lt;Code&gt;&nbsp;&rarr;&nbsp;&lt;InstList&gt;</li>
     *   <li>[3]&nbsp;&lt;Code&gt;&nbsp;&rarr;&nbsp;&epsilon;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Code&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree code() throws IOException, ParseException{
        switch(current.getType()) {
            // [2] <Code>  ->  <InstList>
            case BEG:
            case IF:
            case WHILE:
            case PRINT:
            case READ:
            case VARNAME:
                ruleOutput(2,"<Code>","<InstList>");
                return new ParseTree(NonTerminal.Code, Arrays.asList(
                    instructionList()
                ));
            // [3] <Code>  ->  EPSILON 
            case END:
                ruleOutput(3,"<Code>","ɛ");
                return new ParseTree(NonTerminal.Code, Arrays.asList(
                    new ParseTree(LexicalUnit.EPSILON)
                ));
            default:
                throw new ParseException(current,NonTerminal.Code,Arrays.asList(
                    LexicalUnit.BEG,
                    LexicalUnit.IF,
                    LexicalUnit.WHILE,
                    LexicalUnit.PRINT,
                    LexicalUnit.READ,
                    LexicalUnit.VARNAME,
                    LexicalUnit.END
                ));
        }
    }
    
    /**
     * Treats a &lt;InstList&gt; at the top of the stack.
     * 
     * Tries to apply rule [4]&nbsp;&lt;InstList&gt;&nbsp;&rarr;&nbsp;&lt;Instruction&gt;&lt;InstListTail&gt;
     * 
     * @return a ParseTree with a &lt;InstList&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree instructionList() throws IOException, ParseException{
        // [4] <InstList>  ->  <Instruction><InstListTail>
        ruleOutput(4,"<InstList>","<Instruction> <InstListTail>");
        return new ParseTree(NonTerminal.InstList, Arrays.asList(instruction(),instructionListTail()));
    }

    /**
     * Treats a &lt;InstListTail&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[5]&nbsp;&lt;InstListTail&gt;&nbsp;&rarr;&nbsp;<code>...</code>&lt;Instruction&gt;&lt;InstListTail&gt;</li>
     *   <li>[6]&nbsp;&lt;InstListTail&gt;&nbsp;&rarr;&nbsp;&epsilon;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;InstListTail&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree instructionListTail() throws IOException, ParseException{
        switch(current.getType()) {
            // [5] <InstListTail>  ->  ...<Instruction><InstListTail>
            case DOTS:
                ruleOutput(5,"<InstListTail>","... <Instruction> <InstListTail>");
                return new ParseTree(NonTerminal.InstListTail,Arrays.asList(
                    match(LexicalUnit.DOTS),
                    instruction(),
                    instructionListTail()
                ));
            // [6] <InstListTail>  ->  EPSILON
            case END:
                ruleOutput(6,"<InstListTail>","ɛ");
                return new ParseTree(NonTerminal.InstListTail,Arrays.asList(
                    new ParseTree(LexicalUnit.EPSILON)
                ));
            default:
                throw new ParseException(current,NonTerminal.InstListTail,Arrays.asList(
                    LexicalUnit.DOTS,
                    LexicalUnit.END
                ));
        }
    }

    /**
     * Treats a &lt;Instruction&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[7]&nbsp;&lt;Instruction&gt;&nbsp;&rarr;&nbsp;&lt;Assign&gt;</li>
     *   <li>[8]&nbsp;&lt;Instruction&gt;&nbsp;&rarr;&nbsp;&lt;If&gt;</li>
     *   <li>[9]&nbsp;&lt;Instruction&gt;&nbsp;&rarr;&nbsp;&lt;While&gt;</li>
     *   <li>[10]&nbsp;&lt;Instruction&gt;&nbsp;&rarr;&nbsp;&lt;Print&gt;</li>
     *   <li>[11]&nbsp;&lt;Instruction&gt;&nbsp;&rarr;&nbsp;&lt;Read&gt;</li>
     *   <li>[12]&nbsp;&lt;Instruction&gt;&nbsp;&rarr;&nbsp;<code>begin</code> &lt;InstList&gt; <code>end</code></li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Instruction&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree instruction() throws IOException, ParseException{
        switch(current.getType()) {
            // [7] <Instruction>  ->  <Assign>
            case VARNAME:
                ruleOutput(7,"<Instruction>","<Assign>");
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    assignExpr()
                ));
            // [8] <Instruction>  ->  <If>
            case IF:
                ruleOutput(8,"<Instruction>","<If>");
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    ifExpr()
                ));
            // [9] <Instruction>  ->  <While>
            case WHILE:
                ruleOutput(9,"<Instruction>","<While>");
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    whileExpr()
                ));
            // [10] <Instruction>  ->  <Print>
            case PRINT:
                ruleOutput(10,"<Instruction>","<Print>");
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    printExpr()
                ));
            // [11] <Instruction>  ->  <Read>
            case READ:
                ruleOutput(11,"<Instruction>","<Read>");
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    readExpr()
                ));
            // [12] <Instruction>  ->  begin <InstList> end
            case BEG:
                ruleOutput(12,"<Instruction>","begin <InstList> end");
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    match(LexicalUnit.BEG),
                    instructionList(),
                    match(LexicalUnit.END)
                ));
            default:
                throw new ParseException(current,NonTerminal.Instruction,Arrays.asList(
                    LexicalUnit.VARNAME,
                    LexicalUnit.IF,
                    LexicalUnit.WHILE,
                    LexicalUnit.PRINT,
                    LexicalUnit.READ,
                    LexicalUnit.BEG
                ));
        }
    }
    
    /**
     * Treats a &lt;Assign&gt; at the top of the stack.
     * 
     * Tries to apply rule [13]&nbsp;&lt;Assign&gt;&nbsp;&rarr;&nbsp;[Varname]<code>:=</code>&lt;ExprArith&gt;
     * 
     * @return a ParseTree with a &lt;Assign&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree assignExpr() throws IOException, ParseException{
        // [13] <Assign>  ->  [Varname] := <ExprArith>
        ruleOutput(13,"<Assign>","[Varname] := <ExprArith>");
        return new ParseTree(NonTerminal.Assign, Arrays.asList(
            match(LexicalUnit.VARNAME),
            match(LexicalUnit.ASSIGN),
            exprArith()
        ));
    }
    
    /**
     * Treats a &lt;ExprArith&gt; at the top of the stack.
     * 
     * Tries to apply rule [14]&nbsp;&lt;ExprArith&gt;&nbsp;&rarr;&nbsp;&lt;Prod&gt;&lt;ExprArith'&gt;
     * 
     * @return a ParseTree with a &lt;ExprArith&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree exprArith() throws IOException, ParseException{
        // [14] <ExprArith>  ->  <Prod> <ExprArith'>
        ruleOutput(14,"<ExprArith>","<Prod> <ExprArith'>");
        return new ParseTree(NonTerminal.ExprArith, Arrays.asList(
            prod(),
            exprArithPrime()
        ));
    }

    /**
     * Treats a &lt;ExprArith'&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[15]&nbsp;&lt;ExprArith'&gt;&nbsp;&rarr;&nbsp;<code>+</code>&lt;Prod&gt;&lt;ExprArith'&gt;</li>
     *   <li>[16]&nbsp;&lt;ExprArith'&gt;&nbsp;&rarr;&nbsp;<code>-</code>&lt;Prod&gt;&lt;ExprArith'&gt;</li>
     *   <li>[17]&nbsp;&lt;ExprArith'&gt;&nbsp;&rarr;&nbsp;&epsilon;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;ExprArith'&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree exprArithPrime() throws IOException, ParseException{
        switch (current.getType()) {
            // [15] <ExprArith'>  ->  + <Prod> <ExprArith'>
            case PLUS:
                ruleOutput(15,"<ExprArith'>","+ <Prod> <ExprArith'>");
                return new ParseTree(NonTerminal.ExprArithPrime, Arrays.asList(
                    match(LexicalUnit.PLUS),
                    prod(),
                    exprArithPrime()
                ));
            // [16] <ExprArith'>  ->  - <Prod> <ExprArith'>
            case MINUS:
                ruleOutput(16,"<ExprArith'>","- <Prod> <ExprArith'>");
                return new ParseTree(NonTerminal.ExprArithPrime, Arrays.asList(
                    match(LexicalUnit.MINUS),
                    prod(),
                    exprArithPrime()
                ));
            // [17] <ExprArith'>  ->  EPSILON
            case END:
            case THEN:
            case ELSE:
            case DO:
            case DOTS:
            case RPAREN:
            case RBRACK:
            case AND:
            case OR:
            case EQUAL:
            case SMALLER:
                ruleOutput(17,"<ExprArith'>","ɛ");
                return new ParseTree(NonTerminal.ExprArithPrime, Arrays.asList(
                    new ParseTree(LexicalUnit.EPSILON)
                ));
            default:
                throw new ParseException(current,NonTerminal.ExprArithPrime,Arrays.asList(
                    LexicalUnit.PLUS,
                    LexicalUnit.MINUS,
                    LexicalUnit.END,
                    LexicalUnit.THEN,
                    LexicalUnit.ELSE,
                    LexicalUnit.DO,
                    LexicalUnit.DOTS,
                    LexicalUnit.RPAREN,
                    LexicalUnit.RBRACK,
                    LexicalUnit.AND,
                    LexicalUnit.OR,
                    LexicalUnit.EQUAL,
                    LexicalUnit.SMALLER
                ));
        }
    }
    
    /**
     * Treats a &lt;Prod&gt; at the top of the stack.
     * 
     * Tries to apply rule [18]&nbsp;&lt;Prod&gt;&nbsp;&rarr;&nbsp;&lt;Atom&gt;&lt;Prod'&gt;
     * 
     * @return a ParseTree with a &lt;Prod&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree prod() throws IOException, ParseException{
        // [18] <Prod>  ->  <Atom> <Prod'>
        ruleOutput(18,"<Prod'>","<Atom> <Prod'>");
        return new ParseTree(NonTerminal.Prod, Arrays.asList(
            atom(),
            prodPrime()
        ));
    }

    /**
     * Treats a &lt;Prod'&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[19]&nbsp;&lt;Prod'&gt;&nbsp;&rarr;&nbsp;<code>*</code>&lt;Atom&gt;&lt;Prod'&gt;</li>
     *   <li>[20]&nbsp;&lt;Prod'&gt;&nbsp;&rarr;&nbsp;<code>/</code>&lt;Atom&gt;&lt;Prod'&gt;</li>
     *   <li>[21]&nbsp;&lt;Prod'&gt;&nbsp;&rarr;&nbsp;&epsilon;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Prod'&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree prodPrime() throws IOException, ParseException{
        switch (current.getType()) {
            // [19] <Prod'>  ->  * <Atom> <Prod'>
            case TIMES:
                ruleOutput(19,"<Prod'>","* <Atom> <Prod'>");
                return new ParseTree(NonTerminal.ProdPrime, Arrays.asList(
                    match(LexicalUnit.TIMES),
                    atom(),
                    prodPrime()
                ));
            // [20] <Prod'>  ->  / <Atom> <Prod>
            case DIVIDE:
                ruleOutput(20,"<Prod'>","/ <Atom> <Prod>");
                return new ParseTree(NonTerminal.ProdPrime, Arrays.asList(
                    match(LexicalUnit.DIVIDE),
                    atom(),
                    prodPrime()
                ));
            // [21] <Prod'>  ->  EPSILON
            case END:
            case THEN:
            case ELSE:
            case DO:
            case DOTS:
            case PLUS:
            case MINUS:
            case RPAREN:
            case RBRACK:
            case AND:
            case OR:
            case EQUAL:
            case SMALLER:
                ruleOutput(21,"<Prod'>","ɛ");
                return new ParseTree(NonTerminal.ProdPrime, Arrays.asList(
                    new ParseTree(LexicalUnit.EPSILON)
                ));
            default:
                throw new ParseException(current,NonTerminal.ProdPrime,Arrays.asList(
                    LexicalUnit.PLUS,
                    LexicalUnit.MINUS,
                    LexicalUnit.TIMES,
                    LexicalUnit.DIVIDE,
                    LexicalUnit.END,
                    LexicalUnit.THEN,
                    LexicalUnit.ELSE,
                    LexicalUnit.DO,
                    LexicalUnit.DOTS,
                    LexicalUnit.RPAREN,
                    LexicalUnit.RBRACK,
                    LexicalUnit.AND,
                    LexicalUnit.OR,
                    LexicalUnit.EQUAL,
                    LexicalUnit.SMALLER
                ));
        }
    }

    /**
     * Treats a &lt;Atom&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[22]&nbsp;&lt;Atom&gt;&nbsp;&rarr;&nbsp;<code>-</code>&lt;Atom&gt;</li>
     *   <li>[23]&nbsp;&lt;Atom&gt;&nbsp;&rarr;&nbsp;<code>(</code>&lt;ExprArith&gt;<code>)</code></li>
     *   <li>[24]&nbsp;&lt;Atom&gt;&nbsp;&rarr;&nbsp;[VarName]</li>
     *   <li>[25]&nbsp;&lt;Atom&gt;&nbsp;&rarr;&nbsp;[Number]</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Atom&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree atom() throws IOException, ParseException{
        switch (current.getType()) {
            // [22] <Atom>  ->  - <Atom>
            case MINUS:
                ruleOutput(22,"<Atom>","- <Atom>");
                return new ParseTree(NonTerminal.Atom, Arrays.asList(
                    match(LexicalUnit.MINUS),
                    atom()
                ));
            // [23] <Atom>  ->  (<ExprArith>)
            case LPAREN:
                ruleOutput(23,"<Atom>","(<ExprArith>)");
                return new ParseTree(NonTerminal.Atom, Arrays.asList(
                    match(LexicalUnit.LPAREN),
                    exprArith(),
                    match(LexicalUnit.RPAREN)
                ));
            // [24] <Atom>  ->  [VarName]
            case VARNAME:
                ruleOutput(24,"<Atom>","[VarName]");
                return new ParseTree(NonTerminal.Atom, Arrays.asList(
                    match(LexicalUnit.VARNAME)
                ));
            // [25] <Atom>  ->  [Number]
            case NUMBER:
                ruleOutput(25,"<Atom>","[Number]");
                return new ParseTree(NonTerminal.Atom, Arrays.asList(
                    match(LexicalUnit.NUMBER)
                ));
            default:
                throw new ParseException(current,NonTerminal.Atom,Arrays.asList(
                    LexicalUnit.MINUS,
                    LexicalUnit.LPAREN,
                    LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER
                ));
        }
    }
    
    /**
     * Treats a &lt;If&gt; at the top of the stack.
     * 
     * Tries to apply rule [26]&nbsp;&lt;If&gt;&nbsp;&rarr;&nbsp;<code>if</code> &lt;Cond&gt; <code>then</code> &lt;Instruction&gt; <code>else</code>&lt;IfTail&gt;
     * 
     * @return a ParseTree with a &lt;If&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree ifExpr() throws IOException, ParseException{
        // [26] <If>  -> if <Cond> then <Instruction> else <IfTail>
        ruleOutput(26,"<If>","if <Cond> then <Instruction> else <IfTail>");
        return new ParseTree(NonTerminal.If, Arrays.asList(
            match(LexicalUnit.IF),
            cond(),
            match(LexicalUnit.THEN),
            instruction(),
            match(LexicalUnit.ELSE),
            ifTail()
        ));
    }

    /**
     * Treats a &lt;IfTail&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[27]&nbsp;&lt;IfTail&gt;&nbsp;&rarr;&nbsp;&lt;Instruction&gt;</li>
     *   <li>[28]&nbsp;&lt;IfTail&gt;&nbsp;&rarr;&nbsp;&epsilon;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;IfTail&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree ifTail() throws IOException, ParseException{
        switch (current.getType()) {
            // [27] <IfTail>  ->  <Instruction>
            case BEG:
            case IF:
            case WHILE:
            case PRINT:
            case READ:
            case VARNAME:
                ruleOutput(27,"<IfTail>","<Instruction>");
                return new ParseTree(NonTerminal.IfTail, Arrays.asList(
                    instruction()
                ));
            // [28] <IfTail>  ->  EPSILON
            case END:
            case DOTS:
                ruleOutput(28,"<IfTail>","ɛ");
                return new ParseTree(NonTerminal.IfTail, Arrays.asList(
                    new ParseTree(LexicalUnit.EPSILON)
                ));
            default:
                throw new ParseException(current,NonTerminal.IfTail,Arrays.asList(
                    LexicalUnit.BEG,
                    LexicalUnit.END,
                    LexicalUnit.IF,
                    LexicalUnit.WHILE,
                    LexicalUnit.PRINT,
                    LexicalUnit.READ,
                    LexicalUnit.DOTS,
                    LexicalUnit.VARNAME
                ));
        }
    }
    
    /**
     * Treats a &lt;Cond&gt; at the top of the stack.
     * 
     * Tries to apply rule [29]&nbsp;&lt;Cond&gt;&nbsp;&rarr;&nbsp;&lt;Conj&gt;&lt;Cond'&gt;
     * 
     * @return a ParseTree with a &lt;Cond&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree cond() throws IOException, ParseException{
        // [29] <Cond>  -> <Conj> <Cond'>
        ruleOutput(29,"<Cond>","<Conj> <Cond'>");
        return new ParseTree(NonTerminal.Cond, Arrays.asList(
            conj(),
            condPrime()
        ));
    }
    
    /**
     * Treats a &lt;Cond'&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[30]&nbsp;&lt;Cond'&gt;&nbsp;&rarr;&nbsp;<code>or</code> &lt;Conj&gt;&lt;Cond'&gt;</li>
     *   <li>[31]&nbsp;&lt;Cond'&gt;&nbsp;&rarr;&nbsp;&epsilon;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Cond'&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree condPrime() throws IOException, ParseException{
        switch (current.getType()) {
            // [30] <Cond'>  ->  or <Conj> <Cond'>
            case OR:
                ruleOutput(30,"<Cond'>","or <Conj> <Cond'>");
                return new ParseTree(NonTerminal.CondPrime, Arrays.asList(
                    match(LexicalUnit.OR),
                    conj(),
                    condPrime()
                ));
            // [31] <Cond'>  ->  EPSILON
            case THEN:
            case DO:
            case RBRACK:
                ruleOutput(31,"<Cond'>","ɛ");
                return new ParseTree(NonTerminal.CondPrime, Arrays.asList(
                    new ParseTree(LexicalUnit.EPSILON)
                ));
            default:
                throw new ParseException(current,NonTerminal.CondPrime,Arrays.asList(
                    LexicalUnit.OR,
                    LexicalUnit.THEN,
                    LexicalUnit.DO,
                    LexicalUnit.RBRACK
                ));
        }
    }
    
    /**
     * Treats a &lt;Conj&gt; at the top of the stack.
     * 
     * Tries to apply rule [32]&nbsp;&lt;Conj&gt;&nbsp;&rarr;&nbsp;&lt;SimpleCond&gt;&lt;Conj'&gt;
     * 
     * @return a ParseTree with a &lt;Conj&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree conj() throws IOException, ParseException{
        // [32] <Conj>  -> <SimpleCond> <Conj'>
        ruleOutput(32,"<Conj>","<SimpleCond> <Conj'>");
        return new ParseTree(NonTerminal.Conj, Arrays.asList(
            simpleCond(),
            conjPrime()
        ));
    }
        
    /**
     * Treats a &lt;Conj'&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[33]&nbsp;&lt;Conj'&gt;&nbsp;&rarr;&nbsp;<code>and</code> &lt;SimpleCond&gt;&lt;Conj'&gt;</li>
     *   <li>[34]&nbsp;&lt;Conj'&gt;&nbsp;&rarr;&nbsp;&epsilon;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Conj'&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree conjPrime() throws IOException, ParseException{
        switch (current.getType()) {
            // [33] <Conj'>  ->  and <SimpleCond> <Conj'>
            case AND:
                ruleOutput(33,"<Conj'>","and <SimpleCond> <Conj'>");
                return new ParseTree(NonTerminal.ConjPrime, Arrays.asList(
                    match(LexicalUnit.AND),
                    simpleCond(),
                    conjPrime()
                ));
            // [34] <Conj'>  ->  EPSILON
            case OR:
            case THEN:
            case DO:
            case RBRACK:
                ruleOutput(34,"<Conj'>","ɛ");
                return new ParseTree(NonTerminal.ConjPrime, Arrays.asList(
                    new ParseTree(LexicalUnit.EPSILON)
                ));
            default:
                throw new ParseException(current,NonTerminal.ConjPrime,Arrays.asList(
                    LexicalUnit.AND,
                    LexicalUnit.OR,
                    LexicalUnit.THEN,
                    LexicalUnit.DO,
                    LexicalUnit.RBRACK
                ));
        }
    }
    
    /**
     * Treats a &lt;SimpleCond&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[35]&nbsp;&lt;SimpleCond&gt;&nbsp;&rarr;&nbsp;<code>{</code>&lt;Cond&gt;<code>}</code></li>
     *   <li>[36]&nbsp;&lt;SimpleCond&gt;&nbsp;&rarr;&nbsp;&lt;ExprArith&gt;&lt;Comp&gt;&lt;ExprArith&gt;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;SimpleCond&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree simpleCond() throws IOException, ParseException{
        switch (current.getType()) {
            // [35] <SimpleCond>  ->  {<Cond>}
            case LBRACK:
                ruleOutput(35,"<SimpleCond>","{<Cond>}");
                return new ParseTree(NonTerminal.SimpleCond, Arrays.asList(
                    match(LexicalUnit.LBRACK),
                    cond(),
                    match(LexicalUnit.RBRACK)
                ));
            // [36] <SimpleCond>  ->  <ExprArith> <Comp> <ExprArith>
            case MINUS:
            case LPAREN:
            case VARNAME:
            case NUMBER:
                ruleOutput(36,"<SimpleCond>","<ExprArith> <Comp> <ExprArith>");
                return new ParseTree(NonTerminal.SimpleCond, Arrays.asList(
                    exprArith(),
                    compOp(),
                    exprArith()
                ));
            default:
                throw new ParseException(current,NonTerminal.SimpleCond,Arrays.asList(
                    LexicalUnit.LBRACK,
                    LexicalUnit.MINUS,
                    LexicalUnit.LPAREN,
                    LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER
                ));
        }
    }
                
    /**
     * Treats a &lt;Comp&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[37]&nbsp;&lt;Comp&gt;&nbsp;&rarr;&nbsp;<code>=</code></li>
     *   <li>[38]&nbsp;&lt;Comp&gt;&nbsp;&rarr;&nbsp;<code>&lt;</code></li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Comp&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree compOp() throws IOException, ParseException{
        switch (current.getType()) {
            // [37] <Comp>  ->  =
            case EQUAL:
                ruleOutput(37,"<Comp>","=");
                return new ParseTree(NonTerminal.Comp, Arrays.asList(
                    match(LexicalUnit.EQUAL)
                ));
            // [38] <Comp>  ->  <
            case SMALLER:
                ruleOutput(38,"<Comp>","<");
                return new ParseTree(NonTerminal.Comp, Arrays.asList(
                    match(LexicalUnit.SMALLER)
                ));
            default:
                throw new ParseException(current,NonTerminal.Comp,Arrays.asList(
                    LexicalUnit.EQUAL,
                    LexicalUnit.SMALLER
                ));
        }
    }
    
    /**
     * Treats a &lt;While&gt; at the top of the stack.
     * 
     * Tries to apply rule [39]&nbsp;&lt;While&gt;&nbsp;&rarr;&nbsp;<code>while</code>&lt;Cond&gt; <code>do</code> &lt;Instruction&gt;
     * 
     * @return a ParseTree with a &lt;While&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree whileExpr() throws IOException, ParseException{
        // [39] <While>  ->  while <Cond> do <Instruction>
        ruleOutput(39,"<While>","while <Cond> do <Instruction>");
        return new ParseTree(NonTerminal.While, Arrays.asList(
            match(LexicalUnit.WHILE),
            cond(),
            match(LexicalUnit.DO),
            instruction()
        ));
    }
    
    /**
     * Treats a &lt;Print&gt; at the top of the stack.
     * 
     * Tries to apply rule [40]&nbsp;&lt;Print&gt;&nbsp;&rarr;&nbsp;<code>print(</code>[Varname]<code>)</code>
     * 
     * @return a ParseTree with a &lt;Print&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree printExpr() throws IOException, ParseException{
        // [40] <Print>  ->  print([VarName])
        ruleOutput(40,"<Print>","print([VarName])");
        return new ParseTree(NonTerminal.Print, Arrays.asList(
            match(LexicalUnit.PRINT),
            match(LexicalUnit.LPAREN),
            match(LexicalUnit.VARNAME),
            match(LexicalUnit.RPAREN)
        ));
    }
    
    /**
     * Treats a &lt;Read&gt; at the top of the stack.
     * 
     * Tries to apply rule [41]&nbsp;&lt;Read&gt;&nbsp;&rarr;&nbsp;<code>read(</code>[Varname]<code>)</code>
     * 
     * @return a ParseTree with a &lt;Read&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree readExpr() throws IOException, ParseException{
        // [41] <Read>  ->  read([VarName])
        ruleOutput(41,"<Read>","read([VarName])");
        return new ParseTree(NonTerminal.Read, Arrays.asList(
            match(LexicalUnit.READ),
            match(LexicalUnit.LPAREN),
            match(LexicalUnit.VARNAME),
            match(LexicalUnit.RPAREN)
        ));
    }

/*
    private ParseTree nonterminal() throws IOException, ParseException{
        return new ParseTree(NonTerminal.TODO); // TODO
    }
*/
}
