import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

public class AssemblyC {
	String eval;
	String cCode = "";
	ArrayList<CodeGenTuple> funcTuples;
	Table globalTable;
	Table localTable;
	TreeMap<String, Integer> offsets;

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
		for (String key : keys) {
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
		for (int i = 0; i < funcTuples.size(); i++) {
			offsets = new TreeMap<String, Integer>();
			CodeGenTuple funcTuple = funcTuples.get(i);
			cCode += funcTuple.getFuncName() + ":\n";
			int stackSize = setStackSize(funcTuple.getLocalTable());
			
			growStack(stackSize);
			writeAssembly(funcTuple.getThreeAddressList());
			CleanUp(stackSize);

		}
	}

	private int setStackSize(Table localTable) {
		int stackSize = 0;

		this.localTable = localTable;
		TreeMap<String, SymbolType> thisLocal = localTable.getTable();
		Set<String> keys = thisLocal.keySet();

		int offset = 1;
		for (String key : keys) {

			if (thisLocal.get(key) == SymbolType.INT) {
				offsets.put(key, offset);
				stackSize += 1;
				offset++;
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

			// This code is to differentiate between temps and actual variables.
			String offset_src1 = "";
			String offset_src2 = "";
			String offset_dest = "";
			if (aTao.src1 != null && localTable.getTable().containsKey(aTao.src1.toString())) {
				offset_src1 = "";
				offset_src1 = "*(fp-" + offsets.get(aTao.src1.toString()) + ")";
			} else {
				if (aTao.src1 != null)
					offset_src1 = aTao.src1.toString();
			}

			if (aTao.src2 != null && localTable.getTable().containsKey(aTao.src2.toString())) {
				offset_src2 = "";
				offset_src2 = "*(fp-" + offsets.get(aTao.src2.toString()) + ")";
			} else {
				if (aTao.src2 != null)
					offset_src2 = aTao.src2.toString();
			}

			if (aTao.destination != null && localTable.getTable().containsKey(aTao.destination.toString())) {
				offset_dest = "";
				offset_dest = "*(fp-" + offsets.get(aTao.destination.toString()) + ")";
			} else {
				if (aTao.destination != null)
					offset_dest = aTao.destination.toString();
			}

			switch (aTao.op) {
			case NUM:
				cCode += "r1 = " + aTao.src1 + ";\n";
				cCode += offset_dest + " = r1;\n";
				break;
			case PLUS:
				cCode += "r1 = " + offset_src1 + ";\n";
				cCode += "r2 = " + offset_src2 + ";\n";
				cCode += "r3 = r1 + r2;\n";
				cCode += offset_dest + " = r3;\n";
				break;
			case MINUS:
				cCode += "r1 = " + offset_src1 + ";\n";
				cCode += "r2 = " + offset_src2 + ";\n";
				cCode += "r3 = r1 - r2;\n";
				cCode += offset_dest + " = r3;\n";
				break;
			case MUL:
				cCode += "r1 = " + offset_src1 + ";\n";
				cCode += "r2 = " + offset_src2 + ";\n";
				cCode += "r3 = r1 * r2;\n";
				cCode += offset_dest + " = r3;\n";
				break;
			case DIV:
				cCode += "r1 = " + offset_src1 + ";\n";
				cCode += "r2 = " + offset_src2 + ";\n";
				cCode += "r3 = r1 / r2;\n";
				cCode += offset_dest + " = r3;\n";
				break;
			case ASSIGN:
				cCode += "r1 = " + offset_src1 + ";\n";
				cCode += offset_dest + " = r1;\n";
				break;
			case LT:
				cCode += "r1 = " + offset_src1 + ";\n";
				cCode += "r2 = " + offset_src2 + ";\n";
				cCode += "if (r1 < r2) goto truelabel" + aTao.destination + ";\n";
				break;
			case LTE:
				cCode += "r1 = " + offset_src1 + ";\n";
				cCode += "r2 = " + offset_src2 + ";\n";
				cCode += "if (r1 <= r2) goto truelabel" + aTao.destination + ";\n";
				break;
			case GT:
				cCode += "r1 = " + offset_src1 + ";\n";
				cCode += "r2 = " + offset_src2 + ";\n";
				cCode += "if (r1 > r2) goto truelabel" + aTao.destination + ";\n";
				break;
			case GTE:
				cCode += "r1 = " + offset_src1 + ";\n";
				cCode += "r2 = " + offset_src2 + ";\n";
				cCode += "if (r1 >= r2) goto truelabel" + aTao.destination + ";\n";
				break;
			case GOTO:
				cCode += "goto falselabel" + aTao.destination + ";\n";
				break;
			case LABEL:
				cCode += aTao.src1 + ":\n";
				break;
			case IF: // destination for IF statement should be where it goes if true
				cCode += "falselabel" + aTao.destination.toString() + ":\n";
				break;
			case START_WHILE:
				cCode += "repeatLabel" + aTao.src1.toString() + ":\n";
				break;
			case WHILE:
				cCode += "goto repeatLabel" + aTao.src1.toString() + ";\n";
				cCode += "falselabel" + aTao.destination.toString() + ":\n";
				break;
			case EQUALS:
				cCode += "r1 = " + offset_src1 + ";\n";
				cCode += "r2 = " + offset_src2 + ";\n";
				cCode += "if (r1 == r2) goto truelabel" + aTao.destination + ";\n";
				break;
			case NOTEQUALS:
				cCode += "r1 = " + offset_src1 + ";\n";
				cCode += "r2 = " + offset_src2 + ";\n";
				cCode += "if (r1 != r2) goto truelabel" + aTao.destination + ";\n";
				break;
			}
			
		}

		cCode += "\n";
	}

	private void CleanUp(int stackSize) {
		cCode += "sp = sp + " + stackSize + ";\n";
		cCode += "fp = *(sp+2);\n";
		cCode += "ra = *(sp+1);\n";
		cCode += "sp = sp + 2;\n";
		cCode += "goto *ra;\n";
	}

	private void addFooter() {

		cCode += "exit:\n";
		//cCode += "printf(\"%d\\n\", reserved);\n";
		cCode += "return reserved;\n";
		cCode += "}";
	}
}
