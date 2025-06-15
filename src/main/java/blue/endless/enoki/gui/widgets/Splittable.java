package blue.endless.enoki.gui.widgets;

import org.jetbrains.annotations.Nullable;

public interface Splittable {
	
	/**
	 * Breaks this widget into one or two pieces. Result is guaranteed to have either result, leftover, or both nonnull
	 * @param lineWidth the target width. The wrap will attempt to make lines as close as possible to this width without
	 *                  going over.
	 * @param force     if true, certain checks will be relaxed in order to force *something* to be on this line.
	 * @return          a Splittable.Result indicating a result which "fits" on this line, and a leftover widget that
	 *                  does not.
	 */
	Result split(int lineWidth, boolean force);
	
	public static record Result(@Nullable AbstractMarkdownWidget result, @Nullable AbstractMarkdownWidget leftover) {
		public static Result everythingFits(AbstractMarkdownWidget everything) {
			return new Result(everything, null);
		}
		
		public static Result nothingFits(AbstractMarkdownWidget nonFitting) {
			return new Result(null, nonFitting);
		}
		
		public boolean success() {
			return result != null;
		}
	};
}
