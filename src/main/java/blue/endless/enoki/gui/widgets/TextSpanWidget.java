package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.markdown.styles.LayoutStyle;
import blue.endless.enoki.markdown.styles.properties.StyleProperties;
import blue.endless.enoki.text.WordWrap;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

public class TextSpanWidget extends AbstractMarkdownWidget implements Splittable {
	protected final TextRenderer font;
	protected final String text;
	
	public TextSpanWidget(String text, LayoutStyle style, TextRenderer font) {
		super(0, 0, style.getTextWidth(text, font), style.applyScale(font.fontHeight), Text.literal(text).fillStyle(style.asStyle()), style);
		this.font = font;
		this.text = text;
	}

	@Override
	@NotNull
	public Iterator<ClickableWidget> iterator() {
		return Collections.emptyIterator();
	}
	
	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		boolean shadow = style.getOrDefault(StyleProperties.SHADOW, false);
		context.drawText(this.font, this.getMessage(), 0, 0, Colors.WHITE, shadow);
	}
	
	@Override
	public int getHeight() {
		if (text.isBlank()) return 0;
		return super.getHeight();
	}
	
	@Override
	public Result split(int lineWidth, boolean force) {
		WordWrap wrap = new WordWrap();
		
		String firstLine = (force) ?
				wrap.hardWrap(font, lineWidth, text, style)
				:
				wrap.getCleanFirstLine(font, lineWidth, text, style);
		
		if (firstLine == null) return Result.nothingFits(this);
		if (firstLine.length() == text.length()) return Result.everythingFits(this);
		String remainder = text.substring(firstLine.length());
		return new Result(
				new TextSpanWidget(firstLine, style, font),
				new TextSpanWidget(remainder, style, font)
				);
	}
}
