package blue.endless.enoki;

import blue.endless.enoki.resource.style.StyleReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blue.endless.enoki.markdown.DocNode;
import blue.endless.enoki.resource.LocalizedResourceManager;
import blue.endless.enoki.resource.style.StyleRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class EnokiClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Enoki");
	
	public static final StyleRegistry STYLES = new StyleRegistry();
	
	@Override
	public void onInitializeClient() {
		LOGGER.info("***************************CLIENT INIT*****************************");
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
			.registerReloadListener(new StyleReloadListener(STYLES));
	}
	
	public static LocalizedResourceManager<DocNode> registerResourceManager(Identifier id) {
		LocalizedResourceManager<DocNode> result = new LocalizedResourceManager<>();
		result.reloader(ResourceType.CLIENT_RESOURCES)
			.id(id)
			.basePath(id.getNamespace() + "/" + id.getPath())
			.resourcePredicate(path -> path.getPath().endsWith(".md"))
			.decoder(Enoki.DEFAULT_DECODER)
			.register();
		
		return result;
	}
}
