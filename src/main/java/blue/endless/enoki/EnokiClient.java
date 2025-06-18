package blue.endless.enoki;

import blue.endless.enoki.markdown.DocNode;
import blue.endless.enoki.markdown.styles.LayoutStyleSheet;
import blue.endless.enoki.resource.LocalizedResourceManager;
import blue.endless.enoki.resource.style.StyleRegistry;
import blue.endless.enoki.resource.style.StyleReloadListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class EnokiClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Enoki");

	/**
	 * The global style registry. If you put your style sheets into {@code assets/<your mod id>/enoki/styles},
	 * then this registry can safely be ignored. If you load your styles from some other place manually, then
	 * you have to register them in this object.
	 * 
	 * @see StyleRegistry#registerStyleSheet(Identifier, LayoutStyleSheet) Registering custom style sheets
	 */
	public static final StyleRegistry STYLES = new StyleRegistry();
	
	@Override
	public void onInitializeClient() {
		LOGGER.info("***************************CLIENT INIT*****************************");
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
			.registerReloadListener(new StyleReloadListener(STYLES));
	}

	/**
	 * Registers a new client-side resource manager for Markdown documents.
	 * <p>
	 * For example, if the manager identifier is {@code my_mod:my_folder}, then this resource manager will load
	 * all documents in {@code assets/<any mod id>/my_mod/my_folder} and its sub-folders.
	 * 
	 * @param id The manager id
	 * @return A resource manager for Markdown documents
	 */
	public static LocalizedResourceManager<DocNode> registerResourceManager(Identifier id) {
		LocalizedResourceManager<DocNode> result = new LocalizedResourceManager<>();
		result.reloaderBuilder(ResourceType.CLIENT_RESOURCES)
			.id(id)
			.basePath(id.getNamespace() + "/" + id.getPath())
			.resourcePredicate(path -> path.getPath().endsWith(".md"))
			.decoder(Enoki.DEFAULT_DECODER)
			.register();
		
		return result;
	}
}
