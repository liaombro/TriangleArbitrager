import org.example.API.MsgHandler;
import org.example.util.JacksonHandler;
import org.example.models.symbols_request.Symbol;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;

public class JSONTest {

    @Test
    public void serializeTest(){
        Symbol symb = new Symbol();
        symb.setBaseCurrencyName("ololo");
        JacksonHandler jsoner = new JacksonHandler();

        String expected = "{\"baseCurrencyName\":\"ololo\"}";
        String actual = jsoner.toJSON(symb);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void deserializeTest(){
        JacksonHandler jsoner = new JacksonHandler();

        String message = "{\"baseCurrencyName\": \"ololo\"}";
        Symbol actual = jsoner.fromJSON(message, Symbol.class);

        Assert.assertEquals("ololo", actual.getBaseCurrencyName());
    }

    @Test
    public void marketOrderRequest(){
        MsgHandler handler = new MsgHandler();

        String actual = handler.prepareBuyMessageBody("Base_Quote", "1", true, "SELL");

        MatcherAssert.assertThat(actual, containsString("Base_Quote"));

    }
}
