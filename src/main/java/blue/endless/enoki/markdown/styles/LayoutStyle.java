package blue.endless.enoki.markdown.styles;

import blue.endless.enoki.markdown.styles.codec_intermediates.DecorationsIntermediate;
import blue.endless.enoki.markdown.styles.codec_intermediates.MarginsIntermediate;
import blue.endless.enoki.markdown.styles.properties.StyleProperties;
import blue.endless.enoki.markdown.styles.properties.StyleProperty;
import blue.endless.enoki.utils.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LayoutStyle {
	/*
	{
		"size": 1.0,
		// "color": -1,
		// "color": [ 1.0, 0.0, 0.066, 0.133 ],
		// "color": "f012",
		// "color": "001122",
		"color": "#ff001122",
		"style": {
			"bold": true,
			"italic": true,
			"underline": false,
			"strikethrough": false,
			"shadow": true
		},
		"indent": 0,
		// "margins": 0,
		// "margins": [ 0, 1, 2, 3 ],
		"margins": {
			"top": 0,
			"right": 1,
			"bottom": 2,
			"left": 3
		}
	}
	 */
	
	public static final Codec<LayoutStyle> CODEC =
		RecordCodecBuilder.create(inst -> inst.group(
			Codec.FLOAT.optionalFieldOf("size")
				.forGetter(style -> style.get(StyleProperties.SIZE)),
			CodecUtils.COLOR_CODEC.optionalFieldOf("color")
				.forGetter(style -> style.get(StyleProperties.COLOR)),
			CodecUtils.COLOR_CODEC.optionalFieldOf("background_color")
				.forGetter(style -> style.get(StyleProperties.BACKGROUND_COLOR)),
			DecorationsIntermediate.CODEC.optionalFieldOf("text_styles")
				.forGetter(DecorationsIntermediate::of),
			MarginsIntermediate.CODEC.optionalFieldOf("margins")
				.forGetter(MarginsIntermediate::of)
		).apply(inst, (size, color, backgroundColor, decorations, margins) -> {
			LayoutStyle style = new LayoutStyle();
			
			size.ifPresent(theSize -> style.put(StyleProperties.SIZE, theSize));
			color.ifPresent(theColor -> style.put(StyleProperties.COLOR, theColor));
			backgroundColor.ifPresent(theColor -> style.put(StyleProperties.BACKGROUND_COLOR, theColor));
			decorations.ifPresent(theDecorations -> theDecorations.applyTo(style));
			margins.ifPresent(theMargins -> theMargins.applyTo(style));
			
			return style;
		}));
	
	private final Map<StyleProperty<?>, Object> properties = new HashMap<>();
	
	private LayoutStyle() {}
	
	public static LayoutStyle empty() {
		return new LayoutStyle();
	}
	
	public static LayoutStyle defaulted() {
		LayoutStyle style = LayoutStyle.empty();

		style.put(StyleProperties.SIZE, 1.0f);
		style.put(StyleProperties.COLOR, -1);
		style.put(StyleProperties.BACKGROUND_COLOR, 0);
		
		style.put(StyleProperties.BOLD, false);
		style.put(StyleProperties.ITALIC, false);
		style.put(StyleProperties.UNDERLINE, false);
		style.put(StyleProperties.STRIKETHROUGH, false);
		style.put(StyleProperties.SHADOW, false);
		
		style.put(StyleProperties.MARGIN_TOP, 0);
		style.put(StyleProperties.MARGIN_BOTTOM, 0);
		style.put(StyleProperties.MARGIN_RIGHT, 0);
		style.put(StyleProperties.MARGIN_LEFT, 0);
		
		assert style.properties.size() == StyleProperties.PROPERTIES.size();
		
		return style;
	}
	
	public <T extends Comparable<T>> void put(@NotNull StyleProperty<T> property, @NotNull T value) {
		if (!property.isCompatible(value)) {
			throw new IllegalArgumentException("Invalid value type for property '" + property.getName() + "'!");
		}
		
		if (!property.isValid(value)) {
			throw new IllegalArgumentException("Value is out of range for property '" + property.getName() + "'!");
		}
		
		this.properties.put(property, value);
	}
	
	public <T extends Comparable<T>> Optional<T> get(StyleProperty<T> property) {
		// noinspection unchecked
		T value = (T) this.properties.getOrDefault(property, null);
		return Optional.ofNullable(value);
	}
	
	public <T extends Comparable<T>> T getOrDefault(StyleProperty<T> property, T defaultValue) {
		return this.get(property).orElse(defaultValue);
	}
	
	public Style asStyle() {
		Style style = Style.EMPTY;
		
		Optional<Boolean> bold = this.get(StyleProperties.BOLD);
		Optional<Boolean> italic = this.get(StyleProperties.ITALIC);
		Optional<Boolean> underline = this.get(StyleProperties.UNDERLINE);
		Optional<Boolean> strikethrough = this.get(StyleProperties.STRIKETHROUGH);
		Optional<Boolean> shadow = this.get(StyleProperties.SHADOW);
		
		if (bold.isPresent()) style = style.withBold(bold.get());
		if (italic.isPresent()) style = style.withItalic(italic.get());
		if (underline.isPresent()) style = style.withUnderline(underline.get());
		if (strikethrough.isPresent()) style = style.withStrikethrough(strikethrough.get());
		if (shadow.isPresent() && shadow.get()) style = style.withShadowColor(0);

		Optional<Integer> color = this.get(StyleProperties.COLOR);
		if (color.isPresent()) style = style.withColor(color.get());
		
		return style;
	}
	
	public int applyScale(int value) {
		return value;
	}
	
	public int getTextWidth(String string, TextRenderer font) {
		return getTextWidth(Text.literal(string).setStyle(this.asStyle()), font);
	}
	
	public int getTextWidth(Text text, TextRenderer font) {
		return this.applyScale(font.getWidth(text));
	}

	/**
	 * Sets missing properties from the provided LayoutStyle. If a key is in neither style, it will still not be
	 * present after the call.
	 * 
	 * @param defaults The layout style to use as defaults
	 */
	public void applyDefaults(LayoutStyle defaults) {
		for (Map.Entry<StyleProperty<?>, Object> entry : defaults.properties.entrySet()) {
			if (this.properties.containsKey(entry.getKey())) continue;
			
			this.properties.put(entry.getKey(), entry.getValue());
		}
	}

	public LayoutStyle copy() {
		LayoutStyle copy = LayoutStyle.empty();
		copy.properties.putAll(this.properties);
		return copy;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LayoutStyle[");
		
		boolean first = true;
		for (Map.Entry<StyleProperty<?>, Object> entry : this.properties.entrySet()) {
			if (first) {
				first = false;
			} else {
				builder.append(", ");
			}
			
			builder.append(entry.getKey().getName());
			builder.append("=");
			builder.append(entry.getValue());
		}
		
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof LayoutStyle other)) return false;
		return Objects.equals(this.properties, other.properties);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.properties);
	}
}
