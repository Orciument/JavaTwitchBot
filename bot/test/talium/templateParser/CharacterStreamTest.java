package talium.templateParser;

import org.junit.jupiter.api.Test;
import talium.templateParser.exeptions.UnexpectedEndOfInputException;

import static org.junit.jupiter.api.Assertions.*;

class CharacterStreamTest {

    @Test
    void peek_shows_next_character() throws UnexpectedEndOfInputException {
        var stream = new CharakterStream("TEST STREAM");
        assertEquals('T', stream.peek());
        assertEquals('T', stream.peek());
        assertEquals('T', stream.peek());
        assertEquals('T', stream.peek());
    }

    @Test
    void next_advances_to_next_character() throws UnexpectedEndOfInputException {
        var stream = new CharakterStream("TEST STREAM");
        assertEquals('T', stream.next());
        assertEquals('E', stream.next());
        assertEquals('S', stream.next());
        assertEquals('T', stream.next());
        assertEquals(' ', stream.peek());
        assertEquals(' ', stream.peek());
    }

    @Test
    void eof_true_at_end() throws UnexpectedEndOfInputException {
        String testStream = "TEST STREAM";
        var stream = new CharakterStream(testStream);
        for (int i = 0; i < testStream.length() ; i++) {
            stream.next();
        }
        assertTrue(stream.isEOF());
    }

    @Test
    void eof_false_at_not_end() throws UnexpectedEndOfInputException {
        var stream = new CharakterStream("TEST STREAM");
        assertFalse(stream.isEOF());
        stream.next();
        assertFalse(stream.isEOF());
        stream.next();
        assertFalse(stream.isEOF());
    }
}
