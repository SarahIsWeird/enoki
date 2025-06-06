package blue.endless.enoki.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public class WordWrap {
    private BreakIterator breaks;
    private String localeName;

    public WordWrap() {
        this(MinecraftClient.getInstance().getLanguageManager().getLanguage());
    }

    public WordWrap(String localeName) {
        this.localeName = localeName;
        Locale locale = getLocaleByName(localeName);
        this.breaks = BreakIterator.getWordInstance(locale);
    }

    public String getLocaleName() {
        return localeName;
    }

    public String getFirstLine(TextRenderer font, int width, String text, Style style) {
        int totalWidth = font.getWidth(Text.literal(text).setStyle(style));
        if (totalWidth <= width) return text;

        String firstLine = text;
        breaks.setText(text);

        int pos = breaks.last();
        if (pos == BreakIterator.DONE) return hardWrap(font, width, firstLine, style);
        firstLine = firstLine.substring(0, pos);

        while (font.getWidth(Text.literal(firstLine).setStyle(style)) > width) {
            pos = breaks.previous();
            if (pos == BreakIterator.DONE) return hardWrap(font, width, firstLine, style);
            firstLine = firstLine.substring(0, pos);
        }

        return firstLine;
    }

    public String hardWrap(TextRenderer font, int width, String text, Style style) {
        while (font.getWidth(Text.literal(text).setStyle(style)) > width && text.length() > 1) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    public List<String> wrap(TextRenderer font, int width, String text, Style style) {
        List<String> lines = new ArrayList<>();

        while (!text.isEmpty()) {
            String line = getFirstLine(font, width, text, style);
            lines.add(line);
            text = text.substring(line.length());
        }

        return lines;
    }

    private static Locale getLocaleByName(String localeName) {
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.toString().equalsIgnoreCase(localeName)) {
                return locale;
            }
        }

        return Locale.getDefault();
    }
}
