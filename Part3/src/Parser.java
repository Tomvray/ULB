import java.util.List;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
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
     * translation of the code into llvm language
     */
    private String entry_str = "";
    /**
     * counter of the number of if condition to have different label names 
     */
    private int if_counter = 0;
    /**
     * counter of the number of while loop to have different label names 
     */
    private int while_counter = 0;
    /**
     * counter of the temporary variable asssignation
     */
    private int v_counter = 0;
    /**
     * boolean that indicates if the print function has been used
     */
    private boolean print = false;
    /**
     * boolean that indicates if the read function has been used
     */
    private boolean read = false;
    /**
     * variable table containing variables that have been assigned before
     */
    List<String> varTable = new ArrayList<>();
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
    private void addVar(String var){
        if (!varTable.contains(var)){
            varTable.add(var);
        }
    }

    private void loadVar(String var)
    {
        if (varTable.contains(var)){
                entry_str+= "%" + v_counter + " = load i32 , i32* %" + var + "\n";
                v_counter++;
        }
        else{
            throw new Error("error: use of undefined value " + var);
        }
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
        return ;
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
     * Parses the file and writes the llvm code on this standard output.
     * 
     * @return a ParseTree containing the parsed file structured by the grammar rules.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    public ParseTree parse() throws IOException, ParseException{
        // Program is the initial symbol of the grammar
        ParseTree pt = program();
        entry_str += "ret i32 0\n}";
        if (print){
            System.out.println("@.strP = private unnamed_addr constant [4 x i8] c\"%d\\0A\\00\", align 1\ndefine void @println(i32 %x) #0 {\n%1 = alloca i32, align 4\nstore i32 %x, i32* %1, align 4\n%2 = load i32, i32* %1, align 4\n%3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)\nret void\n}\ndeclare i32 @printf(i8*, ...) #1");
        }
        if (read){
            System.out.println("@.strR = private unnamed_addr constant [3 x i8] c\"%d\\00\", align 1\ndefine i32 @readInt() #0 {\n%1 = alloca i32, align 4\n%2 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %1)\n%3 = load i32, i32* %1, align 4\nret i32 %3\n}\n\ndeclare i32 @scanf(i8*, ...) #1");
        }
        System.out.println("define i32 @main() {\nentry:\n");
        for (String var : varTable)
        {
            System.out.println("%" + var + "  = alloca i32\n");
        }
        System.out.println(entry_str);
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
                return new ParseTree(NonTerminal.Code, Arrays.asList(
                    instructionList()
                ));
            // [3] <Code>  ->  EPSILON 
            case END:
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
                return new ParseTree(NonTerminal.InstListTail,Arrays.asList(
                    match(LexicalUnit.DOTS),
                    instruction(),
                    instructionListTail()
                ));
            // [6] <InstListTail>  ->  EPSILON
            case END:
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
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    assignExpr()
                ));
            // [8] <Instruction>  ->  <If>
            case IF:
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    ifExpr()
                ));
            // [9] <Instruction>  ->  <While>
            case WHILE:
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    whileExpr()
                ));
            // [10] <Instruction>  ->  <Print>
            case PRINT:
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    printExpr()
                ));
            // [11] <Instruction>  ->  <Read>
            case READ:
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    readExpr()
                ));
            // [12] <Instruction>  ->  begin <InstList> end
            case BEG:
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
     * if the rule is matched, it generates the llvm code corresponding to the assign 
     * it also add the variable to the varTable
     * 
     * @return a ParseTree with a &lt;Assign&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree assignExpr() throws IOException, ParseException{
        // [13] <Assign>  ->  [Varname] := <ExprArith>
        ParseTree var, assign, exp;
        String varString = (String) current.getValue();
        var = match(LexicalUnit.VARNAME);
        assign = match(LexicalUnit.ASSIGN);
        exp = exprArith();
        addVar(varString);
        entry_str += "store i32 %" + (v_counter - 1) +", i32* %"+  varString + "\n";
        return new ParseTree(NonTerminal.Assign, Arrays.asList(
            var,
            assign,
            exp
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
     * It generates the llvm instruction for the addition or the difference and stores them in temporary variable
     * 
     * @return a ParseTree with a &lt;ExprArith'&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree exprArithPrime() throws IOException, ParseException{
        ParseTree res;
        int ante = v_counter - 1;
        switch (current.getType()) {
            // [15] <ExprArith'>  ->  + <Prod> <ExprArith'>
            case PLUS:
                res = new ParseTree(NonTerminal.ExprArithPrime, Arrays.asList(
                    match(LexicalUnit.PLUS),
                    prod(),
                    exprArithPrime()
                ));
                entry_str += "%" + v_counter + " = add i32 %" + ante + ", %" + (v_counter - 1) + "\n";
                v_counter ++;
                return (res);
            // [16] <ExprArith'>  ->  - <Prod> <ExprArith'>
            case MINUS:
                res = new ParseTree(NonTerminal.ExprArithPrime, Arrays.asList(
                    match(LexicalUnit.MINUS),
                    prod(),
                    exprArithPrime()
                ));
                entry_str += "%" + v_counter + " = sub i32 %" + ante + ", %" + (v_counter - 1) + "\n";
                v_counter ++;
                return (res);
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
     * Generates the llvm instruction for the multipkication / division and stores them in temporary variable
     * 
     * @return a ParseTree with a &lt;Prod'&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree prodPrime() throws IOException, ParseException{
        ParseTree res;
        int ante = v_counter - 1;
        switch (current.getType()) {
            // [19] <Prod'>  ->  * <Atom> <Prod'>
            case TIMES:
                res = new ParseTree(NonTerminal.ProdPrime, Arrays.asList(
                    match(LexicalUnit.TIMES),
                    atom(),
                    prodPrime()
                ));
                entry_str += "%" + v_counter + " = mul i32 %" + ante + ", %" + (v_counter - 1) + "\n";
                v_counter ++;
                return (res);
            // [20] <Prod'>  ->  / <Atom> <Prod>
            case DIVIDE:
                res = new ParseTree(NonTerminal.ProdPrime, Arrays.asList(
                    match(LexicalUnit.DIVIDE),
                    atom(),
                    prodPrime()
                ));
                entry_str += "%" + v_counter + " = sdiv i32 %" + ante + ", %" + (v_counter - 1) + "\n";
                v_counter ++;
                return (res);
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
     * Generates the llvm code for atom expression and stores them in temporary variable
     * 
     * @return a ParseTree with a &lt;Atom&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree atom() throws IOException, ParseException{
        List<ParseTree> chdn;
        switch (current.getType()) {
            // [22] <Atom>  ->  - <Atom>
            case MINUS:
                chdn =  Arrays.asList(
                    match(LexicalUnit.MINUS),
                    atom()
                );
                entry_str += "%" +v_counter + " = sub i32 0, %" + (v_counter - 1) + "\n";
                v_counter++;
                return (new ParseTree(NonTerminal.Atom,chdn));
            // [23] <Atom>  ->  (<ExprArith>)
            case LPAREN:
                return new ParseTree(NonTerminal.Atom, Arrays.asList(
                    match(LexicalUnit.LPAREN),
                    exprArith(),
                    match(LexicalUnit.RPAREN)
                ));
            // [24] <Atom>  ->  [VarName]
            case VARNAME:
                loadVar((String) current.getValue());
                return new ParseTree(NonTerminal.Atom, Arrays.asList(
                    match(LexicalUnit.VARNAME)
                ));
            // [25] <Atom>  ->  [Number]
            case NUMBER:
                entry_str += "%" + v_counter + " = add i32 0, " + current.getValue() + "\n";
                v_counter++;
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
     * Generates the llvm instruction for an if statement:
     * first generates the condtion
     * then uses br to go to the iftrue label or the iffalse label
     * finaly writes the iffalse and iftrue code parts.
     * The end of these pârts is sent with br to the endif label
     * 
     * @return a ParseTree with a &lt;If&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree ifExpr() throws IOException, ParseException{
        // [26] <If>  -> if <Cond> then <Instruction> else <IfTail>
        ParseTree ifT, cond, then, instruction, elseT, iftail;
        int label;
        
        ifT = match(LexicalUnit.IF);
        cond = cond();
        label = if_counter;
        entry_str += "br i1 %" + (v_counter - 1) + ", label %iftrue" + label + ", label %iffalse" + label + "\niftrue" + label + ":\n";
        if_counter++;
        then = match(LexicalUnit.THEN);
        instruction = instruction();
        entry_str +="br label %endif" + (label) + "\niffalse" + (label) + ":\n";
        elseT = match(LexicalUnit.ELSE);
        iftail = ifTail();
        entry_str +="br label %endif" + (label) + "\nendif" + (label) + ":\n";
        return new ParseTree(NonTerminal.If, Arrays.asList(
            ifT, cond, then, instruction, elseT, iftail
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
                    return new ParseTree(NonTerminal.IfTail, Arrays.asList(
                    instruction()
                ));
            // [28] <IfTail>  ->  EPSILON
            case END:
            case DOTS:
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
     * generates the llvm boolean result of an or condition
     * 
     * @return a ParseTree with a &lt;Cond'&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree condPrime() throws IOException, ParseException{
        int ante = v_counter - 1;
        ParseTree res;
    
        switch (current.getType()) {
            // [30] <Cond'>  ->  or <Conj> <Cond'>
            case OR:
                res = new ParseTree(NonTerminal.CondPrime, Arrays.asList(
                    match(LexicalUnit.OR),
                    conj(),
                    condPrime()
                ));
                entry_str += "%" + v_counter + "= or i1 %" + ante + ", %" + (v_counter - 1) +"\n";
                v_counter++;
                return (res);
            // [31] <Cond'>  ->  EPSILON
            case THEN:
            case DO:
            case RBRACK:
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
     * generates the llvm boolean result of an and condition
     * @return a ParseTree with a &lt;Conj'&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree conjPrime() throws IOException, ParseException{
        int ante = v_counter - 1;
        ParseTree res;

        switch (current.getType()) {
            // [33] <Conj'>  ->  and <SimpleCond> <Conj'>
            case AND:
                res = new ParseTree(NonTerminal.ConjPrime, Arrays.asList(
                    match(LexicalUnit.AND),
                    simpleCond(),
                    conjPrime()
                ));
                entry_str += "%" + v_counter + "= and i1 %" + ante + ", %" + (v_counter - 1) +"\n";
                v_counter++;
                return (res);
            // [34] <Conj'>  ->  EPSILON
            case OR:
            case THEN:
            case DO:
            case RBRACK:
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
     * generates the llvm instruction for a condition
     * @return a ParseTree with a &lt;SimpleCond&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree simpleCond() throws IOException, ParseException{
        int ante;
        ParseTree exp1, exp2, op;

        switch (current.getType()) {
            // [35] <SimpleCond>  ->  {<Cond>}
            case LBRACK:
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
                exp1 = exprArith();
                ante = v_counter - 1;
                if (current.getType() == LexicalUnit.SMALLER){
                    op = compOp();
                    exp2 = exprArith();
                    entry_str += "%" + v_counter + "= icmp slt i32 %" + ante + ", %" + (v_counter - 1) +"\n";
                    v_counter++;
                    return new ParseTree(NonTerminal.SimpleCond, Arrays.asList(
                        exp1,
                        op,
                        exp2
                    ));
                }
                else if (current.getType() == LexicalUnit.EQUAL){
                    op = compOp();
                    exp2 = exprArith();
                    entry_str += "%" + v_counter + "= icmp eq i32 %" + ante + ", %" + (v_counter - 1) +"\n";
                    v_counter ++;
                    return new ParseTree(NonTerminal.SimpleCond, Arrays.asList(
                        exp1,
                        op,
                        exp2
                    ));
                }
                throw new ParseException(current,NonTerminal.Comp,Arrays.asList(
                    LexicalUnit.EQUAL,
                    LexicalUnit.SMALLER
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
                return new ParseTree(NonTerminal.Comp, Arrays.asList(
                    match(LexicalUnit.EQUAL)
                ));
            // [38] <Comp>  ->  <
            case SMALLER:
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
        ParseTree whileT, cond, doT, instruction;
        int label = while_counter;
    
        // [39] <While>  ->  while <Cond> do <Instruction>
        whileT = match(LexicalUnit.WHILE);
        entry_str += "br label %while.cond" + label + "\nwhile.cond" + label +":\n";
        cond = cond();
        entry_str += "br i1 %" + (v_counter - 1) + ", label %while.body"+ label + ", label %while.end" + label +"\nwhile.body" + label + ":\n";
        while_counter++;
        doT = match(LexicalUnit.DO);
        instruction = instruction();
        entry_str += "br label %while.cond"+ label + "\nwhile.end"+ label+ ":\n";
        return new ParseTree(NonTerminal.While, Arrays.asList(
            whileT,
            cond,
            doT,
            instruction
        ));
    }
    
    /**
     * Treats a &lt;Print&gt; at the top of the stack.
     * 
     * Tries to apply rule [40]&nbsp;&lt;Print&gt;&nbsp;&rarr;&nbsp;<code>print(</code>[Varname]<code>)</code>
     * Writes the llvm code for the read instruction
     * Checks if the variable is in the VarTable, throws an error if not
     * 
     * @return a ParseTree with a &lt;Print&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree printExpr() throws IOException, ParseException{
        ParseTree print, lpar, rpar, var;

        // [40] <Print>  ->  print([VarName])
        this.print = true;
        print = match(LexicalUnit.PRINT);
        lpar = match(LexicalUnit.LPAREN);
        entry_str += "%" + v_counter + " = load i32, i32* %" + current.getValue() + " \n";
        entry_str += "call void @println(i32 %" + v_counter + ")\n";
        v_counter++;
        var = match(LexicalUnit.VARNAME);
        rpar = match(LexicalUnit.RPAREN);      
        return new ParseTree(NonTerminal.Print, Arrays.asList(
            print,
            lpar,
            var,
            rpar
        ));
    }
    
    /**
     * Treats a &lt;Read&gt; at the top of the stack.
     * 
     * Tries to apply rule [41]&nbsp;&lt;Read&gt;&nbsp;&rarr;&nbsp;<code>read(</code>[Varname]<code>)</code>
     * Writes the llvm code for the read instruction
     * add the variable to the VarTable
     * 
     * @return a ParseTree with a &lt;Read&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree readExpr() throws IOException, ParseException{
        // [41] <Read>  ->  read([VarName])
        ParseTree read, lpar, var, rpar;

        this.read = true;
        read = match(LexicalUnit.READ);
        lpar = match(LexicalUnit.LPAREN);
        addVar((String) current.getValue());
        entry_str += "%" + v_counter + " = call i32 @readInt()\n";
        v_counter ++;
        entry_str += "store i32 %" + (v_counter - 1) +", i32* %"+  current.getValue() + "\n";
        var = match(LexicalUnit.VARNAME);
        rpar = match(LexicalUnit.RPAREN);
        return new ParseTree(NonTerminal.Read, Arrays.asList(
            read, lpar, var, rpar
        ));
    }
}
