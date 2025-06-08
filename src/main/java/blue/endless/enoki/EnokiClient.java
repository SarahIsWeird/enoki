package blue.endless.enoki;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class EnokiClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
			.registerReloadListener(new MarkdownResourceReloadListener());
	}
}
