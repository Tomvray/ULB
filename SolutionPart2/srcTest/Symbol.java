/**
 * Symbol objects represent a terminal or non-terminal symbol in the grammar.
 * 
 * @author Not fully determined but assmed to be among Marie Van Den Bogaard, Léo Exibard, Gilles Geeraerts. Javadoc by Mathieu Sassolas.
 */
 
 public class Symbol{
     /**
      * Undefined line/column position of symbol.
      */
	private static final int UNDEFINED_POSITION = -1; // Changed to private because I didn't see why they were public
    
     /**
      * No value attached to symbol, for terminals without value.
      */
	private static final Object NO_VALUE = null;      // and nothing bad happened, so I guess it works -- MS
	
    /**
     * The LexicalUnit (terminal) attached to this token.
     */
	private final LexicalUnit type;
    
    /**
     * The value attached to the token.
     * 
     * May be any Object. In fact, for terminals with value it is indeed the value attached to the terminal. It can also be used to store the {@link NonTerminal NonTerminal} variable when the symbol is non-terminal.
     */
	private final Object value;
    
    /**
     * The position of the symbol in the parsed file.
     */
	private final int line,column;

    /**
     * Creates a Symbol using the provided attributes.
     * 
     * @param unit the LexicalUnit (terminal) associated with the symbol.
     * @param line the line where the symbol appears in the file.
     * @param column the column where the symbol appears in the file.
     * @param value the value of the symbol: value of a terminal symbol or {@link NonTerminal NonTerminal} associated with the symbol.
     */
	public Symbol(LexicalUnit unit,int line,int column,Object value){
		this.type	= unit;
		this.line	= line+1;
		this.column	= column;
		this.value	= value;
	}
	
    /**
     * Creates a Symbol using the provided attributes and no value.
     * 
     * @param unit the LexicalUnit (terminal) associated with the symbol.
     * @param line the line where the symbol appears in the file.
     * @param column the column where the symbol appears in the file.
     */
	public Symbol(LexicalUnit unit,int line,int column){
		this(unit,line,column,NO_VALUE);
	}
	
    /**
     * Creates a Symbol using the provided attributes, without column nor value.
     * 
     * @param unit the LexicalUnit (terminal) associated with the symbol.
     * @param line the line where the symbol appears in the file.
     */
	public Symbol(LexicalUnit unit,int line){
		this(unit,line,UNDEFINED_POSITION,NO_VALUE);
	}
	
    /**
     * Creates a Symbol using the provided attributes, without position or value.
     * 
     * @param unit the LexicalUnit (terminal) associated with the symbol.
     */
	public Symbol(LexicalUnit unit){
		this(unit,UNDEFINED_POSITION,UNDEFINED_POSITION,NO_VALUE);
	}
	
    /**
     * Creates a Symbol using the provided attributes, without position.
     * 
     * @param unit the LexicalUnit (terminal) associated with the symbol.
     * @param value the value of the symbol: value of a terminal symbol or {@link NonTerminal NonTerminal} associated with the symbol.
     */
	public Symbol(LexicalUnit unit,Object value){
		this(unit,UNDEFINED_POSITION,UNDEFINED_POSITION,value);
	}

    /**
     * Returns whether the symbol represents a terminal.
     * 
     * A terminal symbol must have a non-null LexicalUnit type.
     * 
     * @return a boolean which is true iff the Symbol represents a terminal.
     */
	public boolean isTerminal(){
		return this.type != null;
	}
	
    /**
     * Returns whether the symbol represents a non-terminal.
     * 
     * A non-terminal symbol has no type.
     * 
     * @return a boolean which is true iff the Symbol represents a non-terminal.
     */
	public boolean isNonTerminal(){
		return this.type == null;
	}
	
    /**
     * Returns the type of the symbol.
     * 
     * The type of a non-terminal is null.
     * 
     * @return the value of attribute {@link type type}.
     */
	public LexicalUnit getType(){
		return this.type;
	}
	
    /**
     * Returns the value of the symbol.
     * 
     * @return the value of attribute {@link value value}.
     */
	public Object getValue(){
		return this.value;
	}
	
    /**
     * Returns the line where the symbol appeared.
     * 
     * @return the value of attribute {@link line line}.
     */
	public int getLine(){
		return this.line;
	}
	
    /**
     * Returns the column where the symbol appeared.
     * 
     * @return the value of attribute {@link column column}.
     */
	public int getColumn(){
		return this.column;
	}
	
    /**
     * Returns a hash code value for the object.
     * 
     * @return a hash code based on the type and value of the Symbol.
     */
	@Override
	public int hashCode(){
		final String value	= this.value != null? this.value.toString() : "null";
		final String type		= this.type  != null? this.type.toString()  : "null";
		return new String(value+"_"+type).hashCode();
	}
	
    /**
     * Returns a string representation of the symbol.
     * This method has been modified from the provided class to provide the value of the non-terminal symbols.
     * 
     * @return a string representation of the token's value and type.
     */
	@Override
	public String toString(){
        final String value	= this.value != null? this.value.toString() : "null";
		if(this.isTerminal()){
			final String type		= this.type  != null? this.type.toString()  : "null";
			return "token: "+value+"\tlexical unit: "+type;
		}
		return "Non-terminal symbol: "+value;
	}
    
    /**
     * Returns a LaTeX representation of the symbol.
     * 
     * @return a string containing LaTeX code of a representation of the token's value and type.
     */
	public String toTexString(){
        String value = "";
		if(this.isTerminal()){
            if (this.type == LexicalUnit.VARNAME || this.type == LexicalUnit.NUMBER) {
                value = this.value != null? ": "+this.value.toString() : "";
            }
			final String type = this.type  != null? this.type.toTexString()  : "null";
			return type+value;
		} else {
            if (this.value != null && this.value instanceof NonTerminal) {
                value	= ((NonTerminal) this.value).toTexString();
            } else {
                value="null";
            }
			return value;
        }
		//return "UNREACHABLE RETURN";
	}
}
