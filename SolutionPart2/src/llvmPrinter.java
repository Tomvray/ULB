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

    public llvmPrinter(ParseTree pTree){
        this.pTree = pTree;
        toprint = "";
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

        if (pT.label.getType() == LexicalUnit.EPSILON)
            return ;
        else
            printinstructionList(pT.children.get(0));
    
    }

    private void printinstructionList(ParseTree pT){
        System.out.println(pT.children.get(0).label.getValue());
        System.out.println(pT.children.get(1).label.getValue());

        printInstruction(pT.children.get(0));
        printInlistTail(pT.children.get(1));
    }

    private void printInstruction(ParseTree pT){
        //toprint += "instruction";
        switch (pT.children.get(0).label.getValue()) {
            case NonTerminal.Assign:
                printAssign(pT.children.get(0));
            default:
                ;
        }
        //System.out.println("Instruction :" + pT.children.get(0).label.getValue());
        toprint += "\n";
    }

    private void printInlistTail(ParseTree pT){
        // [5] <InstListTail>  ->  ...<Instruction><InstListTail>
        if (pT.children.size() == 3){
            printInstruction(pT.children.get(1));
            printInlistTail(pT.children.get(2));
       }
        // [6] <InstListTail>  ->  EPSILON
        else
            return ;

        }
        private void printAssign(ParseTree pT){
            toprint += "%" + pT.children.get(0).label.getValue() + "  = alloca i32\n";
            toprint += "store i32 ";
            printExprArith(pT.children.get(2));
            toprint += ", i32* %"+ pT.children.get(0).label.getValue() +"\n";
        }
        
        private void printExprArith(ParseTree pT){
            printProd(pT.children.get(0));
            printExprArithPrime(pT.children.get(1));
        }

        private void printExprArithPrime(ParseTree pT){
            switch (pT.getChildValue(0)) {
                case LexicalUnit.PLUS:
                    toprint += "+";
                case LexicalUnit.MINUS:

                default :
            }  
        }

        private void printProd(ParseTree pT){
            printAtom(pT.children.get(0));
            printProdPrime(pT.children.get(1));
        }

        private void printAtom(ParseTree pT){
            toprint += pT.children.get(0).label.getValue();
        }

        private void printProdPrime(ParseTree pT){
            toprint += "Pas encore fait";
        }
}