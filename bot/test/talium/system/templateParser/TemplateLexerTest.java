package talium.system.templateParser;

import org.junit.jupiter.api.Test;
import talium.system.templateParser.exeptions.UnexpectedEndOfInputException;
import talium.system.templateParser.exeptions.UnexpectedTokenException;
import talium.system.templateParser.exeptions.UnsupportedDirective;
import talium.system.templateParser.tokens.TemplateToken;
import talium.system.templateParser.tokens.TemplateTokenKind;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

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
        assert tokens.size() == 1;
        assert tokens.getFirst().kind() == TemplateTokenKind.TEXT;
        assert tokens.getFirst().value().equals("Hello, {var.name}!");
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
        TemplateLexer lex = new TemplateLexer("Hello, %{ if var.name != \"\" }${var.name}%{ else }unnamed{ endif }");
        System.out.println(lex.parse());
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
    void directive_start_in_text() throws UnsupportedDirective, UnexpectedEndOfInputException {
        try {
            new TemplateLexer("template text % mehr text").parse();
            fail("Should have thrown an TemplateSyntaxException");
        } catch (UnexpectedTokenException _) { }
        try {
            new TemplateLexer("template text %c mehr text").parse();
            fail("Should have thrown an TemplateSyntaxException");
        } catch (UnexpectedTokenException _) { }
    }

    @Test
    void variable_start_in_text() throws UnsupportedDirective, UnexpectedEndOfInputException {
        try {
            new TemplateLexer("template text $ mehr text").parse();
            fail("Should have thrown an TemplateSyntaxException");
        } catch (UnexpectedTokenException _) { }
        try {
            new TemplateLexer("template text $c mehr text").parse();
            fail("Should have thrown an TemplateSyntaxException");
        } catch (UnexpectedTokenException _) { }
    }

    @Test
    void returnNonNull() throws UnexpectedTokenException, UnsupportedDirective, UnexpectedEndOfInputException {
        assert !new TemplateLexer("t%{   endif }").parse().contains(null);
    }

    @Test
    void unnecessary_tokens_to_text() throws UnexpectedTokenException, UnsupportedDirective, UnexpectedEndOfInputException {
        assert new TemplateLexer("t%{ endif }").parse().equals(List.of(TemplateToken.text("t"), TemplateToken.endif()));
        assert new TemplateLexer("t%{ endfor }").parse().equals(List.of(TemplateToken.text("t"), TemplateToken.for_end()));
        assert new TemplateLexer("t%{ else }").parse().equals(List.of(TemplateToken.text("t"), TemplateToken.if_else()));
    }
}
