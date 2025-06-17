package blue.endless.enoki.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.dynamic.Codecs;

public class CodecUtils {
	private static String colorToHexCode(int value) {
		if (value >= 0xff000000) {
			return String.format("#%08x", value);
		}
		
		return String.format("#%06x", value);
	}
	
	private static DataResult<Integer> hexComponentToInt(String hex) {
		try {
			int component = Integer.parseInt(hex, 16);
			if (hex.length() == 1) {
				component = (component << 8) + component;
			}
			
			return DataResult.success(component);
		} catch (NumberFormatException e) {
			return DataResult.error(() -> "'%s' is not a valid color component!".formatted(hex));
		}
	}
	
	private static DataResult<Integer> hexColorToInt(String hexCode) {
		if (!hexCode.startsWith("#")) hexCode = hexCode.substring(1);
		
		var alpha = DataResult.success(255);
		var red = DataResult.success(0);
		var green = DataResult.success(0);
		var blue = DataResult.success(0);

		switch (hexCode.length()) {
			case 3 -> {
				red = hexComponentToInt(hexCode.substring(0, 1));
				green = hexComponentToInt(hexCode.substring(1, 2));
				blue = hexComponentToInt(hexCode.substring(2, 3));
			}
			case 4 -> {
				alpha = hexComponentToInt(hexCode.substring(0, 1));
				red = hexComponentToInt(hexCode.substring(1, 2));
				green = hexComponentToInt(hexCode.substring(2, 3));
				blue = hexComponentToInt(hexCode.substring(3, 4));
			}
			case 6 -> {
				red = hexComponentToInt(hexCode.substring(0, 2));
				green = hexComponentToInt(hexCode.substring(2, 4));
				blue = hexComponentToInt(hexCode.substring(4, 6));
			}
			case 8 -> {
				alpha = hexComponentToInt(hexCode.substring(0, 2));
				red = hexComponentToInt(hexCode.substring(2, 4));
				green = hexComponentToInt(hexCode.substring(4, 6));
				blue = hexComponentToInt(hexCode.substring(6, 8));
			}
			default -> {
				// Can't have non-finals in lambdas! Formatting it beforehand is fine, it's not an expensive op.
				final String errorMessage = "Hex color '%s' has an invalid length!".formatted(hexCode);
				return DataResult.error(() -> errorMessage);
			}
		}
		
		if (alpha.isError()) return alpha;
		if (red.isError()) return red;
		if (green.isError()) return green;
		if (blue.isError()) return blue;
		
		int color = alpha.getOrThrow() << 24 | red.getOrThrow() << 16 | green.getOrThrow() << 8 | blue.getOrThrow();
		return DataResult.success(color);
	}
	
	private static final Codec<Integer> HEX_COLOR_CODEC = Codec.STRING.comapFlatMap(
		CodecUtils::hexColorToInt, CodecUtils::colorToHexCode);
	
	public static final Codec<Integer> COLOR_CODEC = Codec.withAlternative(Codecs.ARGB, HEX_COLOR_CODEC);
}
