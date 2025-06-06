package blue.endless.enoki.markdown;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

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
public record NodeStyle(float size, int color, byte style) {
	public static final NodeStyle NORMAL = new NodeStyle(1.0f, -1);
	
	private static final int BOLD          = 0x01;
	private static final int ITALIC        = 0x02;
	private static final int UNDERLINE     = 0x04;
	private static final int STRIKETHROUGH = 0x08;
	private static final int SHADOW        = 0x10;
	
	public NodeStyle(float size, int color) {
		this(size, color, (byte) 0);
	}
	
	public NodeStyle(float size, int color, int style) {
		this(size, color, (byte) style);
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
	
	public NodeStyle withColor(Formatting formatting) {
		Integer formatColor = formatting.getColorValue();
		return (formatColor == null) ? withColor(-1) : withColor(formatColor);
	}
	
	public NodeStyle withColor(int argb) {
		if (color == argb) return this;
		return new NodeStyle(size, argb, style);
	}
	
	public NodeStyle withBold() {
		return new NodeStyle(size, color, style | BOLD);
	}
	
	public NodeStyle withItalic() {
		return new NodeStyle(size, color, style | ITALIC);
	}
	
	public NodeStyle withUnderline() {
		return new NodeStyle(size, color, style | UNDERLINE);
	}
	
	public NodeStyle withStrikethrough() {
		return new NodeStyle(size, color, style | STRIKETHROUGH);
	}
	
	public NodeStyle withShadow() {
		return new NodeStyle(size, color, style | SHADOW);
	}
	
	public NodeStyle withoutBold() {
		return new NodeStyle(size, color, style & ~BOLD);
	}
	
	public NodeStyle withoutItalic() {
		return new NodeStyle(size, color, style & ~ITALIC);
	}
	
	public NodeStyle withoutUnderline() {
		return new NodeStyle(size, color, style & ~UNDERLINE);
	}
	
	public NodeStyle withoutStrikethrough() {
		return new NodeStyle(size, color, style & ~STRIKETHROUGH);
	}
	
	public NodeStyle withoutShadow() {
		return new NodeStyle(size, color, style & ~SHADOW);
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
}
