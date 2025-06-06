package blue.endless.enoki.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record BlockContext(int x, int y, int width) {
}
