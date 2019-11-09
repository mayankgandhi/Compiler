public class Token {
        TokenType tokenType;
        String tokenVal;

        public Token(TokenType tokenType, String tokenVal) {
            this.tokenType = tokenType;
            this.tokenVal = tokenVal;
        }

        public String toString() {
            return this.tokenType + ": " + this.tokenVal + " ";
        }
    }