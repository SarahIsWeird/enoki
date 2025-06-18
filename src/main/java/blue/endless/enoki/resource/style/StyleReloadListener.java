package blue.endless.enoki.resource.style;

import blue.endless.enoki.Enoki;
import blue.endless.enoki.markdown.styles.LayoutStyleSheet;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@ApiStatus.Internal
public class StyleReloadListener implements SimpleResourceReloadListener<Map<Identifier, LayoutStyleSheet>> {
	private static final String RESOURCE_PATH = Enoki.MOD_ID + "/styles";
	private static final String PATH_PREFIX = RESOURCE_PATH + "/";
	private static final String PATH_SUFFIX = ".json";
	
	private static final Logger LOGGER = LogManager.getLogger("EnokiStyleReloadListener");
	
	private final StyleRegistry styleRegistry;
	
	public StyleReloadListener(StyleRegistry styleRegistry) {
		this.styleRegistry = styleRegistry;
	}

	@Override
	public Identifier getFabricId() {
		return Identifier.of("enoki", "style_sheet_loader");
	}

	private static Optional<Identifier> resolveStyleId(Identifier resourceId) {
		String path = resourceId.getPath();
		if (!path.startsWith(PATH_PREFIX) || !path.endsWith(PATH_SUFFIX)) {
			return Optional.empty();
		}

		path = path.substring(PATH_PREFIX.length());
		path = path.substring(0, path.length() - PATH_SUFFIX.length());

		if (path.isEmpty()) return Optional.empty();
		return Optional.of(resourceId.withPath(path));
	}

	@Override
	public CompletableFuture<Map<Identifier, LayoutStyleSheet>> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, Resource> resources = manager.findResources(
				RESOURCE_PATH,
				identifier -> identifier.getPath().endsWith(PATH_SUFFIX)
			);
			
			Map<Identifier, LayoutStyleSheet> sheets = HashMap.newHashMap(resources.size());
			for (Map.Entry<Identifier, Resource> resourceEntry : resources.entrySet()) {
				Identifier resourceId = resourceEntry.getKey();
				Resource resource = resourceEntry.getValue();
				
				loadStyleSheet(resourceId, resource)
					.ifSuccess(entry -> {
						sheets.put(entry.getKey(), entry.getValue());
						LOGGER.info("Loaded style sheet {}.", entry.getKey());
					})
					.ifError(error -> {
						LOGGER.error("Failed to load style sheet for resource id {}:", resourceId);
						LOGGER.error(error);
					});
			}
			
			return sheets;
		}, executor);
	}

	private DataResult<Map.Entry<Identifier, LayoutStyleSheet>> loadStyleSheet(Identifier resourceId, Resource resource) {
		Optional<Identifier> idResult = resolveStyleId(resourceId);
		if (idResult.isEmpty()) {
			return DataResult.error(() -> "Invalid resource id for style sheet: %s".formatted(resourceId));
		}

		Identifier styleId = idResult.get();

		try (BufferedReader reader = resource.getReader()) {
			JsonElement element = JsonHelper.deserialize(reader);
			return LayoutStyleSheet.CODEC.parse(JsonOps.INSTANCE, element)
				.map(sheet -> Map.entry(styleId, sheet));
		} catch (IOException e) {
			return DataResult.error(e::toString);
		}
	}

	@Override
	public CompletableFuture<Void> apply(Map<Identifier, LayoutStyleSheet> newSheets, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			this.styleRegistry.clear();
			newSheets.forEach(this.styleRegistry::registerStyleSheet);

			LOGGER.info("Registered {} style sheets.", newSheets.size());
		}, executor);
	}
}
