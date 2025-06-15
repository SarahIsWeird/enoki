package blue.endless.enoki.gui.widgets;

import java.util.Iterator;
import java.util.List;

import blue.endless.enoki.markdown.LayoutStyle;
import blue.endless.enoki.text.WordWrap;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public class TextSpanWidget extends AbstractMarkdownWidget implements Splittable {
	protected final TextRenderer font;
	protected final String text;
	
	public TextSpanWidget(String text, LayoutStyle style, TextRenderer font) {
		super(0, 0, style.style().getTextWidth(text, font), (int) (font.fontHeight * style.style().size()), Text.literal(text).fillStyle(style.style().asStyle()), style);
		this.font = font;
		this.text = text;
	}

	@Override
	public Iterator<ClickableWidget> iterator() {
		return List.<ClickableWidget>of().iterator();
	}
	
	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.drawText(this.font, this.getMessage(), 0, 0, Colors.WHITE, this.style.style().shadow());
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
				wrap.hardWrap(font, lineWidth, text, style.style())
				:
				wrap.getCleanFirstLine(font, lineWidth, text, style.style());
		
		if (firstLine == null) return Result.nothingFits(this);
		if (firstLine.length() == text.length()) return Result.everythingFits(this);
		String remainder = text.substring(firstLine.length());
		return new Result(
				new TextSpanWidget(firstLine, style, font),
				new TextSpanWidget(remainder, style, font)
				);
	}
}
