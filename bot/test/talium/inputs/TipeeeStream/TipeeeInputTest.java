package talium.inputs.TipeeeStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TipeeeInputTest {
    private static final String reponse = """
                {
                    "code": 200,
                    "message": "success",
                    "datas": {
                        "port": "443",
                        "host": "https://sso-cf.tipeeestream.com"
                    }
                }""";

    @Test
    void parseUrl() throws TipeeeInput.SocketInfoEndpointException {
        String actual = TipeeeInput.parseSocketInfoUrlResponse(reponse, "TESTKEY").toString();
        assertEquals("https://sso-cf.tipeeestream.com?access_token=TESTKEY", actual);
    }

    @Test
    void parseUrl_garbage() {
        try {
            TipeeeInput.parseSocketInfoUrlResponse("8904ehfizgheodie398rtiwkef903fj9opcsajf", "TESTKEY");
            fail("Should have thrown SocketInfoEndpointException");
        } catch (TipeeeInput.SocketInfoEndpointException _) { }
    }
    @Test
    void parseUrl_invalid_url() {
        final String reponse = """
                {
                    "code": 200,
                    "message": "success",
                    "datas": {
                        "port": "443",
                        "host": "hts://sso-cf.tipeeestream.com"
                    }
                }""";
        try {
            TipeeeInput.parseSocketInfoUrlResponse(reponse, "TESTKEY");
            fail("Should have thrown SocketInfoEndpointException");
        } catch (TipeeeInput.SocketInfoEndpointException _) { }
    }

    /// test if the response format (and info url) is still the same as we expect. If this fails, tipeee has made a change
    /// we should then investigate (response, docs, posts) what changes they have made, and if they are breaking
    @Test
    void info_url_format_as_expected() throws TipeeeInput.SocketInfoEndpointException, JSONException {
        var body = TipeeeInput.getSocketUrlFromUrl(new TipeeeConfig(Optional.empty(), "", "").tipeeeSocketInfoUrl());
        assertEquals(new JSONObject(reponse).toString(), new JSONObject(body).toString(), "TipeeeStream socket Info Endpoint has changed response format!");
    }
}