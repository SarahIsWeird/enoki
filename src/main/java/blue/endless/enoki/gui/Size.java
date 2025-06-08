package blue.endless.enoki.gui;

public record Size(int width, int height) {
	public static final Size DEFAULT = new Size(-1, -1);

	public Size scale(float factor) {
		return new Size((int) (width * factor),  (int) (height * factor));
	}
}
