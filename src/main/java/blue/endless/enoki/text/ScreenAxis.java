package blue.endless.enoki.text;

import blue.endless.enoki.gui.Position;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.Widget;

@Environment(EnvType.CLIENT)
public enum ScreenAxis {
	HORIZONTAL(1, 0),
	VERTICAL(0, 1);
	
	private final int xOffset;
	private final int yOffset;
	
	ScreenAxis(int xOffset, int yOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	
	public int xOffset() {
		return this.xOffset;
	}
	
	public int yOffset() {
		return this.yOffset;
	}
	
	public Position orientCoordinates(int primaryAxis, int crossAxis) {
		return switch(this) {
			case HORIZONTAL -> new Position(primaryAxis, crossAxis);
			case VERTICAL -> new Position(crossAxis, primaryAxis);
		};
	}
	
	public int select(int x, int y) {
		return switch(this) {
			case HORIZONTAL -> x;
			case VERTICAL -> y;
		};
	}
	
	public int getSize(Widget w) {
		return switch(this) {
			case HORIZONTAL -> w.getWidth();
			case VERTICAL -> w.getHeight();
		};
	}
	
	public ScreenAxis opposite() {
		return switch(this) {
			case HORIZONTAL -> VERTICAL;
			case VERTICAL -> HORIZONTAL;
		};
	}
}
