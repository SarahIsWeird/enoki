package blue.endless.enoki.gui;

public record Margins(int top, int right, int bottom, int left) {
	public static final Margins ZERO = Margins.of(0);
	public static final Margins DOCUMENT = Margins.of(8);
	public static final Margins PAD_BELOW = new Margins(0, 0, 4, 0);
	
	public static Margins of(int width) {
		return new Margins(width, width, width, width);
	}
}
