public class Symbol
{
    enum SymbolType
    {
        LEXEME, INT, FUNC
    }
    private SymbolType type;
    private String identifier;
    private String value;
    private int offset;
    
    public void setType( SymbolType type )
    {
        this.type = type;
    }
    
    public void setIdentifier( String identifier )
    {
    	this.identifier = identifier;
    }
    
    public void setValue( String value )
    {
    	this.value = value;
    }

    public void setOffset( int offset )
    {
        this.offset = offset;
    }

    public SymbolType getType()
    {
    	return this.type;
    }
    
    public String getIdentifier()
    {
    	return this.identifier;
    }
    
    public String getValue()
    {
    	return this.value;
    }

    public int getOffset( )
    {
        return offset;
    }
}
