package talium.system.templateParser;

import org.junit.jupiter.api.Test;
import talium.system.templateParser.exeptions.UnexpectedEndOfInputException;
import talium.system.templateParser.exeptions.UnexpectedTokenException;
import talium.system.templateParser.exeptions.UnsupportedDirective;
import talium.system.templateParser.tokens.TemplateToken;
import talium.system.templateParser.tokens.TemplateTokenKind;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// the correct behaviour is not entirely specified, thats why this test is disabled by default.
// more or less: if a token is started it has to be finished, otherwise it is a syntax error
// completion of a statement is not yet checked at this stage, so a missing else directive is not an error at this stage
public class TemplateLexerTest {
    @Test
    void missing_closing_bracket() throws UnsupportedDirective, UnexpectedEndOfInputException {
        TemplateLexer lex = new TemplateLexer("Hello, ${var.name!");
        try {
            lex.parse();
            fail("Should have thrown an TemplateSyntaxException");
        } catch (UnexpectedTokenException _) {}
    }

    @Test
    void missing_dollar() throws UnexpectedTokenException, UnsupportedDirective, UnexpectedEndOfInputException {
        var tokens = new TemplateLexer("Hello, {var.name}!").parse();
        assertEquals(1, tokens.size());
        assertEquals(TemplateTokenKind.TEXT, tokens.getFirst().kind());
        assertEquals("Hello, {var.name}!", tokens.getFirst().value());
    }

    @Test
    void missing_closing_bracket_directive() throws UnsupportedDirective, UnexpectedEndOfInputException {
        TemplateLexer lex = new TemplateLexer("Hello, %{ if var.name != \"\" }${var.name}%{ else }unnamed%{ endif ");
        try {
            lex.parse();
            fail("Should have thrown an TemplateSyntaxException");
        } catch (UnexpectedTokenException _) { }
    }

    @Test
    void missing_percent() throws UnexpectedTokenException, UnsupportedDirective, UnexpectedEndOfInputException {
        var tokens = new TemplateLexer("Hello, %{ if var.name != \"\" }${var.name}%{ else }unnamed{ endif }").parse();
        assertEquals(5, tokens.size());
        assertEquals(TemplateTokenKind.TEXT, tokens.get(0).kind());
        assertEquals("Hello, ", tokens.get(0).value());
        assertEquals(TemplateTokenKind.IF_HEAD, tokens.get(1).kind());
        assertEquals(TemplateTokenKind.VAR, tokens.get(2).kind());
        assertEquals(TemplateTokenKind.IF_ELSE, tokens.get(3).kind());
        assertEquals(TemplateTokenKind.TEXT, tokens.get(4).kind());
        assertEquals("unnamed{ endif }", tokens.get(4).value());
    }

    @Test
    void malformed_endIf() throws UnsupportedDirective, UnexpectedEndOfInputException {
        TemplateLexer lex = new TemplateLexer("Hello, %{ if var.name != \"\" }${var.name}%{ else }unnamed%{ endif dadasdadad }");
        try {
            lex.parse();
            fail("Should have thrown an TemplateSyntaxException");
        } catch (UnexpectedTokenException _) { }
    }

    @Test
    void percent_dollar_in_template() throws UnsupportedDirective, UnexpectedEndOfInputException, UnexpectedTokenException {
        new TemplateLexer("template text % mehr text").parse();
        new TemplateLexer("template text %c mehr text").parse();
        new TemplateLexer("template text $ mehr text").parse();
        new TemplateLexer("template text $c mehr text").parse();
    }

    @Test
    void variable_start_in_text() throws UnsupportedDirective, UnexpectedEndOfInputException {
        try {
            new TemplateLexer("template text ${ mehr text").parse();
            fail("Should have thrown an TemplateSyntaxException");
        } catch (UnexpectedTokenException _) { }
        try {
            new TemplateLexer("template text ${c mehr text").parse();
            fail("Should have thrown an TemplateSyntaxException");
        } catch (UnexpectedTokenException _) { }
    }

    @Test
    void returnNonNull() throws UnexpectedTokenException, UnsupportedDirective, UnexpectedEndOfInputException {
        assertFalse(new TemplateLexer("t%{   endif }").parse().contains(null));
    }

    @Test
    void unnecessary_tokens_to_text() throws UnexpectedTokenException, UnsupportedDirective, UnexpectedEndOfInputException {
        assertEquals(List.of(TemplateToken.text("t"), TemplateToken.endif() ), new TemplateLexer("t%{ endif }").parse());
        assertEquals(List.of(TemplateToken.text("t"), TemplateToken.for_end()), new TemplateLexer("t%{ endfor }").parse());
        assertEquals(List.of(TemplateToken.text("t"), TemplateToken.if_else()), new TemplateLexer("t%{ else }").parse());
    }
}
