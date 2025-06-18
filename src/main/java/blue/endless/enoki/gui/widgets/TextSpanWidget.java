package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.markdown.styles.LayoutStyle;
import blue.endless.enoki.markdown.styles.properties.StyleProperties;
import blue.endless.enoki.text.WordWrap;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public class TextSpanWidget extends AbstractMarkdownWidget implements Splittable {
	protected final TextRenderer font;
	protected final String text;
	
	public TextSpanWidget(String text, LayoutStyle style, TextRenderer font) {
		super(0, 0, style.getTextWidth(text, font), style.applyScale(font.fontHeight), Text.literal(text).fillStyle(style.asStyle()), style);
		this.font = font;
		this.text = text;
	}
	
	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.getMatrices().push();
		
		float textSize = this.style.getOrDefault(StyleProperties.SIZE, 1f);
		if (Math.abs(textSize - 1f) > 0.001f) {
			context.getMatrices().scale(textSize, textSize, 1);
		}
		
		boolean shadow = style.getOrDefault(StyleProperties.SHADOW, false);
		context.drawText(this.font, this.getMessage(), 0, 0, Colors.WHITE, shadow);
		
		context.getMatrices().pop();
	}
	
	@Override
	public int getHeight() {
		if (text.isBlank()) return 0;
		return super.getHeight();
	}
	
	@Override
	public Text getAsText() {
		System.out.println("Message: "+getMessage());
		return getMessage();
	}
	
	@Override
	public Result split(int lineWidth, boolean force) {
		WordWrap wrap = new WordWrap();
		
		String firstLine = (force) ?
				wrap.hardWrap(font, lineWidth, text, style).stripLeading()
				:
				wrap.getCleanFirstLine(font, lineWidth, text, style).stripLeading();
		
		if (firstLine == null) return Result.nothingFits(this);
		if (firstLine.length() == text.length()) return Result.everythingFits(this);
		String remainder = text.substring(firstLine.length());
		return new Result(
				new TextSpanWidget(firstLine, style, font),
				new TextSpanWidget(remainder, style, font)
				);
	}
}
