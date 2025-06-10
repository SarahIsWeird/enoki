package blue.endless.enoki;

import blue.endless.enoki.markdown.DocNode;
import blue.endless.enoki.resource.LocalizedResourceManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class EnokiClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
	}
	
	public static LocalizedResourceManager<DocNode> registerResourceManager(Identifier id) {
		LocalizedResourceManager<DocNode> result = new LocalizedResourceManager<>();
		result.reloader(ResourceType.CLIENT_RESOURCES)
			.id(id)
			.basePath(id.getNamespace() + "/" + id.getPath())
			.resourcePredicate((it) -> it.getPath().endsWith(".md"))
			.decoder(Enoki.DEFAULT_DECODER)
			.register();
		
		return result;
	}
}
