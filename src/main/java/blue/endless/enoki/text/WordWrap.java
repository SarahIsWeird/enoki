package blue.endless.enoki.text;

import blue.endless.enoki.markdown.styles.LayoutStyle;
import blue.endless.enoki.util.NotNullByDefault;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import org.jetbrains.annotations.Nullable;

import java.text.BreakIterator;
import java.util.Locale;

@Environment(EnvType.CLIENT)
@NotNullByDefault
public class WordWrap {
	private final BreakIterator breaks;
	
	public WordWrap() {
		this(MinecraftClient.getInstance().getLanguageManager().getLanguage());
	}
	
	public WordWrap(String localeName) {
		Locale locale = getLocaleByName(localeName);
		this.breaks = BreakIterator.getWordInstance(locale);
	}
	
	@Nullable
	public String getCleanFirstLine(TextRenderer font, int width, String text, LayoutStyle style) {
		int totalWidth = style.getTextWidth(text, font);
		if (totalWidth <= width) return text;
		
		String firstLine = text;
		breaks.setText(text);
		
		int pos = breaks.last();
		if (pos == BreakIterator.DONE) return null;
		firstLine = firstLine.substring(0, pos);
		
		while (style.getTextWidth(firstLine, font) > width) {
			pos = breaks.previous();
			if (pos == BreakIterator.DONE) return null;
			firstLine = firstLine.substring(0, pos);
		}
		
		return firstLine;
	}
	
	public String hardWrap(TextRenderer font, int width, String text, LayoutStyle style) {
		while (style.getTextWidth(text, font) > width && text.length() > 1) {
			text = text.substring(0, text.length() - 1);
		}
		
		return text;
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
