package util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Json BigDecimal deal
 * Created by howen on 15/12/10.
 */
public class DiscountSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator jgen, SerializerProvider provider) throws IOException{
        jgen.writeString(value.setScale(1, BigDecimal.ROUND_DOWN).stripTrailingZeros().toPlainString());
    }
}