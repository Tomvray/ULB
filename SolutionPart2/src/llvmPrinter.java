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
    }

    public void generatellvm(){
        toprint += "int main()\n"
        printCode();
        System.out.println("}");
    }

    private void printCode(ParseTree pT){
        for (ParseTree child in pTree){
            if (child.label.getType() == EPSILON)
                return ;
            else
                printinstructionList(child);
        }
    }

    private void printinstructionList(ParseTree pT){
        printInlist(pT.children[0]);
        printInlistTail(pT.children[1]);
    }

}