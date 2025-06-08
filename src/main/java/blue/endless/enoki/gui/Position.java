package blue.endless.enoki.gui;

import blue.endless.enoki.text.ScreenAxis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record Position(int x, int y) {
	public static final Position ZERO = new Position(0, 0);
	
	public int get(ScreenAxis axis) {
		return switch(axis) {
			case HORIZONTAL -> x;
			case VERTICAL -> y;
		};
	}
	
	public static Position of(int x, int y) {
		return new Position(x, y);
	}
	
	public Position withOffset(int x, int y) {
		return new Position(this.x + x, this.y + y);
	}
	
	public Position withOffset(ScreenAxis axis, int distance) {
		return new Position(
				axis.xOffset() * distance + x,
				axis.yOffset() * distance + y
				);
	}
}
