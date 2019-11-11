/*
 * Any leaf ASTnode there should only be one child, this will be the left ASTnode
 * Any interior ASTnode should have two children, what is left of the operation and what is to the right
 */

import java.util.LinkedList;

public class ASTnode {
    ASTnode left = null;
    ASTnode right = null;
    String value;
    LinkedList<ASTnode> stmts = new LinkedList<>();
    TokenType type = TokenType.INVALID;
    String op = "";
    int loc;
    int rLoc;
    int fLoc;
    int tLoc;

    ASTnode(ASTnode aNode) {
        this.left = aNode.left;
        this.right = aNode.right;
        this.value = aNode.value;
        this.stmts = aNode.stmts;
        this.type = aNode.type;
        this.op = aNode.op;
    }

    ASTnode(TokenType type) {
        this.type = type;
        this.stmts = new LinkedList<>();
    }

    ASTnode(TokenType type, String value) {
        this.type = type;
        this.value = value;
        this.stmts = new LinkedList<>();
    }

    public ASTnode() {
        // TODO Auto-generated constructor stub
    }

    public String toString() {
        String val = "Node type:" + type + ", Node value:" + value;
        if (this.type != TokenType.NUM)
            val += ", Left Child (" + this.left.toString() + ")" + ", Right Child (" + this.right.toString() + ")";
        return val;
    }
}