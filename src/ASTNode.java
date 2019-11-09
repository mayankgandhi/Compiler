import java.util.LinkedList;

public class ASTNode{
    ASTNode left, right;
    String value;
    LinkedList<ASTNode> stmts = new LinkedList<>();
    TokenType type;
    int loc;
    String op;
	public int fLoc;
	public int tLoc;
	public int rLoc;

    ASTNode()
    {
        this.left = null;
        this.right = null;
        this.value = "";
        this.stmts = null;
        this.op = "";
    }

    ASTNode( ASTNode node )
    {
        this.left = node.left;
        this.right = node.right;
        this.value = node.value;
        this.stmts = node.stmts;
        this.type = node.type;
        this.op = node.op;
    }

    ASTNode(TokenType type){
        this.type = type;
        this.stmts = new LinkedList<>(  );
    }

    ASTNode( TokenType type, String value){
        this.type = type;
        this.value = value;
        this.stmts = new LinkedList<>(  );
    }

    public String toString() {
        String val = "ASTNode type:" + type + ", ASTNode value:" + value;
        if(this.type != TokenType.NUM)
            val += ", Left Child (" + this.left.toString() + ")" + ", Right Child (" + this.right.toString() + ")";
        return val;
    }
}