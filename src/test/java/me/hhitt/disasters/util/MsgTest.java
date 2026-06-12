package me.hhitt.disasters.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MsgTest {

    @Test
    void parseNullReturnsEmpty() {
        Component result = Msg.parse(null);
        String text = PlainTextComponentSerializer.plainText().serialize(result);
        assertEquals("", text);
    }

    @Test
    void parseEmptyStringReturnsEmpty() {
        Component result = Msg.parse("");
        String text = PlainTextComponentSerializer.plainText().serialize(result);
        assertEquals("", text);
    }

    @Test
    void parsePlainText() {
        Component result = Msg.parse("Hello World");
        String text = PlainTextComponentSerializer.plainText().serialize(result);
        assertEquals("Hello World", text);
    }

    @Test
    void parseColorCodes() {
        Component result = Msg.parse("<red>Red Text");
        String text = PlainTextComponentSerializer.plainText().serialize(result);
        assertEquals("Red Text", text);
    }

    @Test
    void parseMiniMessageFormatting() {
        Component result = Msg.parse("<bold>Bold Text</bold>");
        String text = PlainTextComponentSerializer.plainText().serialize(result);
        assertEquals("Bold Text", text);
    }

    @Test
    void parseWithGradient() {
        Component result = Msg.parse("<rainbow>Rainbow");
        String text = PlainTextComponentSerializer.plainText().serialize(result);
        assertEquals("Rainbow", text);
    }
}
