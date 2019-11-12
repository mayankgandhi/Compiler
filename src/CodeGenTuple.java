import java.util.ArrayList;

public class CodeGenTuple {

	ArrayList<ThreeAddressObject> threeAddressList;
	Table localTable;
	String funcName;
	int stackSize;

	public CodeGenTuple(ArrayList<ThreeAddressObject> newObjects, Table localTable, String funcName) {
		this.threeAddressList=newObjects;
		this.localTable=localTable;
		this.funcName=funcName;
		this.stackSize = 0;
	}
	
	public ArrayList<ThreeAddressObject> getThreeAddressList() {
		return threeAddressList;
	}

	public void setThreeAddressList(ArrayList<ThreeAddressObject> threeAddressList) {
		this.threeAddressList = threeAddressList;
	}

	public Table getLocalTable() {
		return localTable;
	}

	public void setLocalTable(Table localTable) {
		this.localTable = localTable;
	}

	public String getFuncName() {
		return funcName;
	}

	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}

	public int getStackSize() {
		return stackSize;
	}

	public void setStackSize(int stackSize) {
		this.stackSize = stackSize;
	}
}