package blue.endless.enoki.resource;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class StyleManager {
	private static final String RESOURCE_PATH = "markdown/styles";
	private static final String PATH_PREFIX = RESOURCE_PATH + "/";
	private static final String PATH_SUFFIX = ".json";
	
	protected final Map<Identifier, LayoutStyleSheet> styleSheets = new HashMap<>();
	
	public Optional<LayoutStyleSheet> getStyleSheet(Identifier id) {
		return Optional.ofNullable(styleSheets.getOrDefault(id, null))
			.map(LayoutStyleSheet::copy);
	}
	
	public void registerStyleSheet(Identifier id, LayoutStyleSheet style) {
		styleSheets.put(id, style);
	}
	
	public void registerStyleSheet(Map.Entry<Identifier, LayoutStyleSheet> entry) {
		this.registerStyleSheet(entry.getKey(), entry.getValue());
	}

	public ReloadListener getReloadListener() {
		return new ReloadListener();
	}

	public class ReloadListener implements SimpleResourceReloadListener<Map<Identifier, LayoutStyleSheet>> {
		private static final Logger LOGGER = LogManager.getLogger();
		
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

		public void reload(ResourceManager manager) {
			
		}
		
		private Map.Entry<Identifier, LayoutStyleSheet> processResource(Identifier resourceId, Resource resource) {
			DataResult<Map.Entry<Identifier, LayoutStyleSheet>> r = loadStyleSheet(resourceId, resource);
			return r.getOrThrow();
			//	.ifSuccess(StyleManager.this::registerStyleSheet)
			//	.ifError(error -> {
			//		LOGGER.error("Failed to load style sheet for {}:", resourceId);
			//		LOGGER.error(error);
			//	});
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
		public CompletableFuture<Map<Identifier, LayoutStyleSheet>> load(ResourceManager manager, Executor executor) {
			
			return CompletableFuture.supplyAsync(() -> {
				Map<Identifier, Resource> resources = manager.findResources(
					"markdown",
					//RESOURCE_PATH,
					(it) -> true
					//identifier -> identifier.getPath().endsWith(PATH_SUFFIX)
				);
				LOGGER.info("Reload firing. " + resources.size() + " resources found.");
				
				Map<Identifier, LayoutStyleSheet> results = new HashMap<>();
				for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
					try {
						Map.Entry<Identifier, LayoutStyleSheet> result = processResource(entry.getKey(), entry.getValue());
						results.put(result.getKey(), result.getValue());
					} catch (Throwable t) {
						LOGGER.error("Failed to load style sheet for {}:", entry.getKey());
					}
					
				}
				
				return results;
			}, executor);
		}
		
		@Override
		public CompletableFuture<Void> apply(Map<Identifier, LayoutStyleSheet> data, ResourceManager manager, Executor executor) {
			return CompletableFuture.runAsync(() -> {
				styleSheets.clear();
				
				for(Map.Entry<Identifier, LayoutStyleSheet> entry : data.entrySet()) {
					styleSheets.put(entry.getKey(), entry.getValue());
				}
			}, executor);
		}
		
	}
}
