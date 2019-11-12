import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

public class AssemblyC {
	String eval;
	String cCode = "";
	ArrayList<CodeGenTuple> funcTuples;
	Table globalTable;

	public AssemblyC(String newEval) {
		eval = newEval;
		EvalParser parser = new EvalParser();
		parser.program(eval);
		funcTuples = parser.funcTuples;
		globalTable = parser.globalTable;
	}

	public String assembleCcode() {
		cCode = "";
		addHeader();
		
		addFunctions();
		
		addFooter();
		
		return cCode;
	}
	
	private void addHeader() {
		cCode += "#include <stdio.h>\n";
		cCode += "#include <inttypes.h>\n";
		cCode += "int main(int argc, char **argv){\n";
		cCode += "int64_t r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0;\n";
		addGlobals();
		cCode += "int64_t stack[100];\n";
		cCode += "int64_t *sp = &stack[99];\n";
		cCode += "int64_t *fp = &stack[99];\n";
		cCode += "int64_t *ra = &&exit;\n";
		cCode += "goto mainEntry;\n";
	}

	private void addGlobals() {
		TreeMap<String, SymbolType> thisGlobal = globalTable.getTable();
		Set<String> keys = thisGlobal.keySet();
		
		// if not globals
		if (keys.size() == 0)
			return;
		
		boolean firstGlobal = true;
		cCode += "int64_t ";
		for (String key: keys) {
			if (firstGlobal == false)
				cCode += ", ";
			
			if (thisGlobal.get(key) == SymbolType.INT) {
				cCode += key + " = 0";
				firstGlobal = false;
			}
			
		}
		cCode += ";\n";
	}
	
	private void addFunctions() {
//		cCode += "===============TEST CODE ==============\n";
		for(int i = 0; i < funcTuples.size(); i++) {
			CodeGenTuple funcTuple = funcTuples.get(i);
			cCode += funcTuple.getFuncName() + ":\n";
			// TO DO, Change real stack size
			int stackSize = setStackSize(funcTuple.getLocalTable());
			stackSize = 2;
			growStack(stackSize);
			writeAssembly(funcTuple.getThreeAddressList());
			CleanUp(stackSize);
			
		}
		//cCode += "===============TEST CODE ==============\n";
	}
	
	private int setStackSize(Table localTable) {
		int stackSize = 0;
		
		TreeMap<String, SymbolType> thisLocal = localTable.getTable();
		Set<String> keys = thisLocal.keySet();
		
		for (String key : keys ) {
			if (thisLocal.get(key) == SymbolType.INT) {
				stackSize += 1;
			}
		}
		return stackSize;
	}

	private void growStack(int stackSize) {
		
		cCode += "sp = sp - 2;\n";
		cCode += "*(sp+2) = fp;\n";
		cCode += "*(sp+1) = ra;\n";
		cCode += "fp = sp;\n";
		cCode += "sp = sp - " + stackSize + ";\n";
	}
	
	private void writeAssembly(ArrayList<ThreeAddressObject> threeAddressList) {
		cCode += "\n";
		
		for (ThreeAddressObject aTao : threeAddressList) {
//			int src1off = 0;
//			int src2off = 0;
//			int srcDESoff = 0;
//			
//			if (aTao.src1 != null && aTao.src1.toString().equals("temp0")) {
//				src1off = 1;
//			}
//			else {
//				src1off = 2;
//			}
//			
//			if (aTao.src2 != null && aTao.src2.toString().equals("temp0")) {
//				src2off = 1;
//			}
//			else {
//				src2off = 2;
//			}
//			
//			if (aTao.destination != null && aTao.destination.toString().equals("temp0")) {
//				srcDESoff = 1;
//			}
//			else {
//				srcDESoff = 2;
//			}
//			
//			
//			switch(aTao.op) {
//			case NUM:
//				cCode += "r1 = " + aTao.src1 + ";\n";
//				if (aTao.destination.toString().contains("temp"))
//					cCode += "*(fp-" + srcDESoff + ") = r1;\n";
//				else
//					cCode += aTao.destination + "= r1;\n";
//				break;
//			case PLUS:
//				cCode += "r1 = *(fp-" + (src1off) + ");\n";
//				cCode += "r2 = *(fp-" + (src2off) + ");\n";
//				cCode += "r3 = r1 + r2;\n";
//				cCode += "*(fp-" + srcDESoff + ") = r3;\n";
//				
//				break;
//			case ASSIGN:
//				cCode += "r1 = *(fp-" + (src1off) + ");\n";
//				if (aTao.destination.toString().contains("temp"))
//					cCode += "*(fp-" + srcDESoff + ") = r1;\n";
//				else
//					cCode += aTao.destination + "= r1;\n";
//				break;
//			case LT:
//				cCode += "r1 = *(fp-" + (src1off) + ");\n";
//				cCode += "r2 = *(fp-" + src2off + ");\n";
//				cCode += "if (r1 < r2) goto truelabel" + aTao.destination + ";\n";
//				break;
//			case GOTO:
//				cCode += "goto falselabel" + aTao.destination + ";\n";
//			case LABEL:
//				cCode += aTao.src1 + ":\n";
//			}
			switch(aTao.op) {
			case NUM:
				cCode += "r1 = " + aTao.src1 + ";\n";
				cCode += "*(fp-" + (aTao.destination + ".offset") + " ) = r1;\n";
				break;
			case PLUS:
				cCode += "r1 = " + (aTao.src1 + ".offset") + ";\n";
				cCode += "r2 = " + (aTao.src2 + ".offset") + ";\n";
				cCode += "r3 = r1 + r2;\n";
				cCode += "*(" + (aTao.destination + ".offset") + ") = r3;\n";
				break;
			case ASSIGN:
				cCode += "r1 = (" + (aTao.src1 + ".offset") + ");\n";
				cCode += "*(fp-" + (aTao.destination + ".offset") + ") = r1;\n";
				break;
			case LT:
				cCode += "r1 = *(" + (aTao.src1 + ".offset") + ");\n";
				cCode += "r2 = *(" + (aTao.src2 + ".offset") + ");\n";
				cCode += "if (r1 < r2) goto truelabel" + aTao.destination + ";\n";
				break;
			case GOTO:
				cCode += "goto falselabel" + aTao.destination + ";\n";
			case LABEL:
				cCode += aTao.src1 + ":\n";
			}
		}
		
		
		cCode += "\n";
	}
	
	private void CleanUp(int stackSize) {
		cCode += "falselabel0:\n";
		cCode += "sp = sp + " + stackSize + ";\n";
		cCode += "fp = *(sp+2);\n";
		cCode += "ra = *(sp+1);\n";
		cCode += "sp = sp + 2;\n";
		cCode += "goto *ra;\n";
	}

	private void addFooter() {

		cCode += "exit:\n";
		cCode += "printf(\"%d\\n\", reserved);\n";
		cCode += "return reserved;\n";
		cCode += "}";
	}
}
