import java.util.TreeMap;

public class Table {
    private TreeMap< String, Symbol > table;
    private Table prev;

    public Table( Table p )
    {
        table = new TreeMap<String,Symbol>();
        prev = p;
    }

    public Table() {
	}

	public void add( String key, Symbol sym )
    {
        table.put( key, sym );
    }

    public Symbol find( Token aToken )
    {
        String key = aToken.tokenVal;
        for ( Table e = this; e != null; e = e.prev )
        {
            Symbol found = e.table.get(key);
            if ( found != null )
            {
                return found;
            }
        }
        return null;
    }

    public TreeMap< String, Symbol > getTable()
    {
        return table;
    }
}
