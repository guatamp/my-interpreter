package craftinginterpreters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static craftinginterpreters.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source){
        this.source = source;
    }

    static{
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    // Go through the tokens one by one from the source file.
    // After the end of the file is reached, add an end of file token.
    // Return a list of tokens.
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    // This is the main function of the scanner. it goes through each character of the file and classifies it into various tokens.
    private void scanToken(){
        char c = advance();
        switch(c){
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '/':
                // need to deal with case of comments
                if(match('/')){
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t': break;
            case '\n':
                line++;
                break;
            case '"':string();break;
            default:
                if(isDigit(c)){
                    number();
                } else if(isAlpha(c)){
                    identifier();
                } else {
                    Lox.error(line, "Unexpected Character.");
                }
                break;
        }
    }
    
    // Recognises identifiers (variable names etc)
    private void identifier(){
        while(isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null) type = IDENTIFIER;
        addToken(type);
    }

    // Recognises numbers and decimals. Also store the number along with the token for future reference
    private void number(){
        while(isDigit(peek())) advance();

        // Look for decimal numbers
        if(peek() == '.' && isDigit(peekNext())){
            // consume the .
            advance();
            while(isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    // When " is detected it labels all the following characters as a string literal until EOF or closing " is detected
    // Also along with the token store the string literal as well
    private void string(){
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n') line++;
            advance();
        }
        if(isAtEnd()){
            Lox.error(line, "Unterminated string");
            return;
        }
        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    // This method is used to differentiate between tokens that can be one or two characters long
    // eg. without this fn != would be split into two different tokens: ! and =
    private boolean match(char expected){
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    // Returns the current character. charAt takes in index value and returns the value at that index
    private char peek(){
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext(){
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // Checks if the character is an alphabet or an underscore. 
    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') ||(c >= 'A' && c <= 'Z')|| c == '_'; 
    }

    // Returns true if the character is a digit or a letter or an _. Used for Identifiers
    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }

    // Checks if the character is a digit
    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

    // Checks if the end of the file has been reached
    private boolean isAtEnd(){
        return current >= source.length();
    }

    // Increments current and returns the character at the previous index
    private char advance(){
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    // Adds a token to the list of tokens
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}