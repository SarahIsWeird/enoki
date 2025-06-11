package blue.endless.enoki.markdown;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

/**
 * This class is both expanded and simplified from net.minecraft.text.Style.
 * 
 * <p>It is expanded to include a size. Text in enoki, such as headers, are rendered larger than body text by default.
 * 
 * <p>It is greatly simplified in that a style element is either ON or UNSET. In minecraft, a style element can be ON,
 * OFF, or UNSET. We don't need the tri-state, and we certainly don't need the janky makeshift tri-state from nullable
 * boxed objects.
 */
@Environment(EnvType.CLIENT)
public record NodeStyle(float size, int color, byte style, @Nullable ClickEventHandler clickHandler) {
	public static final NodeStyle NORMAL = new NodeStyle(1.0f, -1);
	
	private static final int BOLD          = 0x01;
	private static final int ITALIC        = 0x02;
	private static final int UNDERLINE     = 0x04;
	private static final int STRIKETHROUGH = 0x08;
	private static final int SHADOW        = 0x10;
	
	@FunctionalInterface
	public interface ClickEventHandler {
		void handle(double mouseX, double mouseY);
	}
	
	public NodeStyle(float size, int color) {
		this(size, color, (byte) 0);
	}
	
	public NodeStyle(float size, int color, int style) {
		this(size, color, (byte) style, null);
	}
	
	public NodeStyle(float size, int color, int style, @Nullable ClickEventHandler onClick) {
		this(size, color, (byte) style, onClick);
	}

	public NodeStyle {
		if (size <= 0.0f) throw new IllegalArgumentException("The node size must be strictly positive!");
	}
	
	public boolean bold() {
		return (style & BOLD) != 0;
	}
	
	public boolean italic() {
		return (style & ITALIC) != 0;
	}
	
	public boolean underline() {
		return (style & UNDERLINE) != 0;
	}
	
	public boolean strikethrough() {
		return (style & STRIKETHROUGH) != 0;
	}
	
	public boolean shadow() {
		return (style & SHADOW) != 0;
	}
	
	// "with"-ers
	
	public NodeStyle withSize(float value) {
		return new NodeStyle(value, color, style);
	}
	
	public NodeStyle withColor(Formatting formatting) {
		Integer formatColor = formatting.getColorValue();
		return (formatColor == null) ? withColor(-1) : withColor(formatColor);
	}
	
	public NodeStyle withColor(int argb) {
		if (color == argb) return this;
		return new NodeStyle(size, argb, style);
	}
	
	public NodeStyle withBold() {
		return new NodeStyle(size, color, style | BOLD, clickHandler);
	}
	
	public NodeStyle withItalic() {
		return new NodeStyle(size, color, style | ITALIC, clickHandler);
	}
	
	public NodeStyle withUnderline() {
		return new NodeStyle(size, color, style | UNDERLINE, clickHandler);
	}
	
	public NodeStyle withStrikethrough() {
		return new NodeStyle(size, color, style | STRIKETHROUGH, clickHandler);
	}
	
	public NodeStyle withShadow() {
		return new NodeStyle(size, color, style | SHADOW, clickHandler);
	}
	
	public NodeStyle withoutBold() {
		return new NodeStyle(size, color, style & ~BOLD, clickHandler);
	}
	
	public NodeStyle withoutItalic() {
		return new NodeStyle(size, color, style & ~ITALIC, clickHandler);
	}
	
	public NodeStyle withoutUnderline() {
		return new NodeStyle(size, color, style & ~UNDERLINE, clickHandler);
	}
	
	public NodeStyle withoutStrikethrough() {
		return new NodeStyle(size, color, style & ~STRIKETHROUGH, clickHandler);
	}
	
	public NodeStyle withoutShadow() {
		return new NodeStyle(size, color, style & ~SHADOW, clickHandler);
	}
	
	public NodeStyle withOnClick(@Nullable ClickEventHandler newOnClick) {
		return new NodeStyle(size, color, style, newOnClick);
	}
	
	public Style asStyle() {
		Style result = Style.EMPTY;
		
		if (bold())          result = result.withBold(true);
		if (italic())        result = result.withItalic(true);
		if (underline())     result = result.withUnderline(true);
		if (strikethrough()) result = result.withStrikethrough(true);
		if (shadow())        result = result.withShadowColor(0);
		if (color != -1)     result = result.withColor(color);
		
		return result;
	}

	/**
	 * Combines the current style with defaults.
	 * The {@link #color} and the {@link #clickHandler} behavior is preserved.
	 * 
	 * @param defaults The style to default the behavior to
	 * @return The combined style
	 */
	public NodeStyle combined(NodeStyle defaults) {
		int color = this.color;
		if (color == -1) color = defaults.color;
		return new NodeStyle(size * defaults.size, color, this.style | defaults.style, defaults.clickHandler);
	}
	
	public int applyScale(int value) {
		return value;
		// return (int) (attributes * size);
	}
	
	public int getTextWidth(String string, TextRenderer font) {
		return getTextWidth(Text.literal(string).setStyle(this.asStyle()), font);
	}
	
	public int getTextWidth(Text text, TextRenderer font) {
		return this.applyScale(font.getWidth(text));
	}
}
