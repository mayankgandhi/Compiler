import java.util.ArrayList;
import java.util.TreeMap;

public class EvalParser {
	Scanner scan = new Scanner();
	String evalString;
	ASTNode root = new ASTNode(TokenType.PROGRAM);
	int parenCounter = 0;
	boolean expressionInside;
	ArrayList<ThreeAddressObject> threeAddressObjects = new ArrayList<>();
	ArrayList<ThreeAddressObject> tacObjects = new ArrayList<>();
	int tempID = 0; // Label for location
	int tlabelID = 0; // Label id for true
	int flabelID = 0; // Label id for false
	int rlabelID = 0; // Label id for loops
	Table globalTable = new Table(null);
	Table localTable = new Table(null);

	public ASTNode program(String eval) {
		evalString = eval;
		tempID = 0;
		try {
			Token nextToken = lookahead();
			if (nextToken.tokenType == TokenType.PUBLIC) {
				match(nextToken, TokenType.PUBLIC);
			} else if (nextToken.tokenType == TokenType.PRIVATE) {
				match(nextToken, TokenType.PRIVATE);
			}
			nextToken = lookahead();
			match(nextToken, TokenType.CLASS);
			nextToken = lookahead();
			match(nextToken, TokenType.ID);
			ASTNode programASTNode = new ASTNode();
			root.type = TokenType.PROGRAM;
			ASTNode classInit = new ASTNode(TokenType.ID, nextToken.tokenVal);
			nextToken = lookahead();
			match(nextToken, TokenType.OBRA);
			classInit.stmts.add(prgm_list());
			programASTNode.stmts.add(classInit);
			nextToken = lookahead();
			match(nextToken, TokenType.CBRA);
			root.stmts.add(programASTNode);
		} catch (Exception e) {
			System.out.println("ERROR: Syntax error" + e.getMessage());
			System.exit(-1);
		}
		return root;
	}

	private ASTNode prgm_list() throws Exception {
		Token nextToken = lookahead();
		if (nextToken == null) {
			return null;
		}
		ASTNode result = new ASTNode();
		ASTNode temp = new ASTNode();
		switch (nextToken.tokenType) {
		case INT:
			temp = var_decl(true);
			nextToken = lookahead();
			match(nextToken, TokenType.SEMICOLON);
			break;
		case VOID:
			temp = func();
			break;
		}
		while (temp != null) {
			nextToken = lookahead();
			result.stmts.add(temp);
			switch (nextToken.tokenType) {
			case INT:
				temp = var_decl(true);
				nextToken = lookahead();
				match(nextToken, TokenType.SEMICOLON);
				break;
			case VOID:
				temp = func();
				break;
			default:
				temp = null;
				break;
			}
		}
		return result;
	}

	private ASTNode func() throws Exception {
		Token nextToken = lookahead();
		match(nextToken, TokenType.VOID);
		nextToken = lookahead();
		match(nextToken, TokenType.ID);
		String funcName = nextToken.tokenVal;
		localTable = new Table(null);
		if (globalTable.find(nextToken) == null) {
			Symbol aFunc = new Symbol();
			aFunc.setType(Symbol.SymbolType.FUNC);
		} else {
			System.out.println("ERROR: Function \'" + nextToken.tokenVal + "\' already defined");
		}
		nextToken = lookahead();
		match(nextToken, TokenType.OP);
		nextToken = lookahead();
		match(nextToken, TokenType.CP);
		nextToken = lookahead();
		match(nextToken, TokenType.OBRA);
		ASTNode result = stmt_list();
		generateTACForFunc(result, false);
		ArrayList<ThreeAddressObject> newObjects = new ArrayList<>();
		for (ThreeAddressObject t : tacObjects) {
			newObjects.add(new ThreeAddressObject(t));
		}
		tacObjects.clear();
		nextToken = lookahead();
		match(nextToken, TokenType.CBRA);
		return result;
	}

	private ASTNode var_decl(boolean isGlobal) throws Exception {
		Token nextToken = lookahead();
		match(nextToken, TokenType.INT);
		nextToken = lookahead();
		match(nextToken, TokenType.ID);
		if (isGlobal) {
			if (globalTable.find(nextToken) == null) {
				Symbol s = new Symbol();
				s.setType(Symbol.SymbolType.INT);
				globalTable.add(nextToken.tokenVal, s);
			} else {
				System.out.println("ERROR: Variable \'" + nextToken.tokenVal + "\' already defined");
				// throw new Exception( "Variable " + nextToken.tokenVal + " already defined" );
			}
		} else {
			if (localTable.find(nextToken) == null) {
				Symbol s = new Symbol();
				s.setType(Symbol.SymbolType.INT);
				localTable.add(nextToken.tokenVal, s);
			} else {
				System.out.println("ERROR: Variable \'" + nextToken.tokenVal + "\' already defined");
				// throw new Exception( "Variable " + nextToken.tokenVal + " already defined" );
			}
		}
		ASTNode aASTNode = new ASTNode(TokenType.ID, nextToken.tokenVal);
		return aASTNode;
	}

	private ASTNode stmt_list() throws Exception {
		ASTNode result = new ASTNode();
		ASTNode stmt = stmt();
		while (stmt != null) {
			result.stmts.add(stmt);
			stmt = stmt();
		}
		return result;
	}

	private ASTNode stmt() throws Exception {
		Token nextToken = lookahead();
		if (nextToken == null) {
			return null;
		}
		switch (nextToken.tokenType) {
		case INT:
		case ID:
			return assignment();
		case IF:
		case WHILE:
			return control_flow();
		}
		return null;
	}

	private ASTNode assignment() throws Exception {
		Token nextToken = lookahead();
		ASTNode result = new ASTNode();
		if (nextToken != null && nextToken.tokenType == TokenType.INT) {
			result = var_decl(false);
			nextToken = lookahead();
			if (nextToken.tokenType == TokenType.SEMICOLON) {
				match(nextToken, TokenType.SEMICOLON);
				return result;
			}
			match(nextToken, TokenType.EQ);
			ASTNode mid = new ASTNode(TokenType.EQ);
			ASTNode right = E();
			mid.left = result;
			mid.right = right;
			result = mid;
			result.loc = mid.left.loc;
			nextToken = lookahead();
			match(nextToken, TokenType.SEMICOLON);
		} else if (nextToken != null && nextToken.tokenType == TokenType.ID) {
			match(nextToken, TokenType.ID);
			if (localTable.find(nextToken) == null) {
				if (globalTable.find(nextToken) == null) {
					System.out.println("ERROR: Undefined variable: \'" + nextToken.tokenVal + "\'");
				}
			}
			result = new ASTNode(TokenType.ID, nextToken.tokenVal);
			nextToken = lookahead();
			match(nextToken, TokenType.EQ);
			ASTNode mid = new ASTNode(TokenType.EQ);
			ASTNode right = E();
			mid.left = result;
			mid.right = right;
			result = mid;
			result.loc = mid.left.loc;
			nextToken = lookahead();
			match(nextToken, TokenType.SEMICOLON);
		}
		tempID = 0;
		return result;
	}

	private ASTNode control_flow() throws Exception {
		Token nextToken = lookahead();
		ASTNode result;
		if (nextToken != null && nextToken.tokenType == TokenType.IF) {
			match(nextToken, TokenType.IF);
			ASTNode mid = new ASTNode(TokenType.IF);
			// Set labels for IF ASTNode
			mid.fLoc = flabelID;
			mid.tLoc = tlabelID;
			nextToken = lookahead();
			match(nextToken, TokenType.OP);
			// Evaluate inside expression
			result = boolCompare();
			if (result == null) {
				throw new Exception("No expression inside if");
			}
			nextToken = lookahead();
			match(nextToken, TokenType.CP);
			nextToken = lookahead();
			// Reset temp ID
			tempID = 0;
			match(nextToken, TokenType.OBRA);
			ASTNode right = stmt_list();
			mid.left = result;
			mid.right = right;
			result = mid;
			nextToken = lookahead();
			match(nextToken, TokenType.CBRA);
		} else if (nextToken != null && nextToken.tokenType == TokenType.WHILE) {
			match(nextToken, TokenType.WHILE);
			nextToken = lookahead();
			match(nextToken, TokenType.OP);
			ASTNode mid = new ASTNode(TokenType.WHILE);
			// Set labels for WHILE ASTNode
			mid.fLoc = flabelID;
			mid.tLoc = tlabelID;
			mid.rLoc = rlabelID++;
			// Evaluate inside expression
			result = boolCompare();
			if (result == null) {
				throw new Exception("No expression inside while");
			}
			nextToken = lookahead();
			match(nextToken, TokenType.CP);
			nextToken = lookahead();
			// Reset temp ID
			tempID = 0;
			match(nextToken, TokenType.OBRA);
			ASTNode right = stmt_list();
			mid.left = result;
			mid.right = right;
			result = mid;
			nextToken = lookahead();
			match(nextToken, TokenType.CBRA);
		} else {
			return null;
		}
		return result;
	}

	private ASTNode boolCompare() {
		ASTNode result = A();
		while (lookahead().tokenType == TokenType.AND || lookahead().tokenType == TokenType.OR) {
			Token nextToken = lookahead();
			if (nextToken.tokenType == TokenType.AND) {
				match(nextToken, TokenType.AND);
				ASTNode mid = new ASTNode(TokenType.AND);
				mid.left = result;
				mid.right = A();
				// Set children labels
				mid.left.tLoc = mid.right.tLoc;
				mid.left.fLoc = mid.fLoc;
				mid.right.tLoc = mid.tLoc;
				mid.right.fLoc = mid.fLoc;
				// Set AND labels
				result = mid;
				result.fLoc = flabelID;
				result.tLoc = tlabelID;
			} else if (nextToken.tokenType == TokenType.OR) {
				match(nextToken, TokenType.OR);
				ASTNode mid = new ASTNode(TokenType.OR);
				mid.left = result;
				mid.right = A();
				// Set children labels
				mid.left.tLoc = mid.tLoc;
				mid.left.fLoc = mid.right.fLoc;
				mid.right.tLoc = mid.tLoc;
				mid.right.fLoc = mid.fLoc;
				// Set OR labels
				result = mid;
				result.fLoc = flabelID;
				result.tLoc = tlabelID;
			}
		}
		return result;
	}

	private ASTNode A() {
		ASTNode result = B();
		while (lookahead().tokenType == TokenType.EQ || lookahead().tokenType == TokenType.NOEQ) {
			Token nextToken = lookahead();
			if (nextToken.tokenType == TokenType.EQ) {
				match(nextToken, TokenType.EQ);
				ASTNode mid = new ASTNode(TokenType.EQ);
				ASTNode right = B();
				mid.left = result;
				mid.right = right;
				result = mid;
				result.fLoc = flabelID++;
				result.tLoc = tlabelID++;
			} else if (nextToken.tokenType == TokenType.NOEQ) {
				match(nextToken, TokenType.NOEQ);
				ASTNode mid = new ASTNode(TokenType.NOEQ);
				ASTNode right = B();
				mid.left = result;
				mid.right = right;
				result = mid;
				result.fLoc = flabelID++;
				result.tLoc = tlabelID++;
			}
		}
		return result;
	}

	private ASTNode B() {
		ASTNode result = E();
		while (lookahead().tokenType == TokenType.LT || lookahead().tokenType == TokenType.GT
				|| lookahead().tokenType == TokenType.LTE || lookahead().tokenType == TokenType.GTE) {
			Token nextToken = lookahead();
			if (nextToken.tokenType == TokenType.LT) {
				match(nextToken, TokenType.LT);
				ASTNode mid = new ASTNode(TokenType.LT);
				ASTNode right = E();
				mid.left = result;
				mid.right = right;
				Symbol s = new Symbol();
				s.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.left.loc, s);
				Symbol s2 = new Symbol();
				s2.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.right.loc, s2);
				result = mid;
				result.fLoc = flabelID++;
				result.tLoc = tlabelID++;
			} else if (nextToken.tokenType == TokenType.GT) {
				match(nextToken, TokenType.GT);
				ASTNode mid = new ASTNode(TokenType.GT);
				ASTNode right = E();
				mid.left = result;
				mid.right = right;
				Symbol s = new Symbol();
				s.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.left.loc, s);
				Symbol s2 = new Symbol();
				s2.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.right.loc, s2);
				result = mid;
				result.fLoc = flabelID++;
				result.tLoc = tlabelID++;
			} else if (nextToken.tokenType == TokenType.LTE) {
				match(nextToken, TokenType.LTE);
				ASTNode mid = new ASTNode(TokenType.LTE);
				ASTNode right = E();
				mid.left = result;
				mid.right = right;
				Symbol s = new Symbol();
				s.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.left.loc, s);
				Symbol s2 = new Symbol();
				s2.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.right.loc, s2);
				result = mid;
				result.fLoc = flabelID++;
				result.tLoc = tlabelID++;
			} else if (nextToken.tokenType == TokenType.GTE) {
				match(nextToken, TokenType.GTE);
				ASTNode mid = new ASTNode(TokenType.GTE);
				ASTNode right = E();
				mid.left = result;
				mid.right = right;
				Symbol s = new Symbol();
				s.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.left.loc, s);
				Symbol s2 = new Symbol();
				s2.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.right.loc, s2);
				result = mid;
				result.fLoc = flabelID++;
				result.tLoc = tlabelID++;
			}
		}
		return result;
	}

	private ASTNode E() {
		ASTNode result = T();
		while (lookahead().tokenType == TokenType.PLUS || lookahead().tokenType == TokenType.MINUS) {
			Token nextToken = lookahead();
			if (nextToken.tokenType == TokenType.PLUS) {
				match(nextToken, TokenType.PLUS);
				ASTNode mid = new ASTNode(TokenType.PLUS);
				ASTNode right = T();
				mid.left = result;
				mid.right = right;
				Symbol s = new Symbol();
				s.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.left.loc, s);
				Symbol s2 = new Symbol();
				s2.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.right.loc, s2);
				result = mid;
				result.loc = tempID++;
				Symbol s3 = new Symbol();
				s3.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + result.loc, s3);
			} else if (nextToken.tokenType == TokenType.MINUS) {
				match(nextToken, TokenType.MINUS);
				ASTNode mid = new ASTNode(TokenType.MINUS);
				ASTNode right = T();
				mid.left = result;
				mid.right = right;
				Symbol s = new Symbol();
				s.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.left.loc, s);
				Symbol s2 = new Symbol();
				s2.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.right.loc, s2);
				result = mid;
				result.loc = tempID++;
				Symbol s3 = new Symbol();
				s3.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + result.loc, s3);
			}
		}
		return result;
	}

	private ASTNode T() {
		ASTNode result = F();
		while (lookahead().tokenType == TokenType.MUL || lookahead().tokenType == TokenType.DIV) {
			Token nextToken = lookahead();
			if (nextToken.tokenType == TokenType.MUL) {
				match(nextToken, TokenType.MUL);
				ASTNode mid = new ASTNode(TokenType.MUL);
				ASTNode right = F();
				mid.left = result;
				mid.right = right;
				Symbol s = new Symbol();
				s.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.left.loc, s);
				Symbol s2 = new Symbol();
				s2.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.right.loc, s2);
				result = mid;
				result.loc = tempID++;
				Symbol s3 = new Symbol();
				s3.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + result.loc, s3);
			} else if (nextToken.tokenType == TokenType.DIV) {
				match(nextToken, TokenType.DIV);
				ASTNode mid = new ASTNode(TokenType.DIV);
				ASTNode right = F();
				mid.left = result;
				mid.right = right;
				Symbol s = new Symbol();
				s.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.left.loc, s);
				Symbol s2 = new Symbol();
				s2.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + mid.right.loc, s2);
				result = mid;
				result.loc = tempID++;
				Symbol s3 = new Symbol();
				s3.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + result.loc, s3);
			}
		}
		return result;
	}

	private ASTNode F() {
		ASTNode aASTNode;
		try {
			Token nextToken = lookahead();
			if (nextToken != null && nextToken.tokenType == TokenType.OP) {
				parenCounter++;
				match(nextToken, TokenType.OP);
				aASTNode = boolCompare();
				nextToken = lookahead();
				if (nextToken != null && nextToken.tokenType == TokenType.CP) {
					parenCounter--;
					match(nextToken, TokenType.CP);
					if (!expressionInside) {
						throw new Exception("No expression inside parentheses");
					}
				}
				return aASTNode;
			}
			if (nextToken != null && nextToken.tokenType == TokenType.NUM) {
				match(nextToken, TokenType.NUM);
				expressionInside = true;
				aASTNode = new ASTNode(TokenType.NUM, nextToken.tokenVal);
				Symbol s = new Symbol();
				s.setType(Symbol.SymbolType.INT);
				localTable.add("temp" + aASTNode.loc, s);
				aASTNode.loc = tempID++;
				return aASTNode;
			} else if (nextToken != null && nextToken.tokenType == TokenType.ID) {
				match(nextToken, TokenType.ID);
				if (localTable.find(nextToken) == null) {
					if (globalTable.find(nextToken) == null) {
						System.out.println("ERROR: Undefined variable: \'" + nextToken.tokenVal + "\'");
					}
				}
				aASTNode = new ASTNode(TokenType.ID, nextToken.tokenVal);
				return aASTNode;
			}
			if (parenCounter != 0) {
				throw new Exception("Parenthesis count wrong");
			}
		} catch (Exception e) {
			System.out.println("ERROR: Syntax error - " + e.getMessage());
			expressionInside = false;
			System.exit(-1);
		}
		return null;
	}

	/****************************************/
	public int evaluateExpression(String eval) {
		evalString = eval;
		try {
			if (!evalString.isEmpty()) {
				throw new Exception();
			}
		} catch (Exception e) {
			System.out.println("Input string not empty");
		}
		return 0;
	}

	public String getThreeAddr(ASTNode aASTNode, boolean isOR) {
		generateTAC(aASTNode, isOR);
		StringBuilder theFinishedThreeAddress = new StringBuilder();
		for (ThreeAddressObject aTao : threeAddressObjects) {
			theFinishedThreeAddress.append(aTao.toString());
		}
		return theFinishedThreeAddress.toString();
	}

	private void generateTAC(ASTNode aASTNode, boolean isOR) {
		try {
			if (aASTNode == null) {
				return;
			}
			if (aASTNode.type == TokenType.WHILE) {
				Operand start = new Operand(aASTNode.rLoc);
				ThreeAddressObject startObject = new ThreeAddressObject(ThreeAddressObject.Operation.START_WHILE,
						start);
				threeAddressObjects.add(startObject);
			}
			if (aASTNode.type == TokenType.OR) {
				isOR = true;
			}
			generateTAC(aASTNode.left, isOR);
			if (aASTNode.type == TokenType.OR) {
				isOR = false;
			}
			generateTAC(aASTNode.right, isOR);
			for (ASTNode theASTNode : aASTNode.stmts) {
				generateTAC(theASTNode, isOR);
			}
			switch (aASTNode.type) {
			case NUM:
				Operand num_src1 = new Operand(aASTNode.value);
				Operand num_dest = new Operand(printIdOrLoc(aASTNode));
				ThreeAddressObject numObject = new ThreeAddressObject(ThreeAddressObject.Operation.NUM, num_src1,
						num_dest);
				threeAddressObjects.add(numObject);
				break;
			case EQ:
				Operand assign_src1 = new Operand(printIdOrLoc(aASTNode.right));
				Operand assign_dest = new Operand(printIdOrLoc(aASTNode.left));
				ThreeAddressObject assignObject = new ThreeAddressObject(ThreeAddressObject.Operation.ASSIGN,
						assign_src1, assign_dest);
				threeAddressObjects.add(assignObject);
				break;
			case PLUS:
				Operand plus_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand plus_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand plus_dest = new Operand(printIdOrLoc(aASTNode));
				ThreeAddressObject plusObject = new ThreeAddressObject(ThreeAddressObject.Operation.PLUS, plus_src1,
						plus_src2, plus_dest);
				threeAddressObjects.add(plusObject);
				break;
			case MINUS:
				Operand minus_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand minus_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand minus_dest = new Operand(printIdOrLoc(aASTNode));
				ThreeAddressObject minusObject = new ThreeAddressObject(ThreeAddressObject.Operation.MINUS, minus_src1,
						minus_src2, minus_dest);
				threeAddressObjects.add(minusObject);
				break;
			case MUL:
				Operand mul_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand mul_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand mul_dest = new Operand(printIdOrLoc(aASTNode));
				ThreeAddressObject mulObject = new ThreeAddressObject(ThreeAddressObject.Operation.MUL, mul_src1,
						mul_src2, mul_dest);
				threeAddressObjects.add(mulObject);
				break;
			case DIV:
				Operand div_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand div_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand div_dest = new Operand(printIdOrLoc(aASTNode));
				ThreeAddressObject divObject = new ThreeAddressObject(ThreeAddressObject.Operation.DIV, div_src1,
						div_src2, div_dest);
				threeAddressObjects.add(divObject);
				break;
			case LT:
				Operand lt_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand lt_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand lt_dest = new Operand(aASTNode.tLoc);
				ThreeAddressObject ltObject = new ThreeAddressObject(ThreeAddressObject.Operation.LT, lt_src1, lt_src2,
						lt_dest);
				threeAddressObjects.add(ltObject);
				Operand lt_goto = new Operand(aASTNode.fLoc);
				ThreeAddressObject ltGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, lt_goto);
				threeAddressObjects.add(ltGotoObject);
				if (isOR) {
					Operand lt_falseLabel = new Operand("falselabel" + aASTNode.fLoc);
					ThreeAddressObject ltFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lt_falseLabel);
					threeAddressObjects.add(ltFalseObject);
				} else {
					Operand lt_trueLabel = new Operand("truelabel" + aASTNode.tLoc);
					ThreeAddressObject ltTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lt_trueLabel);
					threeAddressObjects.add(ltTrueObject);
				}
				break;
			case GT:
				Operand gt_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand gt_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand gt_dest = new Operand(aASTNode.tLoc);
				ThreeAddressObject gtObject = new ThreeAddressObject(ThreeAddressObject.Operation.GT, gt_src1, gt_src2,
						gt_dest);
				threeAddressObjects.add(gtObject);
				Operand gt_goto = new Operand(aASTNode.fLoc);
				ThreeAddressObject gtGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, gt_goto);
				threeAddressObjects.add(gtGotoObject);
				if (isOR) {
					Operand gt_falseLabel = new Operand("falselabel" + aASTNode.fLoc);
					ThreeAddressObject gtFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gt_falseLabel);
					threeAddressObjects.add(gtFalseObject);
				} else {
					Operand gt_trueLabel = new Operand("truelabel" + aASTNode.tLoc);
					ThreeAddressObject gtTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gt_trueLabel);
					threeAddressObjects.add(gtTrueObject);
				}
				break;
			case LTE:
				Operand lte_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand lte_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand lte_dest = new Operand(aASTNode.tLoc);
				ThreeAddressObject lteObject = new ThreeAddressObject(ThreeAddressObject.Operation.LTE, lte_src1,
						lte_src2, lte_dest);
				threeAddressObjects.add(lteObject);
				Operand lte_goto = new Operand(aASTNode.fLoc);
				ThreeAddressObject lteGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, lte_goto);
				threeAddressObjects.add(lteGotoObject);
				if (isOR) {
					Operand lte_falseLabel = new Operand("falselabel" + aASTNode.fLoc);
					ThreeAddressObject lteFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lte_falseLabel);
					threeAddressObjects.add(lteFalseObject);
				} else {
					Operand lte_trueLabel = new Operand("truelabel" + aASTNode.tLoc);
					ThreeAddressObject lteTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lte_trueLabel);
					threeAddressObjects.add(lteTrueObject);
				}
				break;
			case GTE:
				Operand gte_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand gte_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand gte_dest = new Operand(aASTNode.tLoc);
				ThreeAddressObject gteObject = new ThreeAddressObject(ThreeAddressObject.Operation.GTE, gte_src1,
						gte_src2, gte_dest);
				threeAddressObjects.add(gteObject);
				Operand gte_goto = new Operand(aASTNode.fLoc);
				ThreeAddressObject gteGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, gte_goto);
				threeAddressObjects.add(gteGotoObject);
				if (isOR) {
					Operand gte_falseLabel = new Operand("falselabel" + aASTNode.fLoc);
					ThreeAddressObject gteFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gte_falseLabel);
					threeAddressObjects.add(gteFalseObject);
				} else {
					Operand gte_trueLabel = new Operand("truelabel" + aASTNode.tLoc);
					ThreeAddressObject gteTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gte_trueLabel);
					threeAddressObjects.add(gteTrueObject);
				}
				break;
			case EQ:
				Operand equals_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand equals_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand equals_dest = new Operand(aASTNode.tLoc);
				ThreeAddressObject equalsObject = new ThreeAddressObject(ThreeAddressObject.Operation.EQUALS,
						equals_src1, equals_src2, equals_dest);
				threeAddressObjects.add(equalsObject);
				Operand equals_goto = new Operand(aASTNode.fLoc);
				ThreeAddressObject equalsGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO,
						equals_goto);
				threeAddressObjects.add(equalsGotoObject);
				if (isOR) {
					Operand equals_falseLabel = new Operand("falselabel" + aASTNode.fLoc);
					ThreeAddressObject equalsFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							equals_falseLabel);
					threeAddressObjects.add(equalsFalseObject);
				} else {
					Operand equals_trueLabel = new Operand("truelabel" + aASTNode.tLoc);
					ThreeAddressObject equalsTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							equals_trueLabel);
					threeAddressObjects.add(equalsTrueObject);
				}
				break;
			case NOEQ:
				Operand not_equals_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand not_equals_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand not_equals_dest = new Operand(aASTNode.tLoc);
				ThreeAddressObject notEqualsObject = new ThreeAddressObject(ThreeAddressObject.Operation.NOTEQUALS,
						not_equals_src1, not_equals_src2, not_equals_dest);
				threeAddressObjects.add(notEqualsObject);
				Operand not_equals_goto = new Operand(aASTNode.fLoc);
				ThreeAddressObject notEqualsGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO,
						not_equals_goto);
				threeAddressObjects.add(notEqualsGotoObject);
				if (isOR) {
					Operand not_equals_falseLabel = new Operand("falselabel" + aASTNode.fLoc);
					ThreeAddressObject notEqualsFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							not_equals_falseLabel);
					threeAddressObjects.add(notEqualsFalseObject);
				} else {
					Operand not_equals_trueLabel = new Operand("truelabel" + aASTNode.tLoc);
					ThreeAddressObject notEqualsTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							not_equals_trueLabel);
					threeAddressObjects.add(notEqualsTrueObject);
				}
				break;
			case IF:
				Operand if_dest = new Operand(aASTNode.fLoc);
				ThreeAddressObject ifObject = new ThreeAddressObject(ThreeAddressObject.Operation.IF, if_dest);
				threeAddressObjects.add(ifObject);
				break;
			case WHILE:
				Operand while_src1 = new Operand(aASTNode.rLoc);
				Operand while_dest = new Operand(aASTNode.fLoc);
				ThreeAddressObject whileObject = new ThreeAddressObject(ThreeAddressObject.Operation.WHILE, while_src1,
						while_dest);
				threeAddressObjects.add(whileObject);
				break;
			}
		} catch (Exception e) {
			System.out.println("ERROR: Syntax error");
			System.exit(-1);
		}
	}

	private void generateTACForFunc(ASTNode aASTNode, boolean isOR) {
		try {
			if (aASTNode == null) {
				return;
			}
			if (aASTNode.type == TokenType.WHILE) {
				Operand start = new Operand(aASTNode.rLoc);
				ThreeAddressObject startObject = new ThreeAddressObject(ThreeAddressObject.Operation.START_WHILE,
						start);
				tacObjects.add(startObject);
			}
			if (aASTNode.type == TokenType.OR) {
				isOR = true;
			}
			generateTACForFunc(aASTNode.left, isOR);
			if (aASTNode.type == TokenType.OR) {
				isOR = false;
			}
			generateTACForFunc(aASTNode.right, isOR);
			for (ASTNode theASTNode : aASTNode.stmts) {
				generateTACForFunc(theASTNode, isOR);
			}
			switch (aASTNode.type) {
			case NUM:
				Operand num_src1 = new Operand(aASTNode.value);
				Operand num_dest = new Operand(printIdOrLoc(aASTNode));
				ThreeAddressObject numObject = new ThreeAddressObject(ThreeAddressObject.Operation.NUM, num_src1,
						num_dest);
				tacObjects.add(numObject);
				break;
			case ASSIGN:
				Operand assign_src1 = new Operand(printIdOrLoc(aASTNode.right));
				Operand assign_dest = new Operand(printIdOrLoc(aASTNode.left));
				ThreeAddressObject assignObject = new ThreeAddressObject(ThreeAddressObject.Operation.ASSIGN,
						assign_src1, assign_dest);
				tacObjects.add(assignObject);
				break;
			case PLUS:
				Operand plus_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand plus_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand plus_dest = new Operand(printIdOrLoc(aASTNode));
				ThreeAddressObject plusObject = new ThreeAddressObject(ThreeAddressObject.Operation.PLUS, plus_src1,
						plus_src2, plus_dest);
				tacObjects.add(plusObject);
				break;
			case MINUS:
				Operand minus_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand minus_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand minus_dest = new Operand(printIdOrLoc(aASTNode));
				ThreeAddressObject minusObject = new ThreeAddressObject(ThreeAddressObject.Operation.MINUS, minus_src1,
						minus_src2, minus_dest);
				tacObjects.add(minusObject);
				break;
			case MUL:
				Operand mul_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand mul_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand mul_dest = new Operand(printIdOrLoc(aASTNode));
				ThreeAddressObject mulObject = new ThreeAddressObject(ThreeAddressObject.Operation.MUL, mul_src1,
						mul_src2, mul_dest);
				tacObjects.add(mulObject);
				break;
			case DIV:
				Operand div_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand div_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand div_dest = new Operand(printIdOrLoc(aASTNode));
				ThreeAddressObject divObject = new ThreeAddressObject(ThreeAddressObject.Operation.DIV, div_src1,
						div_src2, div_dest);
				tacObjects.add(divObject);
				break;
			case LT:
				Operand lt_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand lt_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand lt_dest = new Operand(aASTNode.tLoc);
				ThreeAddressObject ltObject = new ThreeAddressObject(ThreeAddressObject.Operation.LT, lt_src1, lt_src2,
						lt_dest);
				tacObjects.add(ltObject);
				Operand lt_goto = new Operand(aASTNode.fLoc);
				ThreeAddressObject ltGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, lt_goto);
				tacObjects.add(ltGotoObject);
				if (isOR) {
					Operand lt_falseLabel = new Operand("falselabel" + aASTNode.fLoc);
					ThreeAddressObject ltFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lt_falseLabel);
					tacObjects.add(ltFalseObject);
				} else {
					Operand lt_trueLabel = new Operand("truelabel" + aASTNode.tLoc);
					ThreeAddressObject ltTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lt_trueLabel);
					tacObjects.add(ltTrueObject);
				}
				break;
			case GT:
				Operand gt_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand gt_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand gt_dest = new Operand(aASTNode.tLoc);
				ThreeAddressObject gtObject = new ThreeAddressObject(ThreeAddressObject.Operation.GT, gt_src1, gt_src2,
						gt_dest);
				tacObjects.add(gtObject);
				Operand gt_goto = new Operand(aASTNode.fLoc);
				ThreeAddressObject gtGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, gt_goto);
				tacObjects.add(gtGotoObject);
				if (isOR) {
					Operand gt_falseLabel = new Operand("falselabel" + aASTNode.fLoc);
					ThreeAddressObject gtFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gt_falseLabel);
					tacObjects.add(gtFalseObject);
				} else {
					Operand gt_trueLabel = new Operand("truelabel" + aASTNode.tLoc);
					ThreeAddressObject gtTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gt_trueLabel);
					tacObjects.add(gtTrueObject);
				}
				break;
			case LTE:
				Operand lte_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand lte_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand lte_dest = new Operand(aASTNode.tLoc);
				ThreeAddressObject lteObject = new ThreeAddressObject(ThreeAddressObject.Operation.LTE, lte_src1,
						lte_src2, lte_dest);
				tacObjects.add(lteObject);
				Operand lte_goto = new Operand(aASTNode.fLoc);
				ThreeAddressObject lteGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, lte_goto);
				tacObjects.add(lteGotoObject);
				if (isOR) {
					Operand lte_falseLabel = new Operand("falselabel" + aASTNode.fLoc);
					ThreeAddressObject lteFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lte_falseLabel);
					tacObjects.add(lteFalseObject);
				} else {
					Operand lte_trueLabel = new Operand("truelabel" + aASTNode.tLoc);
					ThreeAddressObject lteTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lte_trueLabel);
					tacObjects.add(lteTrueObject);
				}
				break;
			case GTE:
				Operand gte_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand gte_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand gte_dest = new Operand(aASTNode.tLoc);
				ThreeAddressObject gteObject = new ThreeAddressObject(ThreeAddressObject.Operation.GTE, gte_src1,
						gte_src2, gte_dest);
				tacObjects.add(gteObject);
				Operand gte_goto = new Operand(aASTNode.fLoc);
				ThreeAddressObject gteGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, gte_goto);
				tacObjects.add(gteGotoObject);
				if (isOR) {
					Operand gte_falseLabel = new Operand("falselabel" + aASTNode.fLoc);
					ThreeAddressObject gteFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gte_falseLabel);
					tacObjects.add(gteFalseObject);
				} else {
					Operand gte_trueLabel = new Operand("truelabel" + aASTNode.tLoc);
					ThreeAddressObject gteTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gte_trueLabel);
					tacObjects.add(gteTrueObject);
				}
				break;
			case EQ:
				Operand equals_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand equals_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand equals_dest = new Operand(aASTNode.tLoc);
				ThreeAddressObject equalsObject = new ThreeAddressObject(ThreeAddressObject.Operation.EQUALS,
						equals_src1, equals_src2, equals_dest);
				tacObjects.add(equalsObject);
				Operand equals_goto = new Operand(aASTNode.fLoc);
				ThreeAddressObject equalsGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO,
						equals_goto);
				tacObjects.add(equalsGotoObject);
				if (isOR) {
					Operand equals_falseLabel = new Operand("falselabel" + aASTNode.fLoc);
					ThreeAddressObject equalsFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							equals_falseLabel);
					tacObjects.add(equalsFalseObject);
				} else {
					Operand equals_trueLabel = new Operand("truelabel" + aASTNode.tLoc);
					ThreeAddressObject equalsTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							equals_trueLabel);
					tacObjects.add(equalsTrueObject);
				}
				break;
			case NOEQ:
				Operand not_equals_src1 = new Operand(printIdOrLoc(aASTNode.left));
				Operand not_equals_src2 = new Operand(printIdOrLoc(aASTNode.right));
				Operand not_equals_dest = new Operand(aASTNode.tLoc);
				ThreeAddressObject notEqualsObject = new ThreeAddressObject(ThreeAddressObject.Operation.NOTEQUALS,
						not_equals_src1, not_equals_src2, not_equals_dest);
				tacObjects.add(notEqualsObject);
				// threeAddress += "GOTO: falseLabel" + aASTNode.fLoc + "\n";
				Operand not_equals_goto = new Operand(aASTNode.fLoc);
				ThreeAddressObject notEqualsGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO,
						not_equals_goto);
				tacObjects.add(notEqualsGotoObject);
				if (isOR) {
					Operand not_equals_falseLabel = new Operand("falselabel" + aASTNode.fLoc);
					ThreeAddressObject notEqualsFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							not_equals_falseLabel);
					tacObjects.add(notEqualsFalseObject);
				} else {
					Operand not_equals_trueLabel = new Operand("truelabel" + aASTNode.tLoc);
					ThreeAddressObject notEqualsTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							not_equals_trueLabel);
					tacObjects.add(notEqualsTrueObject);
				}
				break;
			case IF:
				Operand if_dest = new Operand(aASTNode.fLoc);
				ThreeAddressObject ifObject = new ThreeAddressObject(ThreeAddressObject.Operation.IF, if_dest);
				tacObjects.add(ifObject);
				break;
			case WHILE:
				Operand while_src1 = new Operand(aASTNode.rLoc);
				Operand while_dest = new Operand(aASTNode.fLoc);
				ThreeAddressObject whileObject = new ThreeAddressObject(ThreeAddressObject.Operation.WHILE, while_src1,
						while_dest);
				tacObjects.add(whileObject);
				break;
			}
		} catch (Exception e) {
			System.out.println("ERROR: Syntax error");
			System.exit(-1);
		}
	}

	private String printIdOrLoc(ASTNode aASTNode) {
		if (aASTNode.type == TokenType.ID) {
			return aASTNode.value;
		} else {
			return "temp" + aASTNode.loc;
		}
	}

	private Token lookahead() {
		try {
			if (evalString.isEmpty()) {
				return null;
			}
			Token aToken = scan.extractToken(new StringBuilder(evalString));
			if (aToken == null) {
				throw new Exception("Token is null");
			} else {
				return aToken;
			}
		} catch (Exception e) {
			System.out.println("ERROR: Syntax error - " + e.getMessage());
			System.exit(-1);
		}
		return null;
	}

	private void match(Token aToken, TokenType expectedToken) {
		try {
			if (aToken == null) {
				throw new Exception("Token is null");
			}
			while (Character.isWhitespace(evalString.charAt(0))) {
				evalString = evalString.substring(1);
			}
			if (aToken.tokenType == expectedToken) {
				evalString = evalString.substring(aToken.tokenVal.length());
			} else {
				throw new Exception(
						"Unexpected token type: " + aToken.tokenType + " , Expected Type: " + expectedToken);
			}
		} catch (Exception e) {
			System.out.println("ERROR: Syntax error - " + e.getMessage());
			System.exit(-1);
		}
	}

}