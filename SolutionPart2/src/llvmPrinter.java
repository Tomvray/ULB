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
    
    private String toprint;

    private int counter;

    public llvmPrinter(ParseTree pTree){
        this.pTree = pTree;
        toprint = "";
        counter = 0;
    }

    public void generatellvm(){
        toprint += "int main()\n";
        System.out.println(pTree.children.get(1).label);
        printCode(pTree.children.get(1));
        toprint += "}";
        System.out.println(toprint);
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
        //toprint += "instruction";
        System.out.println("Instruction :");
        switch ((NonTerminal) pT.children.get(0).label.getValue()) {
            // [7] <Instruction>  ->  <Assign>
            case Assign:
                printAssign(pT.children.get(0));
                break;
             // [8] <Instruction>  ->  <If>
            case If:
            // [9] <Instruction>  ->  <While>
            case While:
             // [10] <Instruction>  ->  <Print>
            case Print:
                    printPrint(pT.children.get(0))
            // [11] <Instruction>  ->  <Read>
            case Read:
            // [12] <Instruction>  ->  begin <InstList> end
            case Instruction:
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
            toprint += "%" + pT.children.get(0).label.getValue() + "  = alloca i32\n";
            toprint += "store i32 ";
            printExprArith(pT.children.get(2));
            toprint += ", i32* %"+ pT.children.get(0).label.getValue() +"\n";
             */
             // [13] <Assign>  ->  [Varname] := <ExprArith>
            printExprArith(pT.getChild(2));
            toprint += "%" + pT.children.get(0).label.getValue() + "  = alloca i32\n";
            toprint += "store i32 %" + (counter - 1) +", i32* %"+  pT.children.get(0).label.getValue() + "\n";
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
            int ante = counter - 1;
            switch ((LexicalUnit) pT.getChild(0).label.getType()) {
                // [15] <ExprArith'>  ->  + <Prod> <ExprArith'>
                case PLUS:
                    printProd(pT.getChild(1));
                    printExprArithPrime(pT.getChild(2));
                    toprint += "%" + counter + " = add i32 %" + ante + ", %" + (counter - 1) + "\n";
                    counter ++;
                    return;
                // [16] <ExprArith'>  ->  - <Prod> <ExprArith'>
                case MINUS:
                    printProd(pT.getChild(1));
                    printExprArithPrime(pT.getChild(2));
                    toprint += "%" + counter + " = sub i32 %" + ante + ", %" + (counter - 1) + "\n";
                    counter ++;
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
                toprint += "%" + counter + " = sub i32 0, %" + (counter - 1) + "\n";
                counter++;
                return ;
            // [23] <Atom>  ->  (<ExprArith>)
            case LPAREN:
                printExprArith(pT.getChild(1));
                return ;
            // [24] <Atom>  ->  [VarName]
            case VARNAME:
                toprint+= "%" + counter + " = load i32 , i32 * %" + pT.children.get(0).label.getValue() + "\n";
                counter++;
                return ;
            // [25] <Atom>  ->  [Number]
            case NUMBER:
                toprint += "%" + counter + " = add i32 0, " + pT.children.get(0).label.getValue() + "\n";
                counter++;
                return ;
            default:
                return ;
            }
        }

        private void printProdPrime(ParseTree pT){
            int ante = counter - 1;
            switch ((LexicalUnit) pT.getChild(0).label.getType()) {
                // [19] <Prod'>  ->  * <Atom> <Prod'>
                case TIMES:
                    printAtom(pT.getChild(1));
                    printProdPrime(pT.getChild(2));
                    toprint += "%" + counter + " = mul i32 %" + ante + ", %" + (counter - 1) + "\n";
                    counter ++;
                    return;
                // [20] <Prod'>  ->  / <Atom> <Prod>
                case DIVIDE:
                    printAtom(pT.getChild(1));
                    printProdPrime(pT.getChild(2));
                    toprint += "%" + counter + " = sdiv i32 %" + ante + ", %" + (counter - 1) + "\n";
                    counter ++;
                    return ;

                default :
                    return ;
                }  
            
        }

        private void printIfExpr(ParseTree pT){

        }

        private void printPrint(ParseTree pT){
            // [40] <Print>  ->  print([VarName])
            toprint += "%" + counter + " = load i32, i32* " + pT.children.get(2).label.getValue() + " \n"
            toprint += "  call void @println(i32 %" + counter + ")\n"
            counter++
        }
}