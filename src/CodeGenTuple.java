import java.util.ArrayList;

public class CodeGenTuple {
	ArrayList<ThreeAddressObject> newObjects;
	Table localTable;
	String funcName;

	public CodeGenTuple(ArrayList<ThreeAddressObject> newObjects, Table localTable, String funcName) {
		this.newObjects=newObjects;
		this.localTable=localTable;
		this.funcName=funcName;
	}

}
