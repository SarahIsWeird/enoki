package blue.endless.enoki.markdown.styles.codec_intermediates;

import blue.endless.enoki.markdown.styles.LayoutStyle;
import blue.endless.enoki.markdown.styles.properties.StyleProperties;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.Internal
public record DecorationsIntermediate(
	Optional<Boolean> bold,
	Optional<Boolean> italic,
	Optional<Boolean> underline,
	Optional<Boolean> strikethrough,
	Optional<Boolean> shadow
) {
	public static final Codec<DecorationsIntermediate> CODEC =
		RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("bold")
				.forGetter(DecorationsIntermediate::bold),
			Codec.BOOL.optionalFieldOf("italic")
				.forGetter(DecorationsIntermediate::italic),
			Codec.BOOL.optionalFieldOf("underline")
				.forGetter(DecorationsIntermediate::underline),
			Codec.BOOL.optionalFieldOf("strikethrough")
				.forGetter(DecorationsIntermediate::strikethrough),
			Codec.BOOL.optionalFieldOf("shadow")
				.forGetter(DecorationsIntermediate::shadow)
		).apply(instance, DecorationsIntermediate::new));
	
	public static Optional<DecorationsIntermediate> of(LayoutStyle provider) {
		return Optional.of(new DecorationsIntermediate(
			provider.get(StyleProperties.BOLD),
			provider.get(StyleProperties.ITALIC),
			provider.get(StyleProperties.UNDERLINE),
			provider.get(StyleProperties.STRIKETHROUGH),
			provider.get(StyleProperties.SHADOW)
		));
	}

	public void applyTo(LayoutStyle receiver) {
		bold.ifPresent(bold -> receiver.put(StyleProperties.BOLD, bold));
		italic.ifPresent(italic -> receiver.put(StyleProperties.ITALIC, italic));
		underline.ifPresent(underline -> receiver.put(StyleProperties.UNDERLINE, underline));
		strikethrough.ifPresent(strikethrough -> receiver.put(StyleProperties.STRIKETHROUGH, strikethrough));
		shadow.ifPresent(shadow -> receiver.put(StyleProperties.SHADOW, shadow));
	}
}
