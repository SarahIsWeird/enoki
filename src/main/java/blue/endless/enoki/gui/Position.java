package blue.endless.enoki.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ScreenPos;

@Environment(EnvType.CLIENT)
public record Position(int x, int y) {
	public static final Position ZERO = new Position(0, 0);
	
	public static Position of(int x, int y) {
		return new Position(x, y);
	}
	
	public Position withOffset(int x, int y) {
		return new Position(this.x + x, this.y + y);
	}
	
	public ScreenPos asScreenPos() {
		return new ScreenPos(x, y);
	}
}
