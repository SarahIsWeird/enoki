package blue.endless.enoki.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.dynamic.Codecs;

/**
 * A utility class containing reusable codecs.
 * 
 * @see #COLOR_CODEC
 */
public class CodecUtils {
	private CodecUtils() {}

	/**
	 * A codec capable of parsing ARGB colors represented as hexadecimal. The value is read and written as a string.
	 * <p>
	 * This codec shouldn't be used directly.
	 * 
	 * @see #COLOR_CODEC
	 */
	private static final Codec<Integer> HEX_COLOR_CODEC = Codec.STRING.comapFlatMap(
		CodecUtils::hexColorToInt, CodecUtils::colorToHexCode);

	/**
	 * A codec extending the vanilla {@link Codecs#ARGB}.
	 * It is capable of parsing ARGB colors represented in many different ways:
	 * <ul>
	 *     <li>An integer in the ARGB format (like in vanilla)</li>
	 *     <li>A four-component vector with values in the range {@code (0.0, 1.0)}</li>
	 *     <li>
	 *         A hexadecimal color string, with or without a leading {@code #}.
	 *         <p>
	 *         These string representations are valid:
	 *         <ul>
	 *             <li>{@code AARRGGBB}</li>
	 *             <li>{@code ARGB}, like above, but with the components duplicated like in CSS</li>
	 *             <li>{@code RRGGBB}, with alpha set to {@code ff}={@code 255}</li>
	 *             <li>{@code RGB}, like above, but with the components duplicated</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 * 
	 * The value is read as either a string or an integer, and written as an integer.
	 * 
	 * @see Codecs#ARGB
	 */
	public static final Codec<Integer> COLOR_CODEC = Codec.withAlternative(Codecs.ARGB, HEX_COLOR_CODEC);
	
	/**
	 * Converts an ARGB color in integer format to it's corresponding lowercase ARGB hex code.
	 * 
	 * @param value The ARGB representation of the color
	 * @return The hex code representation of the color
	 */
	private static String colorToHexCode(int value) {
		if (value >= 0xff000000) {
			return String.format("#%08x", value);
		}
		
		return String.format("#%06x", value);
	}

	/**
	 * Parses a single color component into an integer. Parsing is case-insensitive.
	 * <p>
	 * If the component is only one character long, it is duplicated like it would be in HTML/CSS,
	 * e.g., {@code f} becomes {@code ff}.
	 * 
	 * @param hex The component to parse
	 * @return A {@link DataResult} containing either the parsed component or an error message.
	 */
	private static DataResult<Integer> hexComponentToInt(String hex) {
		if (hex.length() > 2) {
			return DataResult.error(() ->
				"'%s' is not a valid color component, as it has to be one or two characters long.".formatted(hex)
			);
		}
		
		try {
			int component = Integer.parseInt(hex, 16);
			if (hex.length() == 1) {
				// f -> ff
				component = (component << 8) + component;
			}
			
			return DataResult.success(component);
		} catch (NumberFormatException e) {
			return DataResult.error(() -> "'%s' is not a valid color component!".formatted(hex));
		}
	}

	/**
	 * Parses a hexadecimal color code into its integer representation. The string can have a leading {@code #}, which
	 * will be removed before parsing, but it is not mandatory. Parsing is case-insensitive.
	 * <p>
	 * The function accepts several formats:
	 * <ul>
	 * <li>{@code #AARRGGBB}</li>
	 * <li>{@code #ARGB}, which is parsed as {@code AARRGGBB}, similar to CSS</li>
	 * <li>{@code #RRGGBB}, where the alpha is set to {@code ff}={@code 255}</li>
	 * <li>{@code #RGB}, which is parsed as {@code RRGGBB}</li>
	 * </ul>
	 * 
	 * @param hexCode The string to parse
	 * @return A {@link DataResult} with either the color integer or an error message.
	 */
	private static DataResult<Integer> hexColorToInt(String hexCode) {
		if (hexCode.startsWith("#")) hexCode = hexCode.substring(1);

		DataResult<Integer> alpha = DataResult.success(255);
		DataResult<Integer> red;
		DataResult<Integer> green;
		DataResult<Integer> blue;

		switch (hexCode.length()) {
			// RGB
			case 3 -> {
				red = hexComponentToInt(hexCode.substring(0, 1));
				green = hexComponentToInt(hexCode.substring(1, 2));
				blue = hexComponentToInt(hexCode.substring(2, 3));
			}
			// ARGB
			case 4 -> {
				alpha = hexComponentToInt(hexCode.substring(0, 1));
				red = hexComponentToInt(hexCode.substring(1, 2));
				green = hexComponentToInt(hexCode.substring(2, 3));
				blue = hexComponentToInt(hexCode.substring(3, 4));
			}
			// RRGGBB
			case 6 -> {
				red = hexComponentToInt(hexCode.substring(0, 2));
				green = hexComponentToInt(hexCode.substring(2, 4));
				blue = hexComponentToInt(hexCode.substring(4, 6));
			}
			// AARRGGBB
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
}
