package blue.endless.enoki.markdown.styles.codec_intermediates;

import blue.endless.enoki.markdown.styles.LayoutStyle;
import blue.endless.enoki.markdown.styles.properties.StyleProperties;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.Internal
public record MarginsIntermediate(
	Optional<Integer> top,
	Optional<Integer> right,
	Optional<Integer> bottom,
	Optional<Integer> left
) {
	public static final Codec<MarginsIntermediate> CODEC =
		RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("top").forGetter(MarginsIntermediate::top),
			Codec.INT.optionalFieldOf("right").forGetter(MarginsIntermediate::right),
			Codec.INT.optionalFieldOf("bottom").forGetter(MarginsIntermediate::bottom),
			Codec.INT.optionalFieldOf("left").forGetter(MarginsIntermediate::left)
		).apply(instance, MarginsIntermediate::new));
	
	public static Optional<MarginsIntermediate> of(LayoutStyle provider) {
		return Optional.of(new MarginsIntermediate(
			provider.get(StyleProperties.MARGIN_TOP),
			provider.get(StyleProperties.MARGIN_RIGHT),
			provider.get(StyleProperties.MARGIN_BOTTOM),
			provider.get(StyleProperties.MARGIN_LEFT)
		));
	}
	
	public void applyTo(LayoutStyle receiver) {
		top.ifPresent(top -> receiver.put(StyleProperties.MARGIN_TOP, top));
		right.ifPresent(right -> receiver.put(StyleProperties.MARGIN_RIGHT, right));
		bottom.ifPresent(bottom -> receiver.put(StyleProperties.MARGIN_BOTTOM, bottom));
		left.ifPresent(left -> receiver.put(StyleProperties.MARGIN_LEFT, left));
	}
}
