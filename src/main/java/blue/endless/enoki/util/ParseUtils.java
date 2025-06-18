package blue.endless.enoki.util;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A utility class containing parsing utilities.
 */
public class ParseUtils {
	private ParseUtils() {}
	
	/**
	 * Parses an {@code int} from a given string. {@code str} can be null or an invalid number, in which case
	 * {@code defaultValue} will be returned.
	 *
	 * @param str The string to parse, or null
	 * @param defaultValue The default attributes
	 * @return The parsed int, or {@code defaultValue} if it couldn't be parsed
	 */
	public static int parseIntOrDefault(@Nullable String str, int defaultValue) {
		try {
			if (str == null) return defaultValue;
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	/**
	 * Parses a string into a boolean. If the string is {@code null}, {@code true} will be returned.
	 * Parsing is case-insensitive.
	 * <p>
	 * Accepted values for {@code true}: {@code true}, {@code yes}, {@code y}
	 * <p>
	 * Accepted values for {@code false}: {@code false}, {@code no}, {@code n}
	 * 
	 * @param str The string to parse
	 * @return {@code true}/{@code false} if parsing succeeded, {@code null} otherwise
	 */
	public static Optional<Boolean> tryParseBoolean(@Nullable String str) {
		if (str == null) return Optional.of(true);
		
		if (str.equalsIgnoreCase("true")
			|| str.equalsIgnoreCase("yes")
			|| str.equalsIgnoreCase("y")) return Optional.of(true);
		
		if (str.equalsIgnoreCase("false")
			|| str.equalsIgnoreCase("no")
			|| str.equalsIgnoreCase("n")) return Optional.of(false);
		
		return Optional.empty();
	}
}
