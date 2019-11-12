public class ThreeAddressObject { // Three Address Object
	enum Operation {
		NUM, PLUS, MINUS, MUL, DIV, LT, LTE, GT, GTE, LEFTPAREN, RIGHTPAREN, EQUALS, NOTEQUALS, ASSIGN, ID, INT,
		SEMICOLON, LEFTCURLY, RIGHTCURLY, IF, WHILE, START_WHILE, AND, OR, VOID, PUBLIC, PRIVATE, CLASS, PROGRAM,
		INVALID, LABEL, GOTO
	}

	/** Values for ThreeAddressObject class */
	Operation op;
	Operand src1;
	Operand src2;
	Operand destination;

	ThreeAddressObject(Operation oper) {
		op = oper;
	}

	ThreeAddressObject(Operation oper, Operand value) {
		if (oper == Operation.LABEL) {
			op = oper;
			src1 = value;
		} else if (oper == Operation.START_WHILE) {
			op = oper;
			src1 = value;
		} else {
			op = oper;
			destination = value;
		}
	}

	ThreeAddressObject(Operation oper, Operand value, Operand dest) {
		this.op = oper;
		this.src1 = value;
		this.destination = dest;
	}

	ThreeAddressObject(Operation oper, Operand one, Operand two, Operand dest) {
		this.op = oper;
		this.src1 = one;
		this.src2 = two;
		this.destination = dest;
	}

	public String toString() {
		String ans = "";

		switch (op) {
		case ASSIGN:
			ans = this.destination.toString() + " = " + this.src1.toString() + "\n";
			break;
		case NUM:
			ans = destination.toString() + " = " + src1.toString() + "\n";
			break;
		case PLUS:
			ans = this.destination.toString() + " = " + this.src1.toString() + " + " + this.src2.toString() + "\n";
			break;
		case MINUS:
			ans = this.destination.toString() + " = " + this.src1.toString() + " - " + this.src2.toString() + "\n";
			break;
		case MUL:
			ans = this.destination.toString() + " = " + this.src1.toString() + " * " + this.src2.toString() + "\n";
			break;
		case DIV:
			ans = this.destination.toString() + " = " + this.src1.toString() + " / " + this.src2.toString() + "\n";
			break;
		case LT:
			ans = "IF_LT: " + this.src1.toString() + ", " + this.src2.toString() + ", " + "trueLabel"
					+ this.destination.toString() + "\n";
			break;
		case GT:
			ans = "IF_GT: " + this.src1.toString() + ", " + this.src2.toString() + ", " + "trueLabel"
					+ this.destination.toString() + "\n";
			break;
		case LTE:
			ans = "IF_LTE: " + this.src1.toString() + ", " + this.src2.toString() + ", " + "trueLabel"
					+ this.destination.toString() + "\n";
			break;
		case GTE:
			ans = "IF_GTE: " + this.src1.toString() + ", " + this.src2.toString() + ", " + "trueLabel"
					+ this.destination.toString() + "\n";
			break;
		case LABEL:
			ans = this.src1.toString() + "\n";
			break;
		case GOTO:
			ans = "GOTO: falseLabel" + this.destination.toString() + "\n";
			break;
		case EQUALS:
			ans = "IF_EQ: " + this.src1.toString() + ", " + this.src2.toString() + ", " + "trueLabel"
					+ this.destination.toString() + "\n";
			break;
		case NOTEQUALS:
			ans = "IF_NE: " + this.src1.toString() + ", " + this.src2.toString() + ", " + "trueLabel"
					+ this.destination.toString() + "\n";
			break;
		case IF: // destination for IF statement should be where it goes if true
			ans = "falseLabel" + this.destination.toString() + "\n";
			break;
		case START_WHILE:
			ans = "repeatLabel" + src1.toString() + "\n";
			break;
		case WHILE: // src1 is the repeating label while the statement continues to be true and the
					// destination is for where it goes after false
			ans = "GOTO: repeatLabel" + this.src1.toString() + "\n" + "falseLabel" + this.destination.toString() + "\n";
			break;
		default:
			ans = "ERROR there is no toString for this operation: " + op.name();
		}

		return ans;
	}
}
