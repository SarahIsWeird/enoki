package blue.endless.enoki.markdown.styles;

import blue.endless.enoki.markdown.styles.codec_intermediates.MarginsIntermediate;
import blue.endless.enoki.markdown.styles.properties.StyleProperties;
import blue.endless.enoki.markdown.styles.properties.StyleProperty;
import blue.endless.enoki.util.CodecUtils;
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

import static java.util.Objects.requireNonNull;

/**
 * This class represents the style of a single Markdown widget.
 * <p>
 * It is mutable for several reasons, chiefly to avoid creating
 * many instances of maps when building the style, so before it
 * can be used to build the widgets, it should be copied by
 * calling {@link #copy()}.
 */
public class LayoutStyle {
	/**
	 * A chonccy boye.
	 * <p>
	 * This codec codes for {@link StyleProperties#PROPERTIES all style properties}. In essence, it is one
	 * big record codec for all the properties except for margins, which are represented by an
	 * {@link MarginsIntermediate#CODEC internal intermediate codec}. It allows for any and every property
	 * to be absent.
	 * <p>
	 * Previously, text styles such as bold and italic had their own intermediate codec. However, this
	 * actually decreased readability of style sheets instead of improving it.
	 */
	public static final Codec<LayoutStyle> CODEC =
		RecordCodecBuilder.create(inst -> inst.group(
			Codec.FLOAT.optionalFieldOf("size")
				.forGetter(style -> style.get(StyleProperties.SIZE)),
			CodecUtils.COLOR_CODEC.optionalFieldOf("color")
				.forGetter(style -> style.get(StyleProperties.COLOR)),
			CodecUtils.COLOR_CODEC.optionalFieldOf("background_color")
				.forGetter(style -> style.get(StyleProperties.BACKGROUND_COLOR)),
			Codec.BOOL.optionalFieldOf("bold")
				.forGetter(style -> style.get(StyleProperties.BOLD)),
			Codec.BOOL.optionalFieldOf("italic")
				.forGetter(style -> style.get(StyleProperties.ITALIC)),
			Codec.BOOL.optionalFieldOf("underline")
				.forGetter(style -> style.get(StyleProperties.UNDERLINE)),
			Codec.BOOL.optionalFieldOf("strikethrough")
				.forGetter(style -> style.get(StyleProperties.STRIKETHROUGH)),
			Codec.BOOL.optionalFieldOf("shadow")
				.forGetter(style -> style.get(StyleProperties.SHADOW)),
			MarginsIntermediate.CODEC.optionalFieldOf("margin")
				.forGetter(MarginsIntermediate::of)
		).apply(inst, (size,
					   color,
					   backgroundColor,
					   bold,
					   italic,
					   underline,
					   strikethrough,
					   shadow,
					   margins) -> {
			LayoutStyle style = new LayoutStyle();
			
			size.ifPresent(theSize -> style.put(StyleProperties.SIZE, theSize));
			color.ifPresent(theColor -> style.put(StyleProperties.COLOR, theColor));
			backgroundColor.ifPresent(theColor -> style.put(StyleProperties.BACKGROUND_COLOR, theColor));
			bold.ifPresent(theBold -> style.put(StyleProperties.BOLD, theBold));
			italic.ifPresent(theItalic -> style.put(StyleProperties.ITALIC, theItalic));
			underline.ifPresent(theUnderline -> style.put(StyleProperties.UNDERLINE, theUnderline));
			strikethrough.ifPresent(theStrikethrough -> style.put(StyleProperties.STRIKETHROUGH, theStrikethrough));
			shadow.ifPresent(theShadow -> style.put(StyleProperties.SHADOW, theShadow));
			margins.ifPresent(theMargins -> theMargins.applyTo(style));
			
			return style;
		}));
	
	private final Map<StyleProperty<?>, Object> properties = new HashMap<>();
	
	/*
	 * It should be explicitly stated whether the style should be empty or defaulted,
	 * so the constructor is marked private.
	 */
	private LayoutStyle() {}

	/**
	 * Creates a layout style with no style properties set.
	 * 
	 * @return An empty layout style
	 */
	public static LayoutStyle empty() {
		return new LayoutStyle();
	}

	/**
	 * Creates a layout style with all style properties set to their defaults:
	 * <ul>
	 *     <li>A size of {@code 1.0f}</li>
	 *     <li>White text color</li>
	 *     <li>No background color</li>
	 *     <li>No text style</li>
	 *     <li>No margins</li>
	 * </ul>
	 * 
	 * @return A defaulted layout style
	 */
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
		
		// Make sure that all properties are accounted for
		assert style.properties.size() == StyleProperties.PROPERTIES.size();
		
		return style;
	}

	/**
	 * Sets a style property to a value.
	 * 
	 * @param property The property to set the value for
	 * @param value The value to set
	 * @throws IllegalArgumentException if the value is not applicable to this property
	 */
	public <T extends Comparable<T>> void put(@NotNull StyleProperty<T> property, @NotNull T value) {
		if (!property.isCompatible(value)) {
			throw new IllegalArgumentException("Invalid value for property '" + property.getName() + "'!");
		}
		
		this.properties.put(property, value);
	}

	/**
	 * Retrieves a value for a style property.
	 * 
	 * @param property The property to retrieve the value for
	 * @return An {@link Optional} possibly containing the value
	 */
	public <T extends Comparable<T>> Optional<@NotNull T> get(@NotNull StyleProperty<T> property) {
		// noinspection unchecked
		T value = (T) this.properties.getOrDefault(property, null);
		return Optional.ofNullable(value);
	}

	/**
	 * Retrieves a value for a style property, or the default value.
	 * 
	 * @param property The property to retrieve the value for
	 * @param defaultValue The default value to use if the property isn't set
	 * @return The property or the default value
	 */
	@NotNull
	public <T extends Comparable<T>> T getOrDefault(@NotNull StyleProperty<T> property, @NotNull T defaultValue) {
		return this.get(property).orElse(requireNonNull(defaultValue));
	}

	/**
	 * Returns this layout style represented as a text style. Not all Enoki style properties
	 * have an equivalent vanilla style property.
	 * 
	 * @return This layout style represented as a text style.
	 */
	public Style asStyle() {
		Style style = Style.EMPTY;
		
		Optional<@NotNull Boolean> bold = this.get(StyleProperties.BOLD);
		Optional<@NotNull Boolean> italic = this.get(StyleProperties.ITALIC);
		Optional<@NotNull Boolean> underline = this.get(StyleProperties.UNDERLINE);
		Optional<@NotNull Boolean> strikethrough = this.get(StyleProperties.STRIKETHROUGH);
		Optional<@NotNull Boolean> shadow = this.get(StyleProperties.SHADOW);
		
		if (bold.isPresent()) style = style.withBold(bold.get());
		if (italic.isPresent()) style = style.withItalic(italic.get());
		if (underline.isPresent()) style = style.withUnderline(underline.get());
		if (strikethrough.isPresent()) style = style.withStrikethrough(strikethrough.get());
		if (shadow.isPresent() && shadow.get()) style = style.withShadowColor(0);

		Optional<Integer> color = this.get(StyleProperties.COLOR);
		if (color.isPresent()) style = style.withColor(color.get());
		
		return style;
	}

	/**
	 * Applies the scale of this style to the given value. If unset, the same value is returned.
	 * 
	 * @param value The value to scale
	 * @return The scaled value
	 */
	public int applyScale(int value) {
		return (int) (value * this.getOrDefault(StyleProperties.SIZE, 1f));
	}

	/**
	 * Calculates the <b>scaled</b> text width, according to the specified text renderer.
	 * <p>
	 * The supplied string is styled according to this object before calculation.
	 * 
	 * @param string The string to calculate the width of
	 * @param font The font renderer which will be used to render the text
	 * @return The text width in GUI units
	 * @see #asStyle()
	 * @see #getTextWidth(Text, TextRenderer)
	 */
	public int getTextWidth(@NotNull String string, @NotNull TextRenderer font) {
		return this.applyScale(getTextWidth(Text.literal(string).setStyle(this.asStyle()), font));
	}

	/**
	 * Calculates the <b>scaled</b> text width, according to the specified text renderer.
	 * 
	 * @param text The already-styled text to calculate the width of
	 * @param font The font renderer which will be used to render the text
	 * @return The text width in GUI units
	 * @see #getTextWidth(String, TextRenderer)
	 */
	public int getTextWidth(@NotNull Text text, @NotNull TextRenderer font) {
		return this.applyScale(font.getWidth(text));
	}

	/**
	 * Sets missing properties from the provided LayoutStyle. If a key is in neither style, it will still not be
	 * present after the call.
	 * <p>
	 * Care should be taken to {@link #copy()} this style if merging two styles irrevocably isn't desired. For
	 * performance reasons, this isn't done inside this method.
	 * 
	 * @param defaults The layout style to use as defaults
	 */
	public void applyDefaults(LayoutStyle defaults) {
		for (Map.Entry<StyleProperty<?>, Object> entry : defaults.properties.entrySet()) {
			if (this.properties.containsKey(entry.getKey())) continue;
			
			this.properties.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Copies this style. Doesn't have the same semantics as {@link Object#clone()}.
	 * <p>
	 * Unset values remain unset.
	 * 
	 * @return A copy of this style
	 */
	public LayoutStyle copy() {
		LayoutStyle copy = LayoutStyle.empty();
		copy.properties.putAll(this.properties);
		return copy;
	}

	@Override
	public String toString() {
		// This SB-based impl provides a nice debug view of the layout style:
		// LayoutStyle[bold=true, color=-1]
		
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
		// Auto-generated by IntelliJ.
		
		if (!(object instanceof LayoutStyle other)) return false;
		return Objects.equals(this.properties, other.properties);
	}

	@Override
	public int hashCode() {
		// Auto-generated by IntelliJ.
		
		return Objects.hashCode(this.properties);
	}
}
