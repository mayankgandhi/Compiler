public class Scanner {

    public Token extractToken(StringBuilder stream) {
        char aChar = stream.charAt(0);
        String aValue = "";
        while (stream.length() != 0) {
            aChar = stream.charAt(0);
            if (Character.isWhitespace(aChar)) {
                stream.deleteCharAt(0);
            } else {
                break;
            }
        }

        if (stream.length() == 0) {
            return null;
        }

        TokenType aType = TokenType.INVALID;

        if (Character.isDigit(aChar)) {
            StringBuilder a = new StringBuilder();
            while (stream.length() != 0) {
                aChar = stream.charAt(0);
                if (Character.isDigit(aChar)) {
                    a.append(aChar);
                    stream.deleteCharAt(0);
                } else {
                    break;
                }
            }
            aValue = a.toString();
            aType = TokenType.NUM;
        } else if ((aChar >= 'a' && aChar <= 'z') || (aChar >= 'A' && aChar <= 'Z')) {
            StringBuilder a = new StringBuilder();
            while (stream.length() != 0) {
                aChar = stream.charAt(0);
                if (Character.isLetterOrDigit(aChar)) {
                    a.append(aChar);
                    stream.deleteCharAt(0);
                } else {
                    break;
                }
            }

            aValue = a.toString();

            switch (a.toString()) {
            case "int":
                aType = TokenType.INT;
                break;
            case "if":
                aType = TokenType.IF;
                break;
            case "while":
                aType = TokenType.WHILE;
                break;
            case "void":
                aType = TokenType.VOID;
                break;
            case "public":
                aType = TokenType.PUBLIC;
                break;
            case "private":
                aType = TokenType.PRIVATE;
                break;
            case "return":
                aType = TokenType.RETURN;
                break;
            case "class":
                aType = TokenType.CLASS;
                break;
            default:
                aType = TokenType.ID;
                break;
            }
        } else if (aChar == ';') {
            stream.deleteCharAt(0);
            aValue = String.valueOf(aChar);
            aType = TokenType.SEMICOLON;
        } else if (aChar == ',') {
            stream.deleteCharAt(0);
            aValue = String.valueOf(aChar);
            aType = TokenType.COMMA;
        } else if (aChar == '{') {
            stream.deleteCharAt(0);
            aValue = String.valueOf(aChar);
            aType = TokenType.LEFTCURLY;
        } else if (aChar == '}') {
            stream.deleteCharAt(0);
            aValue = String.valueOf(aChar);
            aType = TokenType.RIGHTCURLY;
        } else if (aChar == '+') {
            stream.deleteCharAt(0);
            aValue = String.valueOf(aChar);
            aType = TokenType.PLUS;
        } else if (aChar == '-') {
            stream.deleteCharAt(0);
            aValue = String.valueOf(aChar);
            aType = TokenType.MINUS;
        } else if (aChar == '*') {
            stream.deleteCharAt(0);
            aValue = String.valueOf(aChar);
            aType = TokenType.MUL;
        } else if (aChar == '/') {
            stream.deleteCharAt(0);
            aValue = String.valueOf(aChar);
            aType = TokenType.DIV;
        } else if (aChar == '<') {
            aValue = String.valueOf(aChar);
            stream.deleteCharAt(0);
            aType = TokenType.LT;

            if (stream.length() != 0 && stream.charAt(0) == '=') {
                stream.deleteCharAt(0);
                aValue += '=';
                aType = TokenType.LTE;
            }
        } else if (aChar == '>') {
            aValue = String.valueOf(aChar);
            stream.deleteCharAt(0);
            aType = TokenType.GT;

            if (stream.length() != 0 && stream.charAt(0) == '=') {
                stream.deleteCharAt(0);
                aValue += '=';
                aType = TokenType.GTE;
            }
        } else if (aChar == '!') {
            aValue = String.valueOf(aChar);
            stream.deleteCharAt(0);
            if (stream.length() != 0 && stream.charAt(0) == '=') {
                stream.deleteCharAt(0);
                aValue += '=';
                aType = TokenType.NOTEQUALS;
            }
        } else if (aChar == '=') {
            aValue = String.valueOf(aChar);
            stream.deleteCharAt(0);
            aType = TokenType.ASSIGN;

            if (stream.length() != 0 && stream.charAt(0) == '=') {
                stream.deleteCharAt(0);
                aValue += '=';
                aType = TokenType.EQUALS;
            }
        } else if (aChar == '&') {
            aValue = String.valueOf(aChar);
            stream.deleteCharAt(0);
            if (stream.length() != 0 && stream.charAt(0) == '&') {
                stream.deleteCharAt(0);
                aValue += '&';
                aType = TokenType.AND;
            }
        } else if (aChar == '|') {
            aValue = String.valueOf(aChar);
            stream.deleteCharAt(0);
            if (stream.length() != 0 && stream.charAt(0) == '|') {
                stream.deleteCharAt(0);
                aValue += '|';
                aType = TokenType.OR;
            }
        } else if (aChar == '(') {
            stream.deleteCharAt(0);
            aValue = String.valueOf(aChar);
            aType = TokenType.LEFTPAREN;
        } else if (aChar == ')') {
            stream.deleteCharAt(0);
            aValue = String.valueOf(aChar);
            aType = TokenType.RIGHTPAREN;
        } else {
            stream.deleteCharAt(0);
            aValue = String.valueOf(aChar);
            aType = TokenType.INVALID;
        }

        return new Token(aType, aValue);
    }

    public String extractTokens(String arg) {
        StringBuilder currentString = new StringBuilder(arg);
        StringBuilder result = new StringBuilder();
        while (currentString.length() != 0) {
            Token nextToken = extractToken(currentString);
            if (nextToken != null) {
                if (nextToken.tokenType == TokenType.INVALID) {
                    System.out.println("INVALID TOKEN: " + nextToken.tokenVal);
                    break;
                }
                result.append(nextToken.toString());
            }
        }
        return result.toString();
    }

    public static void main(String args[])
    {
        String eval = "private class test { int z ; void main2 ( int x, int y ) { z = 14 ; return x; } ";
        Scanner ob = new Scanner();
        System.out.println(ob.extractTokens(eval));
    }
}