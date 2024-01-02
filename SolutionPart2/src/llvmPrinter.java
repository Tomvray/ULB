import java.util.Arrays;

/**
 * llvm printer for PascalMaisPresque.
 * 
 * Print the code in llvm.
 * 
 * @author Thomas Vray.
 *
 */

public class llvmPrinter{
    /**
     * 
     */
    private ParseTree pTree;
    
    private String entry_str;

    private int if_counter = 0;

    private int while_counter = 0;

    private int v_counter;

    public llvmPrinter(ParseTree pTree){
        this.pTree = pTree;
        entry_str = "";
       v_counter = 0;
    }

    public void generatellvm(){
        entry_str += "define i32 @main() {\nentry:\n";
        System.out.println(pTree.children.get(1).label);
        printCode(pTree.children.get(1));
        entry_str += "ret i32 0\n}";
        System.out.println(entry_str);
    }

    private void printCode(ParseTree pT){
        System.out.println("printcode");
        System.out.println(pT.children.get(0).label);
        // [3] <Code>  ->  EPSILON
        if (pT.label.getType() == LexicalUnit.EPSILON)
            return ;
        // [2] <Code>  ->  <InstList>
        else
            printinstructionList(pT.children.get(0));
    
    }

    private void printinstructionList(ParseTree pT){
        //System.out.println(pT.children.get(0).label.getValue());
        //System.out.println(pT.children.get(1).label.getValue());
        // [4] <InstList>  ->  <Instruction><InstListTail>
        printInstruction(pT.children.get(0));
        printInlistTail(pT.children.get(1));
    }

    private void printInstruction(ParseTree pT){
        //entry_str += "instruction";
        System.out.println("Instruction :");
        // [12] <Instruction>  ->  begin <InstList> end
        if (pT.getChild(0).label.getType() == LexicalUnit.BEG){
            printinstructionList(pT.getChild(1));
            return ;
        }
        switch ((NonTerminal) pT.children.get(0).label.getValue()) {
            // [7] <Instruction>  ->  <Assign>
            case Assign:
                printAssign(pT.children.get(0));
                break;
             // [8] <Instruction>  ->  <If>
            case If:
                printIf(pT.children.get(0));
                break;
            // [9] <Instruction>  ->  <While>
            case While:
                printWhile(pT.children.get(0));
                break;
             // [10] <Instruction>  ->  <Print>
            case Print:
                printPrint(pT.children.get(0));
                break;
            // [11] <Instruction>  ->  <Read>
            case Read:
                printRead(pT.children.get(0));
                break;
            default:
                ;
        }
    }

    private void printInlistTail(ParseTree pT){
        // [5] <InstListTail>  ->  ...<Instruction><InstListTail>
        System.out.println("printInlistTail :");
        if (pT.children.size() == 3){
            printInstruction(pT.children.get(1));
            printInlistTail(pT.children.get(2));
       }
        // [6] <InstListTail>  ->  EPSILON
        else
            return ;

        }
        private void printAssign(ParseTree pT){
            /*
            entry_str += "%" + pT.children.get(0).label.getValue() + "  = alloca i32\n";
            entry_str += "store i32 ";
            printExprArith(pT.children.get(2));
            entry_str += ", i32* %"+ pT.children.get(0).label.getValue() +"\n";
             */
             // [13] <Assign>  ->  [Varname] := <ExprArith>
            printExprArith(pT.getChild(2));
            entry_str += "%" + pT.children.get(0).label.getValue() + "  = alloca i32\n";
            entry_str += "store i32 %" + (v_counter - 1) +", i32* %"+  pT.children.get(0).label.getValue() + "\n";
        }
        
        private void printExprArith(ParseTree pT){
            System.out.println("EXPArith :");
            // [14] <ExprArith>  ->  <Prod> <ExprArith'>
            printProd(pT.getChild(0));
            printExprArithPrime(pT.getChild(1));
            //printAtom(pT.getChild(0).getChild(0));
            //printProd(pT.children.get(0));
            //printExprArithPrime(pT.children.get(1));
        }

        private void printExprArithPrime(ParseTree pT){
            System.out.println("ExpArithPrime :");
            System.out.println(pT.getChild(0).label.getType() == LexicalUnit.EPSILON);
            // [17] <ExprArith'>  ->  EPSILON
            if (pT.getChild(0).label.getType() == LexicalUnit.EPSILON)
                return ;
            System.out.println("ici0");
            int ante = v_counter - 1;
            switch ((LexicalUnit) pT.getChild(0).label.getType()) {
                // [15] <ExprArith'>  ->  + <Prod> <ExprArith'>
                case PLUS:
                    printProd(pT.getChild(1));
                    printExprArithPrime(pT.getChild(2));
                    entry_str += "%" + v_counter + " = add i32 %" + ante + ", %" +(v_counter - 1) + "\n";
                    v_counter ++;
                    return;
                // [16] <ExprArith'>  ->  - <Prod> <ExprArith'>
                case MINUS:
                    printProd(pT.getChild(1));
                    printExprArithPrime(pT.getChild(2));
                    entry_str += "%" + v_counter + " = sub i32 %" + ante + ", %" +(v_counter - 1) + "\n";
                    v_counter ++;
                    return ;

                default :
                    return ;
            }  
        }

        private void printProd(ParseTree pT){
            System.out.println("Prod :");
            // [18] <Prod>  ->  <Atom> <Prod'>
            printAtom(pT.children.get(0));
            printProdPrime(pT.children.get(1));
        }

        private void printAtom(ParseTree pT){
            switch ((LexicalUnit) pT.getChild(0).label.getType()) {
            // [22] <Atom>  ->  - <Atom>
            case MINUS:
                printAtom(pT.getChild(1));
                entry_str += "%" +v_counter + " = sub i32 0, %" + (v_counter - 1) + "\n";
                v_counter++;
                return ;
            // [23] <Atom>  ->  (<ExprArith>)
            case LPAREN:
                printExprArith(pT.getChild(1));
                return ;
            // [24] <Atom>  ->  [VarName]
            case VARNAME:
                entry_str+= "%" + v_counter + " = load i32 , i32* %" + pT.children.get(0).label.getValue() + "\n";
                v_counter++;
                return ;
            // [25] <Atom>  ->  [Number]
            case NUMBER:
                entry_str += "%" + v_counter + " = add i32 0, " + pT.children.get(0).label.getValue() + "\n";
                v_counter++;
                return ;
            default:
                return ;
            }
        }

        private void printProdPrime(ParseTree pT){
            int ante = v_counter - 1;
            switch ((LexicalUnit) pT.getChild(0).label.getType()) {
                // [19] <Prod'>  ->  * <Atom> <Prod'>
                case TIMES:
                    printAtom(pT.getChild(1));
                    printProdPrime(pT.getChild(2));
                    entry_str += "%" + v_counter + " = mul i32 %" + ante + ", %" + (v_counter - 1) + "\n";
                    v_counter ++;
                    return;
                // [20] <Prod'>  ->  / <Atom> <Prod>
                case DIVIDE:
                    printAtom(pT.getChild(1));
                    printProdPrime(pT.getChild(2));
                    entry_str += "%" + v_counter + " = sdiv i32 %" + ante + ", %" + (v_counter - 1) + "\n";
                    v_counter ++;
                    return ;

                default :
                    return ;
                }  
            
        }

        private void printIf(ParseTree pT){
                    System.out.println(pT.label.getValue());

            System.out.println("IF");
            // [26] <If>  -> if <Cond> then <Instruction> else <IfTail>
            printCond(pT.getChild(1));
            entry_str += "br i1 %" + (v_counter - 1) + ", label %iftrue" + if_counter + ", label %iffalse" + if_counter + "\niftrue" + if_counter + ":\n";
            if_counter++;
            printInstruction(pT.getChild(3));
            entry_str +="br label %endif" + (if_counter - 1) + "\niffalse" + (if_counter - 1) + ":\n";
            printIfTail(pT.getChild(5));
            entry_str +="br label %endif" + (if_counter - 1) + "\nendif" + (if_counter - 1) + ":\n";
        }

    private void printCond(ParseTree pT){
        System.out.println("COND:");
                System.out.println(pT.label.getValue());

        // [29] <Cond>  -> <Conj> <Cond'>
        printConj(pT.getChild(0));
        int ante = v_counter - 1;
        printCondP(pT.getChild(1));
        //entry_str += "%" +v_counter + " = icmp slt i32 %" + ante + ", %" + (v_counter - 1) + "\n";
        //v_counter++;
        return ;
    }

    private void printConj(ParseTree pT){
        // [32] <Conj>  -> <SimpleCond> <Conj'>
        System.out.println("CONJ");
        System.out.println(pT.label.getValue());
        printSimpleCond(pT.getChild(0));
        printConjP(pT.getChild(1));
    }

    private void printConjP(ParseTree pT){
        System.out.println("CONJP");
        switch ((LexicalUnit) pT.getChild(0).label.getType()) {
            
            // [33] <Conj'>  ->  and <SimpleCond> <Conj'>
            case AND:
                int ante = v_counter - 1;
                printSimpleCond(pT.getChild(1));
                printConjP(pT.getChild(2));
                entry_str += "%" + v_counter + "= and i1 %" + ante + ", %" + (v_counter - 1) +"\n";
                v_counter++;
                break;
            // [34] <Conj'>  ->  EPSILON
            default :
                return ;
        }
    }

    private void printSimpleCond(ParseTree pT){
        int ante;
        System.out.println(pT.getChild(0).label.getValue());
            // [35] <SimpleCond>  ->  {<Cond>}
            if (pT.getChild(0).label.getType() == LexicalUnit.LBRACK){
                printCond(pT.getChild(1));
                return ;
            }

            // [36] <SimpleCond>  ->  <ExprArith> <Comp> <ExprArith>
            else{
                printExprArith(pT.children.get(0));
                ante = v_counter - 1;
                printExprArith(pT.getChild(2));
                System.out.println("ICIICIC");

                //equal
                if (pT.getChild(1).getChild(0).label.getType() == LexicalUnit.EQUAL)
                {
                    entry_str += "%" + v_counter + "= icmp eq i32 %" + ante + ", %" + (v_counter - 1) +"\n";
                    v_counter ++;
                }
                //smaller
                else
                {
                    entry_str += "%" + v_counter + "= icmp slt i32 %" + ante + ", %" + (v_counter - 1) +"\n";
                    v_counter++;
                }
            }

    }

    private void printCondP(ParseTree pT){
        System.out.println("CONDP");
        System.out.println(pT.label.getValue());

        // [31] <Cond'>  ->  EPSILON
        if (pT.getChild(0).label.getType() == LexicalUnit.EPSILON)
        {
            return ;
        }
        // [30] <Cond'>  ->  or <Conj> <Cond'>
        else{
            int ante = v_counter - 1;
            printConj(pT.getChild(1));
            printCondP(pT.getChild(2));
            entry_str += "%" + v_counter + "= and i1 %" + ante + ", %" + (v_counter - 1) +"\n";
            v_counter++;
        }
    }

    private void printIfTail(ParseTree pT){
        if (pT.getChild(0).label.getType() == LexicalUnit.EPSILON){
            return ;
        }
        printInstruction(pT.getChild(0));
        return ;
    }
    private void printWhile(ParseTree pT){
        // [39] <While>  ->  while <Cond> do <Instruction>
        entry_str += "br label %while.cond" + while_counter + "\nwhile.cond" + while_counter +":\n";
        printCond(pT.getChild(1));
        entry_str += "br i1 %" + (v_counter - 1) + ", label %while.body"+ while_counter + ", label %while.end" + while_counter +"\nwhile.body" + while_counter + ":\n";
        while_counter++;
        System.out.println(pT.getChild(2).label.getValue());
        printInstruction(pT.getChild(3));
        entry_str += "br label %while.cond"+ (while_counter - 1) + "\nwhile.end"+ (while_counter - 1)+ ":\n";


    }

    private void printPrint(ParseTree pT){
        // [40] <Print>  ->  print([VarName])
        entry_str += "%" + v_counter + " = load i32, i32* %" + pT.children.get(2).label.getValue() + " \n";
        entry_str += "call void @println(i32 %" + v_counter + ")\n";
        v_counter++;
    }
    private void printRead(ParseTree pT){
        entry_str += "%" + pT.getChild(2).label.getValue() + " = call i32 @readInt()\n";
    }
}