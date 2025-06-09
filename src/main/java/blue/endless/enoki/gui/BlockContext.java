package blue.endless.enoki.gui;

import blue.endless.enoki.text.LineLayout;
import blue.endless.enoki.text.ScreenAxis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record BlockContext(int x, int y, int width, LineLayout line) {
	BlockContext(int x, int y, int width) {
		this(x, y, width, new LineLayout(new Position(x, y), width, ScreenAxis.HORIZONTAL));
	}
}
