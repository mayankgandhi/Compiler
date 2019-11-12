import java.util.ArrayList;

public class EvalParser {
	Scanner scan = new Scanner();
	String evalString;
	int tempID = 0;
	String threeAddressResult = "";
	ArrayList<Integer> IDs = new ArrayList<Integer>();
	ASTnode root = new ASTnode(TokenType.PROGRAM);
	private int parenCounter = 0;
	boolean expressionInside;
	public ArrayList<ThreeAddressObject> threeAddressObjects = new ArrayList<>();
	int tlabelID = 0; // Label id for true
	int flabelID = 0; // Label id for false
	int rlabelID = 0; // Label id for loops
	private String threeAddress = "";

	private Table localTable;
	Table globalTable = new Table();
	public ArrayList<CodeGenTuple> funcTuples = new ArrayList<CodeGenTuple>();
	private ArrayList<ThreeAddressObject> tacObjects = new ArrayList<ThreeAddressObject>();

	public ASTnode program(String eval) {
		localTable = globalTable;
		evalString = eval;
		tempID = 0;
		IDs.clear();
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
			ASTnode programNode = new ASTnode();
			root.type = TokenType.PROGRAM;
			ASTnode classInit = new ASTnode(TokenType.ID, nextToken.tokenVal);
			nextToken = lookahead();
			match(nextToken, TokenType.LEFTCURLY);
			classInit.stmts.add(prgm_list());
			programNode.stmts.add(classInit);
			nextToken = lookahead();
			match(nextToken, TokenType.RIGHTCURLY);
			root.stmts.add(programNode);
		} catch (Exception e) {
			System.out.println("ERROR: Syntax error : " + e.getMessage());
			System.exit(-1);
		}
		return root;
	}

	private ASTnode prgm_list() throws Exception {
		Token nextToken = lookahead();
		if (nextToken == null) {
			return null;
		}
		ASTnode result = new ASTnode();
		ASTnode temp = new ASTnode();
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

	private ASTnode func() throws Exception {
		Token nextToken = lookahead();
		match(nextToken, TokenType.VOID);
		nextToken = lookahead();
		match(nextToken, TokenType.ID);
		String funcName = nextToken.tokenVal;

		Table prev = localTable;
		localTable = new Table(prev);

		if (globalTable.find(nextToken) == null) {
			globalTable.add(funcName, SymbolType.FUNCTION);
		} else {
			System.out.println("ERROR: Function \'" + nextToken.tokenVal + "\' already defined");
			System.exit(1);
		}
		nextToken = lookahead();
		match(nextToken, TokenType.LEFTPAREN);
		nextToken = lookahead();
		match(nextToken, TokenType.RIGHTPAREN);
		nextToken = lookahead();
		match(nextToken, TokenType.LEFTCURLY);
		ASTnode result = stmt_list();

		generateTACForFunc(result, false);
		ArrayList<ThreeAddressObject> newObjects = new ArrayList<ThreeAddressObject>();
		for (ThreeAddressObject t : tacObjects) {
			newObjects.add(t);
		}
		tacObjects.clear();

		CodeGenTuple aTuple = new CodeGenTuple(newObjects, localTable, funcName);
		funcTuples.add(aTuple);

		nextToken = lookahead();
		match(nextToken, TokenType.RIGHTCURLY);
		return result;
	}

	private void generateTACForFunc(ASTnode aNode, boolean isOR) {

		try {
			if (aNode == null) {
				return;
			}

			if (aNode.type == TokenType.WHILE) {
				// threeAddress += "repeatLabel" + aNode.rLoc + "\n";
				Operand start = new Operand(aNode.rLoc);
				ThreeAddressObject startObject = new ThreeAddressObject(ThreeAddressObject.Operation.START_WHILE,
						start);
				tacObjects.add(startObject);
			}

			if (aNode.type == TokenType.OR) {
				isOR = true;
			}

			generateTACForFunc(aNode.left, isOR);

			if (aNode.type == TokenType.OR) {
				isOR = false;
			}

			generateTACForFunc(aNode.right, isOR);

			for (ASTnode theNode : aNode.stmts) {
				generateTACForFunc(theNode, isOR);
			}

			switch (aNode.type) {
			case NUM:
				Operand num_src1 = new Operand(aNode.value);
				Operand num_dest = new Operand(printIdOrLoc(aNode));
				ThreeAddressObject numObject = new ThreeAddressObject(ThreeAddressObject.Operation.NUM, num_src1,
						num_dest);
				tacObjects.add(numObject);
				break;
			case ASSIGN:
				Operand assign_src1 = new Operand(printIdOrLoc(aNode.right));
				Operand assign_dest = new Operand(printIdOrLoc(aNode.left));
				ThreeAddressObject assignObject = new ThreeAddressObject(ThreeAddressObject.Operation.ASSIGN,
						assign_src1, assign_dest);
				tacObjects.add(assignObject);
				break;
			case PLUS:
				Operand plus_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand plus_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand plus_dest = new Operand(printIdOrLoc(aNode));
				ThreeAddressObject plusObject = new ThreeAddressObject(ThreeAddressObject.Operation.PLUS, plus_src1,
						plus_src2, plus_dest);
				tacObjects.add(plusObject);
				break;
			case MINUS:
				Operand minus_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand minus_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand minus_dest = new Operand(printIdOrLoc(aNode));
				ThreeAddressObject minusObject = new ThreeAddressObject(ThreeAddressObject.Operation.MINUS, minus_src1,
						minus_src2, minus_dest);
				tacObjects.add(minusObject);
				break;
			case MUL:
				Operand mul_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand mul_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand mul_dest = new Operand(printIdOrLoc(aNode));
				ThreeAddressObject mulObject = new ThreeAddressObject(ThreeAddressObject.Operation.MUL, mul_src1,
						mul_src2, mul_dest);
				tacObjects.add(mulObject);
				break;
			case DIV:
				Operand div_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand div_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand div_dest = new Operand(printIdOrLoc(aNode));
				ThreeAddressObject divObject = new ThreeAddressObject(ThreeAddressObject.Operation.DIV, div_src1,
						div_src2, div_dest);
				tacObjects.add(divObject);
				break;
			case LT:
				Operand lt_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand lt_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand lt_dest = new Operand(aNode.tLoc);
				ThreeAddressObject ltObject = new ThreeAddressObject(ThreeAddressObject.Operation.LT, lt_src1, lt_src2,
						lt_dest);
				tacObjects.add(ltObject);
				Operand lt_goto = new Operand(aNode.fLoc);
				ThreeAddressObject ltGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, lt_goto);
				tacObjects.add(ltGotoObject);

				if (isOR) {
					Operand lt_falseLabel = new Operand("falselabel" + aNode.fLoc);
					ThreeAddressObject ltFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lt_falseLabel);
					tacObjects.add(ltFalseObject);
				} else {
					Operand lt_trueLabel = new Operand("truelabel" + aNode.tLoc);
					ThreeAddressObject ltTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lt_trueLabel);
					tacObjects.add(ltTrueObject);
				}
				break;
			case GT:
				Operand gt_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand gt_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand gt_dest = new Operand(aNode.tLoc);
				ThreeAddressObject gtObject = new ThreeAddressObject(ThreeAddressObject.Operation.GT, gt_src1, gt_src2,
						gt_dest);
				tacObjects.add(gtObject);

				Operand gt_goto = new Operand(aNode.fLoc);
				ThreeAddressObject gtGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, gt_goto);
				tacObjects.add(gtGotoObject);

				if (isOR) {
					Operand gt_falseLabel = new Operand("falselabel" + aNode.fLoc);
					ThreeAddressObject gtFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gt_falseLabel);
					tacObjects.add(gtFalseObject);
				} else {
					Operand gt_trueLabel = new Operand("truelabel" + aNode.tLoc);
					ThreeAddressObject gtTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gt_trueLabel);
					tacObjects.add(gtTrueObject);
				}
				break;
			case LTE:
				Operand lte_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand lte_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand lte_dest = new Operand(aNode.tLoc);
				ThreeAddressObject lteObject = new ThreeAddressObject(ThreeAddressObject.Operation.LTE, lte_src1,
						lte_src2, lte_dest);
				tacObjects.add(lteObject);
				Operand lte_goto = new Operand(aNode.fLoc);
				ThreeAddressObject lteGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, lte_goto);
				tacObjects.add(lteGotoObject);

				if (isOR) {
					Operand lte_falseLabel = new Operand("falselabel" + aNode.fLoc);
					ThreeAddressObject lteFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lte_falseLabel);
					tacObjects.add(lteFalseObject);
				} else {
					Operand lte_trueLabel = new Operand("truelabel" + aNode.tLoc);
					ThreeAddressObject lteTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lte_trueLabel);
					tacObjects.add(lteTrueObject);
				}
				break;
			case GTE:
				Operand gte_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand gte_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand gte_dest = new Operand(aNode.tLoc);
				ThreeAddressObject gteObject = new ThreeAddressObject(ThreeAddressObject.Operation.GTE, gte_src1,
						gte_src2, gte_dest);
				tacObjects.add(gteObject);

				Operand gte_goto = new Operand(aNode.fLoc);
				ThreeAddressObject gteGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, gte_goto);
				tacObjects.add(gteGotoObject);

				if (isOR) {
					Operand gte_falseLabel = new Operand("falselabel" + aNode.fLoc);
					ThreeAddressObject gteFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gte_falseLabel);
					tacObjects.add(gteFalseObject);
				} else {
					Operand gte_trueLabel = new Operand("truelabel" + aNode.tLoc);
					ThreeAddressObject gteTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gte_trueLabel);
					tacObjects.add(gteTrueObject);
				}
				break;
			case EQUALS:
				Operand equals_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand equals_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand equals_dest = new Operand(aNode.tLoc);
				ThreeAddressObject equalsObject = new ThreeAddressObject(ThreeAddressObject.Operation.EQUALS,
						equals_src1, equals_src2, equals_dest);
				tacObjects.add(equalsObject);

				Operand equals_goto = new Operand(aNode.fLoc);
				ThreeAddressObject equalsGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO,
						equals_goto);
				tacObjects.add(equalsGotoObject);

				if (isOR) {
					Operand equals_falseLabel = new Operand("falselabel" + aNode.fLoc);
					ThreeAddressObject equalsFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							equals_falseLabel);
					tacObjects.add(equalsFalseObject);
				} else {
					Operand equals_trueLabel = new Operand("truelabel" + aNode.tLoc);
					ThreeAddressObject equalsTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							equals_trueLabel);
					tacObjects.add(equalsTrueObject);
				}
				break;
			case NOTEQUALS:
				Operand not_equals_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand not_equals_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand not_equals_dest = new Operand(aNode.tLoc);
				ThreeAddressObject notEqualsObject = new ThreeAddressObject(ThreeAddressObject.Operation.NOTEQUALS,
						not_equals_src1, not_equals_src2, not_equals_dest);
				tacObjects.add(notEqualsObject);
				Operand not_equals_goto = new Operand(aNode.fLoc);
				ThreeAddressObject notEqualsGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO,
						not_equals_goto);
				tacObjects.add(notEqualsGotoObject);

				if (isOR) {
					Operand not_equals_falseLabel = new Operand("falselabel" + aNode.fLoc);
					ThreeAddressObject notEqualsFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							not_equals_falseLabel);
					tacObjects.add(notEqualsFalseObject);
				} else {
					Operand not_equals_trueLabel = new Operand("truelabel" + aNode.tLoc);
					ThreeAddressObject notEqualsTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							not_equals_trueLabel);
					tacObjects.add(notEqualsTrueObject);
				}
				break;
			case IF:
				Operand if_dest = new Operand(aNode.fLoc);
				ThreeAddressObject ifObject = new ThreeAddressObject(ThreeAddressObject.Operation.IF, if_dest);
				tacObjects.add(ifObject);
				break;
			case WHILE:
				Operand while_src1 = new Operand(aNode.rLoc);
				Operand while_dest = new Operand(aNode.fLoc);
				ThreeAddressObject whileObject = new ThreeAddressObject(ThreeAddressObject.Operation.WHILE, while_src1,
						while_dest);
				tacObjects.add(whileObject);
				break;
			}
		} catch (Exception e) {
			System.out.println("ERROR: Syntax error from TAC for FUNCS");
			System.exit(-1);
		}

	}

	private ASTnode var_decl(boolean isGlobal) throws Exception {
		Token nextToken = lookahead();
		match(nextToken, TokenType.INT);
		nextToken = lookahead();
		match(nextToken, TokenType.ID);

		if (isGlobal) {
			if (globalTable.findLocally(nextToken) == null) {
				globalTable.add(nextToken.tokenVal, SymbolType.INT);
			} else {
				throw new CompilerException(
						"ERROR: Function or variable \'" + nextToken.tokenVal + "\' already defined");
			}
		} else {
			if (localTable.findLocally(nextToken) == null) {
				localTable.add(nextToken.tokenVal, SymbolType.INT);
			} else {
				throw new CompilerException(
						"ERROR: Function or variable \'" + nextToken.tokenVal + "\' already defined");
			}
		}
		ASTnode aNode = new ASTnode(TokenType.ID, nextToken.tokenVal);
		return aNode;
	}

	private ASTnode stmt_list() throws Exception {
		ASTnode result = new ASTnode();
		ASTnode stmt = stmt();
		while (stmt != null) {
			result.stmts.add(stmt);
			stmt = stmt();
		}
		return result;
	}

	private ASTnode stmt() throws Exception {
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

	private ASTnode assignment() throws Exception {
		Token nextToken = lookahead();
		ASTnode result = new ASTnode();
		if (nextToken != null && nextToken.tokenType == TokenType.INT) {
			result = var_decl(false);
			nextToken = lookahead();
			if (nextToken.tokenType == TokenType.SEMICOLON) {
				match(nextToken, TokenType.SEMICOLON);
				return result;
			}
			match(nextToken, TokenType.ASSIGN);
			ASTnode mid = new ASTnode(TokenType.ASSIGN);
			ASTnode right = E();
			mid.left = result;
			mid.right = right;
			result = mid;
			result.loc = mid.left.loc;
			localTable.add(result.left.value, SymbolType.INT );
			nextToken = lookahead();
			match(nextToken, TokenType.SEMICOLON);
		} else if (nextToken != null && nextToken.tokenType == TokenType.ID) {
			match(nextToken, TokenType.ID);
			result = new ASTnode(TokenType.ID, nextToken.tokenVal);

			// changed tino
			///////////////////////////
			if (localTable.find(nextToken) == null && globalTable.find(nextToken)==null) {
				System.out.println("ERROR: variable \'" + nextToken.tokenVal + "\' not defined");
				System.exit(1);
			}

			///////////////////////////
			nextToken = lookahead();
			match(nextToken, TokenType.ASSIGN);
			ASTnode mid = new ASTnode(TokenType.ASSIGN);
			ASTnode right = E();
			mid.left = result;
			mid.right = right;
			result = mid;
			result.loc = mid.left.loc;
			localTable.add(result.left.value, SymbolType.INT);
			nextToken = lookahead();
			match(nextToken, TokenType.SEMICOLON);
		}
		tempID = 0;
		return result;
	}

	private ASTnode control_flow() throws Exception {
		Token nextToken = lookahead();
		ASTnode result;
		if (nextToken != null && nextToken.tokenType == TokenType.IF) {
			match(nextToken, TokenType.IF);
			ASTnode mid = new ASTnode(TokenType.IF);
			// Set labels for IF ASTnode
			mid.fLoc = flabelID;
			mid.tLoc = tlabelID;
			nextToken = lookahead();
			match(nextToken, TokenType.LEFTPAREN);
			// Evaluate inside expression
			result = boolCompare();
			if (result == null) {
				throw new CompilerException("No expression inside if");
			}
			nextToken = lookahead();
			match(nextToken, TokenType.RIGHTPAREN);
			nextToken = lookahead();
			// Reset temp ID
			tempID = 0;
			match(nextToken, TokenType.LEFTCURLY);
			ASTnode right = stmt_list();
			mid.left = result;
			mid.right = right;
			result = mid;
			nextToken = lookahead();
			match(nextToken, TokenType.RIGHTCURLY);
		} else if (nextToken != null && nextToken.tokenType == TokenType.WHILE) {
			match(nextToken, TokenType.WHILE);
			nextToken = lookahead();
			match(nextToken, TokenType.LEFTPAREN);
			ASTnode mid = new ASTnode(TokenType.WHILE);
			// Set labels for WHILE ASTnode
			mid.fLoc = flabelID;
			mid.tLoc = tlabelID;
			mid.rLoc = rlabelID++;
			// Evaluate inside expression
			result = boolCompare();
			if (result == null) {
				throw new CompilerException("No expression inside while");
			}
			nextToken = lookahead();
			match(nextToken, TokenType.RIGHTPAREN);
			nextToken = lookahead();
			// Reset temp ID
			tempID = 0;
			match(nextToken, TokenType.LEFTCURLY);
			ASTnode right = stmt_list();
			mid.left = result;
			mid.right = right;
			result = mid;
			nextToken = lookahead();
			match(nextToken, TokenType.RIGHTCURLY);
		} else {
			return null;
		}
		return result;
	}

	private ASTnode boolCompare() {
		ASTnode result = A();
		while (lookahead().tokenType == TokenType.AND || lookahead().tokenType == TokenType.OR) {
			Token nextToken = lookahead();
			if (nextToken.tokenType == TokenType.AND) {
				match(nextToken, TokenType.AND);
				ASTnode mid = new ASTnode(TokenType.AND);
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
				ASTnode mid = new ASTnode(TokenType.OR);
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

	private ASTnode A() {
		ASTnode result = B();
		while (lookahead().tokenType == TokenType.EQUALS || lookahead().tokenType == TokenType.NOTEQUALS) {
			Token nextToken = lookahead();
			if (nextToken.tokenType == TokenType.EQUALS) {
				match(nextToken, TokenType.EQUALS);
				ASTnode mid = new ASTnode(TokenType.EQUALS);
				ASTnode right = B();
				mid.left = result;
				mid.right = right;
				result = mid;
				result.fLoc = flabelID++;
				result.tLoc = tlabelID++;
			} else if (nextToken.tokenType == TokenType.NOTEQUALS) {
				match(nextToken, TokenType.NOTEQUALS);
				ASTnode mid = new ASTnode(TokenType.NOTEQUALS);
				ASTnode right = B();
				mid.left = result;
				mid.right = right;
				result = mid;
				result.fLoc = flabelID++;
				result.tLoc = tlabelID++;
			}
		}
		return result;
	}

	private ASTnode B() {
		ASTnode result = E();
		while (lookahead().tokenType == TokenType.LT || lookahead().tokenType == TokenType.GT
				|| lookahead().tokenType == TokenType.LTE || lookahead().tokenType == TokenType.GTE) {
			Token nextToken = lookahead();
			if (nextToken.tokenType == TokenType.LT) {
				match(nextToken, TokenType.LT);
				ASTnode mid = new ASTnode(TokenType.LT);
				ASTnode right = E();
				mid.left = result;
				mid.right = right;
				localTable.add("temp"+mid.left.loc, SymbolType.INT );
				localTable.add("temp"+mid.right.loc, SymbolType.INT );
				result = mid;
				result.fLoc = flabelID++;
				result.tLoc = tlabelID++;
			} else if (nextToken.tokenType == TokenType.GT) {
				match(nextToken, TokenType.GT);
				ASTnode mid = new ASTnode(TokenType.GT);
				ASTnode right = E();
				mid.left = result;
				mid.right = right;
				localTable.add("temp"+mid.left.loc, SymbolType.INT );
				localTable.add("temp"+mid.right.loc, SymbolType.INT );
				result = mid;
				result.fLoc = flabelID++;
				result.tLoc = tlabelID++;
			} else if (nextToken.tokenType == TokenType.LTE) {
				match(nextToken, TokenType.LTE);
				ASTnode mid = new ASTnode(TokenType.LTE);
				ASTnode right = E();
				mid.left = result;
				mid.right = right;
				localTable.add("temp"+mid.left.loc, SymbolType.INT );
				localTable.add("temp"+mid.right.loc, SymbolType.INT );
				result = mid;
				result.fLoc = flabelID++;
				result.tLoc = tlabelID++;
			} else if (nextToken.tokenType == TokenType.GTE) {
				match(nextToken, TokenType.GTE);
				ASTnode mid = new ASTnode(TokenType.GTE);
				ASTnode right = E();
				mid.left = result;
				mid.right = right;
				localTable.add("temp"+mid.left.loc, SymbolType.INT );
				localTable.add("temp"+mid.right.loc, SymbolType.INT );
				result = mid;
				result.fLoc = flabelID++;
				result.tLoc = tlabelID++;
			}
		}
		return result;
	}

	private ASTnode E() {
		ASTnode result = T();
		while (lookahead().tokenType == TokenType.PLUS || lookahead().tokenType == TokenType.MINUS) {
			Token nextToken = lookahead();
			if (nextToken.tokenType == TokenType.PLUS) {
				match(nextToken, TokenType.PLUS);
				ASTnode mid = new ASTnode(TokenType.PLUS);
				ASTnode right = T();
				mid.left = result;
				mid.right = right;
				localTable.add("temp"+mid.left.loc, SymbolType.INT );
				localTable.add("temp"+mid.right.loc, SymbolType.INT );
				result = mid;
				result.loc = tempID++;
				localTable.add("temp"+result.loc, SymbolType.INT );

			} else if (nextToken.tokenType == TokenType.MINUS) {
				match(nextToken, TokenType.MINUS);
				ASTnode mid = new ASTnode(TokenType.MINUS);
				ASTnode right = T();
				mid.left = result;
				mid.right = right;
				localTable.add("temp"+mid.left.loc, SymbolType.INT );
				localTable.add("temp"+mid.right.loc, SymbolType.INT );
				result = mid;
				result.loc = tempID++;
				localTable.add("temp"+result.loc, SymbolType.INT );
			}
		}
		return result;
	}

	private ASTnode T() {
		ASTnode result = F();
		while (lookahead().tokenType == TokenType.MUL || lookahead().tokenType == TokenType.DIV) {
			Token nextToken = lookahead();
			if (nextToken.tokenType == TokenType.MUL) {
				match(nextToken, TokenType.MUL);
				ASTnode mid = new ASTnode(TokenType.MUL);
				ASTnode right = F();
				mid.left = result;
				mid.right = right;
				localTable.add("temp"+mid.left.loc, SymbolType.INT );
				localTable.add("temp"+mid.right.loc, SymbolType.INT );
				result = mid;
				result.loc = tempID++;
				localTable.add("temp"+result.loc, SymbolType.INT );
			} else if (nextToken.tokenType == TokenType.DIV) {
				match(nextToken, TokenType.DIV);
				ASTnode mid = new ASTnode(TokenType.DIV);
				ASTnode right = F();
				mid.left = result;
				mid.right = right;
				localTable.add("temp"+mid.left.loc, SymbolType.INT );
				localTable.add("temp"+mid.right.loc, SymbolType.INT );
				result = mid;
				result.loc = tempID++;
				localTable.add("temp"+result.loc, SymbolType.INT );
			}
		}
		return result;
	}

	private ASTnode F() {
		ASTnode aNode;
		try {
			Token nextToken = lookahead();
			if (nextToken != null && nextToken.tokenType == TokenType.LEFTPAREN) {
				parenCounter++;
				match(nextToken, TokenType.LEFTPAREN);
				aNode = boolCompare();
				nextToken = lookahead();
				if (nextToken != null && nextToken.tokenType == TokenType.RIGHTPAREN) {
					parenCounter--;
					match(nextToken, TokenType.RIGHTPAREN);
					if (!expressionInside) {
						throw new CompilerException("No expression inside parentheses");
					}
				}
				return aNode;
			}
			if (nextToken != null && nextToken.tokenType == TokenType.NUM) {
				match(nextToken, TokenType.NUM);
				expressionInside = true;
				aNode = new ASTnode(TokenType.NUM, nextToken.tokenVal);
				localTable.add("temp"+aNode.loc,SymbolType.INT);
				aNode.loc = tempID++;
				return aNode;
			} else if (nextToken != null && nextToken.tokenType == TokenType.ID) {
				match(nextToken, TokenType.ID);
				aNode = new ASTnode(TokenType.ID, nextToken.tokenVal);
				if(localTable.find(nextToken)==null && 
				globalTable.find(nextToken)==null)
				{
					System.out.println("undefined variable");
				}
				return aNode;
			}
			if (parenCounter != 0) {
				throw new CompilerException("Parenthesis count wrong");
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
				throw new CompilerException("evalString is empty");
			}
		} catch (Exception e) {
			System.out.println("Input string not empty");
		}
		return 0;
	}

	public String getThreeAddr(String eval) {
		this.threeAddressResult = "";
		evalString = eval;
		tempID = 0;
		IDs.clear();
		E();
		return this.threeAddressResult;
	}

	public String emitTAC(ASTnode aNode, boolean isOR) {
		try {
			if (aNode == null) {
				return threeAddress;
			}
			if (aNode.type == TokenType.WHILE) {
				Operand start = new Operand(aNode.rLoc);
				ThreeAddressObject startObject = new ThreeAddressObject(ThreeAddressObject.Operation.START_WHILE,
						start);
				threeAddress += startObject.toString();
				threeAddressObjects.add(startObject);
			}
			if (aNode.type == TokenType.OR) {
				isOR = true;
			}
			emitTAC(aNode.left, isOR);
			if (aNode.type == TokenType.OR) {
				isOR = false;
			}
			emitTAC(aNode.right, isOR);
			for (ASTnode theNode : aNode.stmts) {
				emitTAC(theNode, isOR);
			}
			switch (aNode.type) {
			case NUM:
				Operand num_src1 = new Operand(aNode.value);
				Operand num_dest = new Operand(printIdOrLoc(aNode));
				ThreeAddressObject numObject = new ThreeAddressObject(ThreeAddressObject.Operation.NUM, num_src1,
						num_dest);
				threeAddress += numObject.toString();
				threeAddressObjects.add(numObject);
				break;
			case ASSIGN:
				Operand assign_src1 = new Operand(printIdOrLoc(aNode.right));
				Operand assign_dest = new Operand(printIdOrLoc(aNode.left));
				ThreeAddressObject assignObject = new ThreeAddressObject(ThreeAddressObject.Operation.ASSIGN,
						assign_src1, assign_dest);
				threeAddress += assignObject.toString();
				threeAddressObjects.add(assignObject);
				break;
			case PLUS:
				Operand plus_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand plus_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand plus_dest = new Operand(printIdOrLoc(aNode));
				ThreeAddressObject plusObject = new ThreeAddressObject(ThreeAddressObject.Operation.PLUS, plus_src1,
						plus_src2, plus_dest);
				threeAddress += plusObject.toString();
				threeAddressObjects.add(plusObject);
				break;
			case MINUS:
				Operand minus_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand minus_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand minus_dest = new Operand(printIdOrLoc(aNode));
				ThreeAddressObject minusObject = new ThreeAddressObject(ThreeAddressObject.Operation.MINUS, minus_src1,
						minus_src2, minus_dest);
				threeAddress += minusObject.toString();
				threeAddressObjects.add(minusObject);
				break;
			case MUL:
				Operand mul_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand mul_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand mul_dest = new Operand(printIdOrLoc(aNode));
				ThreeAddressObject mulObject = new ThreeAddressObject(ThreeAddressObject.Operation.MUL, mul_src1,
						mul_src2, mul_dest);
				threeAddress += mulObject.toString();
				threeAddressObjects.add(mulObject);
				break;
			case DIV:
				Operand div_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand div_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand div_dest = new Operand(printIdOrLoc(aNode));
				ThreeAddressObject divObject = new ThreeAddressObject(ThreeAddressObject.Operation.DIV, div_src1,
						div_src2, div_dest);
				threeAddress += divObject.toString();
				threeAddressObjects.add(divObject);
				break;
			case LT:
				Operand lt_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand lt_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand lt_dest = new Operand(aNode.tLoc);
				ThreeAddressObject ltObject = new ThreeAddressObject(ThreeAddressObject.Operation.LT, lt_src1, lt_src2,
						lt_dest);
				threeAddress += ltObject.toString();
				threeAddressObjects.add(ltObject);
				Operand lt_goto = new Operand(aNode.fLoc);
				ThreeAddressObject ltGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, lt_goto);
				threeAddress += ltGotoObject.toString();
				threeAddressObjects.add(ltGotoObject);
				if (isOR) {
					Operand lt_falseLabel = new Operand("falseLabel" + aNode.fLoc);
					ThreeAddressObject ltFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lt_falseLabel);
					threeAddress += ltFalseObject.toString();
					threeAddressObjects.add(ltFalseObject);
				} else {
					Operand lt_trueLabel = new Operand("trueLabel" + aNode.tLoc);
					ThreeAddressObject ltTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lt_trueLabel);
					threeAddress += ltTrueObject.toString();
					threeAddressObjects.add(ltTrueObject);
				}
				break;
			case GT:
				Operand gt_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand gt_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand gt_dest = new Operand(aNode.tLoc);
				ThreeAddressObject gtObject = new ThreeAddressObject(ThreeAddressObject.Operation.GT, gt_src1, gt_src2,
						gt_dest);
				threeAddress += gtObject.toString();
				threeAddressObjects.add(gtObject);
				Operand gt_goto = new Operand(aNode.fLoc);
				ThreeAddressObject gtGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, gt_goto);
				threeAddress += gtGotoObject.toString();
				threeAddressObjects.add(gtGotoObject);
				if (isOR) {
					Operand gt_falseLabel = new Operand("falseLabel" + aNode.fLoc);
					ThreeAddressObject gtFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gt_falseLabel);
					threeAddress += gtFalseObject.toString();
					threeAddressObjects.add(gtFalseObject);
				} else {
					Operand gt_trueLabel = new Operand("trueLabel" + aNode.tLoc);
					ThreeAddressObject gtTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gt_trueLabel);
					threeAddress += gtTrueObject.toString();
					threeAddressObjects.add(gtTrueObject);
				}
				break;
			case LTE:
				Operand lte_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand lte_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand lte_dest = new Operand(aNode.tLoc);
				ThreeAddressObject lteObject = new ThreeAddressObject(ThreeAddressObject.Operation.LTE, lte_src1,
						lte_src2, lte_dest);
				threeAddress += lteObject.toString();
				threeAddressObjects.add(lteObject);
				Operand lte_goto = new Operand(aNode.fLoc);
				ThreeAddressObject lteGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, lte_goto);
				threeAddress += lteGotoObject.toString();
				threeAddressObjects.add(lteGotoObject);
				if (isOR) {
					Operand lte_falseLabel = new Operand("falseLabel" + aNode.fLoc);
					ThreeAddressObject lteFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lte_falseLabel);
					threeAddress += lteFalseObject.toString();
					threeAddressObjects.add(lteFalseObject);
				} else {
					Operand lte_trueLabel = new Operand("trueLabel" + aNode.tLoc);
					ThreeAddressObject lteTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							lte_trueLabel);
					threeAddress += lteTrueObject.toString();
					threeAddressObjects.add(lteTrueObject);
				}
				break;
			case GTE:
				Operand gte_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand gte_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand gte_dest = new Operand(aNode.tLoc);
				ThreeAddressObject gteObject = new ThreeAddressObject(ThreeAddressObject.Operation.GTE, gte_src1,
						gte_src2, gte_dest);
				threeAddress += gteObject.toString();
				threeAddressObjects.add(gteObject);
				Operand gte_goto = new Operand(aNode.fLoc);
				ThreeAddressObject gteGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO, gte_goto);
				threeAddress += gteGotoObject.toString();
				threeAddressObjects.add(gteGotoObject);
				if (isOR) {
					Operand gte_falseLabel = new Operand("falseLabel" + aNode.fLoc);
					ThreeAddressObject gteFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gte_falseLabel);
					threeAddress += gteFalseObject.toString();
					threeAddressObjects.add(gteFalseObject);
				} else {
					Operand gte_trueLabel = new Operand("trueLabel" + aNode.tLoc);
					ThreeAddressObject gteTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							gte_trueLabel);
					threeAddress += gteTrueObject.toString();
					threeAddressObjects.add(gteTrueObject);
				}
				break;
			case EQUALS:
				Operand equals_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand equals_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand equals_dest = new Operand(aNode.tLoc);
				ThreeAddressObject equalsObject = new ThreeAddressObject(ThreeAddressObject.Operation.EQUALS,
						equals_src1, equals_src2, equals_dest);
				threeAddress += equalsObject.toString();
				threeAddressObjects.add(equalsObject);
				Operand equals_goto = new Operand(aNode.fLoc);
				ThreeAddressObject equalsGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO,
						equals_goto);
				threeAddress += equalsGotoObject.toString();
				threeAddressObjects.add(equalsGotoObject);
				if (isOR) {
					Operand equals_falseLabel = new Operand("falseLabel" + aNode.fLoc);
					ThreeAddressObject equalsFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							equals_falseLabel);
					threeAddress += equalsFalseObject.toString();
					threeAddressObjects.add(equalsFalseObject);
				} else {
					Operand equals_trueLabel = new Operand("trueLabel" + aNode.tLoc);
					ThreeAddressObject equalsTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							equals_trueLabel);
					threeAddress += equalsTrueObject.toString();
					threeAddressObjects.add(equalsTrueObject);
				}
				break;
			case NOTEQUALS:
				Operand not_equals_src1 = new Operand(printIdOrLoc(aNode.left));
				Operand not_equals_src2 = new Operand(printIdOrLoc(aNode.right));
				Operand not_equals_dest = new Operand(aNode.tLoc);
				ThreeAddressObject notEqualsObject = new ThreeAddressObject(ThreeAddressObject.Operation.NOTEQUALS,
						not_equals_src1, not_equals_src2, not_equals_dest);
				threeAddress += notEqualsObject.toString();
				threeAddressObjects.add(notEqualsObject);
				Operand not_equals_goto = new Operand(aNode.fLoc);
				ThreeAddressObject notEqualsGotoObject = new ThreeAddressObject(ThreeAddressObject.Operation.GOTO,
						not_equals_goto);
				threeAddress += notEqualsGotoObject.toString();
				threeAddressObjects.add(notEqualsGotoObject);
				if (isOR) {
					Operand not_equals_falseLabel = new Operand("falseLabel" + aNode.fLoc);
					ThreeAddressObject notEqualsFalseObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							not_equals_falseLabel);
					threeAddress += notEqualsFalseObject.toString();
					threeAddressObjects.add(notEqualsFalseObject);
				} else {
					Operand not_equals_trueLabel = new Operand("trueLabel" + aNode.tLoc);
					ThreeAddressObject notEqualsTrueObject = new ThreeAddressObject(ThreeAddressObject.Operation.LABEL,
							not_equals_trueLabel);
					threeAddress += notEqualsTrueObject.toString();
					threeAddressObjects.add(notEqualsTrueObject);
				}
				break;
			case IF:
				Operand if_dest = new Operand(aNode.fLoc);
				ThreeAddressObject ifObject = new ThreeAddressObject(ThreeAddressObject.Operation.IF, if_dest);
				threeAddress += ifObject.toString();
				threeAddressObjects.add(ifObject);
				break;
			case WHILE:
				Operand while_src1 = new Operand(aNode.rLoc);
				Operand while_dest = new Operand(aNode.fLoc);
				ThreeAddressObject whileObject = new ThreeAddressObject(ThreeAddressObject.Operation.WHILE, while_src1,
						while_dest);
				threeAddress += whileObject.toString();
				threeAddressObjects.add(whileObject);
				break;
			}
		} catch (Exception e) {
			System.out.println("ERROR: Syntax error");
			System.exit(-1);
		}
		return threeAddress;
	}

	private String printIdOrLoc(ASTnode aNode) {
		if (aNode.type == TokenType.ID) {
			return aNode.value;
		} else {
			return "temp" + aNode.loc;
		}
	}

	private Token lookahead() {

		try {
			if (evalString.isEmpty()) {
				return null;
			}
			Token aToken = (Token) scan.extractToken(new StringBuilder(evalString));
			if (aToken == null) {
				System.out.println(evalString);
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
				throw new CompilerException("Token is null");
			}
			while (Character.isWhitespace(evalString.charAt(0))) {
				evalString = evalString.substring(1);
			}
			if (aToken.tokenType == expectedToken) {
				evalString = evalString.substring(aToken.tokenVal.length());
			} else {
				throw new CompilerException(
						"Unexpected token type: " + aToken.tokenType + " , Expected Type: " + expectedToken);
			}
		} catch (Exception e) {
			System.out.println("ERROR: Syntax error - " + e.getMessage());
			System.exit(-1);
		}
	}

	public static void main(String args[]) {
		EvalParser parser = new EvalParser();
		String eval = "private class test { int i; int y; void main2(){ int i; int y;} }";
		ASTnode root = parser.program(eval);
		System.out.println("---------");
		System.out.println(parser.emitTAC(root, false));
		parser.localTable.printWholeTable();
	}
}