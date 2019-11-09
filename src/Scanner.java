import java.util.LinkedList;
import java.lang.String;
/*
 * Programming Assignment 4 - Alexis Tinoco
 *                            Mayank Gandhi
 */

public class Scanner {

    public LinkedList<Token> ScannedTokens = new LinkedList<Token>();

    public Token extractToken(StringBuilder stream) {
        /* TODO #2: Extract the next token in the string, or report an error */
        Token token = null;
        TokenType tokenType = null;
        String nextChar = "";
        String num = "";
        if (stream.length() != 0) {
            nextChar = stream.charAt(0) + "";
        }
        while (stream.length() != 0) {
            if (nextChar.equals("+")) {
                tokenType = TokenType.PLUS;
                token = new Token(tokenType, nextChar);
                stream.deleteCharAt(0);
                break;
            } else if (nextChar.equals("-")) {
                tokenType = TokenType.MINUS;
                token = new Token(tokenType, nextChar);
                stream.deleteCharAt(0);
                break;
            } else if (nextChar.equals("*")) {
                tokenType = TokenType.MUL;
                token = new Token(tokenType, nextChar);
                stream.deleteCharAt(0);
                break;
            } else if (nextChar.equals("/")) {
                tokenType = TokenType.DIV;
                token = new Token(tokenType, nextChar);
                stream.deleteCharAt(0);
                break;
            } else if (nextChar.equals(";")) {
                tokenType = TokenType.SECOL;
                token = new Token(tokenType, nextChar);
                stream.deleteCharAt(0);
                break;
            } else if (nextChar.equals("{")) {
                tokenType = TokenType.OBRA;
                token = new Token(tokenType, nextChar);
                stream.deleteCharAt(0);
                break;
            } else if (nextChar.equals("}")) {
                tokenType = TokenType.CBRA;
                token = new Token(tokenType, nextChar);
                stream.deleteCharAt(0);
                break;
            } else if (nextChar.equals(">")) {
                // check >=
                if (stream.charAt(1) == '=') {
                    tokenType = TokenType.GTE;
                    stream.deleteCharAt(0);
                    stream.deleteCharAt(0);
                    token = new Token(tokenType, ">=");
                } else {
                    tokenType = TokenType.GT;
                    token = new Token(tokenType, nextChar);
                    stream.deleteCharAt(0);
                }
                break;
            } else if (nextChar.equals("&")) {
                // check &&
                if (stream.charAt(1) == '&') {
                    tokenType = TokenType.ANDAND;
                    stream.deleteCharAt(0);
                    stream.deleteCharAt(0);
                    token = new Token(tokenType, "&&");
                } else {
                    System.out.println("Error, input '&' needs to be follow by '&'");
                    System.exit(1);
                }
                break;
            } else if (nextChar.equals("|")) {
                // check ||
                if (stream.charAt(1) == '|') {
                    tokenType = TokenType.OROR;
                    stream.deleteCharAt(0);
                    stream.deleteCharAt(0);
                    token = new Token(tokenType, "||");
                } else {
                    System.out.println("Error, input '|' needs to be follow by '|'");
                    System.exit(1);
                }
                break;
            }

            else if (nextChar.equals("!")) {
                // check >=
                if (stream.charAt(1) == '=') {
                    tokenType = TokenType.NOEQ;
                    stream.deleteCharAt(0);
                    stream.deleteCharAt(0);
                    token = new Token(tokenType, "!=");
                } else {
                    System.out.println("Error, input '!' needs to be follow by '='");
                    System.exit(1);
                }
                break;
            } else if (nextChar.equals("=")) {
                // check >
                if (stream.charAt(1) == '=') {
                    tokenType = TokenType.EQEQ;
                    stream.deleteCharAt(0);
                    stream.deleteCharAt(0);
                    token = new Token(tokenType, "==");
                } else {
                    tokenType = TokenType.EQ;
                    token = new Token(tokenType, "=");
                    stream.deleteCharAt(0);
                }
                break;
            } else if (nextChar.equals("<")) {
                // check <=
                if (stream.charAt(1) == '=') {
                    tokenType = TokenType.LTE;
                    stream.deleteCharAt(0);
                    stream.deleteCharAt(0);
                    token = new Token(tokenType, "<=");
                } else {
                    tokenType = TokenType.LT;
                    token = new Token(tokenType, nextChar);
                    stream.deleteCharAt(0);
                }
                break;
            } else if (nextChar.equals("(")) {
                tokenType = TokenType.OP;
                token = new Token(tokenType, nextChar);
                stream.deleteCharAt(0);
                break;
            } else if (nextChar.equals(")")) {
                tokenType = TokenType.CP;
                token = new Token(tokenType, nextChar);
                stream.deleteCharAt(0);
                break;
            } else if (nextChar.equals(" ")) {
                stream.deleteCharAt(0);
                if (tokenType == null) {
                    nextChar = stream.charAt(0) + "";
                    continue;
                } else {
                    break;
                }
            }
            // ID
            else if ((nextChar.toCharArray()[0] >= 65 && nextChar.toCharArray()[0] <= 90)
                    || (nextChar.toCharArray()[0] >= 97 && nextChar.toCharArray()[0] <= 122)) {
                tokenType = TokenType.ID;
                String newId = nextChar;
                stream.deleteCharAt(0);
                token = new Token(tokenType, newId);
                while (stream.length() != 0) {
                    char nextChar2 = stream.charAt(0);
                    if (Character.isDigit(nextChar2) || (nextChar2 >= 65 && nextChar2 <= 90)
                            || (nextChar2 >= 97 && nextChar2 <= 122)) {
                        newId += nextChar2 + "";
                        token = new Token(tokenType, newId);
                        stream.deleteCharAt(0);
                        continue;
                    } else {
                        if (newId.equals("int")) {
                            token = new Token(TokenType.ID, newId);
                        } else if (newId.equals("if")) {
                            token = new Token(TokenType.IF, newId);
                        } else if (newId.equals("while")) {
                            token = new Token(TokenType.WHILE, newId);
                        } else if (newId.equals("void")) {
                            token = new Token(TokenType.VOID, newId);
                        }
                        return token;
                    }
                }
            } else {
                // checking if an integer
                try {
                    Integer.parseInt(nextChar);
                    tokenType = TokenType.NUM;
                    token = new Token(tokenType, nextChar);
                    num += nextChar;
                    stream.deleteCharAt(0);
                    // check if there are more integer characters
                    while (stream.length() != 0) {
                        char nextChar2 = stream.charAt(0);
                        if (Character.isDigit(nextChar2)) {
                            num += nextChar2 + "";
                            token = new Token(tokenType, num);
                            stream.deleteCharAt(0);
                            continue;
                        } else {
                            return token;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("ERROR");
                    System.exit(1);
                    stream.deleteCharAt(0);
                    break;
                }
            }
        }
        return token;
    }

    public String extractTokens(String arg) {
        /*
         * TODO #1: Finish this function to iterate over all tokens in the input string.
         * Pseudo code: String exnulinutractTokens(String arg): String resuljhnihnbt=
         * â€œâ€�; while(arg is not empty) Token nextToken = extractToken(arg) result +=
         * nextToken.toString() return result
         */
        String result = ""; // final result
        StringBuilder args = new StringBuilder(arg);
        int i = 0;
        while (args.length() != 0) {
            Token token = extractToken(args);
            ScannedTokens.add(token);
            result += "|" + token.toString() + "|"; // '|' are to match final result in file
            i++;
        }
        return result;
    }

    public static void main(String args[]) {
        Scanner test = new Scanner();
        String result = test.extractTokens("(  { }9 + 9 )");
        System.out.println(result);
        System.out.println(test.ScannedTokens.toString());
    }
}
