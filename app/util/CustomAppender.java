package util;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.io.IOException;

public class CustomAppender extends AppenderBase<ILoggingEvent> {

    PatternLayoutEncoder encoder;

    @Override
    public void start() {
        if (this.encoder == null) {
            addError("No encoder set for the appender named [" + name + "].");
            return;
        }
        try {
            encoder.init(System.out);
        } catch (IOException e) {
        }
        super.start();
    }

    public void append(ILoggingEvent event) {
        try {
            this.encoder.doEncode(event);
            LogUtil.sendLog(event);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public PatternLayoutEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }
}
