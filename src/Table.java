import java.util.Map;
import java.util.TreeMap;

public class Table {
    private TreeMap< String, SymbolType > table;
//    changed tino
//    private Table prev;
    Table prev;

    public Table( Table p )
    {
        table = new TreeMap<String,SymbolType>();
        prev = p;
    }

    public Table() {
    	table = new TreeMap<String,SymbolType>();
    	prev = null;
	}

	public void add( String key, SymbolType sym )
    {
        table.put( key, sym );
        
        // changed tino
        printWholeTable();
    }

	// changed tino
    private void printWholeTable() {
    	Table newTable = this;
    	while (newTable != null) {
    	  System.out.println("-------------");
    	  for (Map.Entry<String, SymbolType> entry : newTable.table.entrySet()) {
    	        SymbolType value = entry.getValue();
    	        String thiskey = entry.getKey();
    	        System.out.println("ID: " + thiskey + " | Type: " + value);
    	   }
    	  newTable = newTable.prev;
    	}
    	System.out.println("====================================");
	}

	public SymbolType find( Token aToken )
    {	  
        String key = aToken.tokenVal;
        for ( Table e = this; e != null; e = e.prev )
        {
        	SymbolType found = e.table.get(key);
            if ( found != null )
            {
                return found;
            }
        }
        return null;
    }
    
    public SymbolType findLocally(Token aToken) {
    	String key = aToken.tokenVal;
    	
    	SymbolType found = this.table.get(key);
        if ( found != null )
        {
            return found;
        }
        
        return null;
    }

    public TreeMap< String, SymbolType > getTable()
    {
        return table;
    }
}
