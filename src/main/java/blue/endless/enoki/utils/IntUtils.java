package blue.endless.enoki.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class IntUtils {
	/**
	 * Parses an {@code int} from a given string. {@code str} can be null or an invalid number, in which case
	 * {@code defaultValue} will be returned.
	 * 
	 * @param str The string to parse, or null
	 * @param defaultValue The default attributes
	 * @return The parsed int, or {@code defaultValue} if it couldn't be parsed
	 */
	@Contract(pure = true)
	public static int parseIntOrDefault(@Nullable String str, int defaultValue) {
		try {
			if (str == null) return defaultValue;
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
}
