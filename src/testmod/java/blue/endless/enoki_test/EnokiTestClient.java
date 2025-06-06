package blue.endless.enoki_test;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class EnokiTestClient implements ClientModInitializer {
	private KeyBinding keyBinding;
	
	@Override
	public void onInitializeClient() {
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"test.keybind.test",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_M,
				"test.keybind.test"
				));
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (keyBinding.isPressed()) {
				MinecraftClient.getInstance().setScreen(new TestScreen());
			}
		});
	}
}
