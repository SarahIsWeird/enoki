package blue.endless.enoki.resource;

import blue.endless.enoki.markdown.styles.LayoutStyleSheet;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
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

public class StyleManager {
	private static final String PATH_PREFIX = "markdown/styles";
	private static final String PATH_SUFFIX = ".json";
	
	protected final Map<Identifier, LayoutStyleSheet> styleSheets = new HashMap<>();
	
	public Optional<LayoutStyleSheet> getStyleSheet(Identifier id) {
		return Optional.ofNullable(styleSheets.getOrDefault(id, null));
	}
	
	public void putStyleSheet(Identifier id, LayoutStyleSheet style) {
		styleSheets.put(id, style);
	}

	public ReloadListener getReloadListener() {
		return new ReloadListener();
	}

	public class ReloadListener implements SimpleSynchronousResourceReloadListener {
		private static final Logger LOGGER = LogManager.getLogger();
		
		@Override
		public Identifier getFabricId() {
			return Identifier.of("enoki", "style_manager_reload_listener");
		}
		
		private static Optional<Identifier> resolveStyleId(Identifier resourceId) {
			String path = resourceId.getPath();
			if (!path.startsWith(PATH_PREFIX + "/") || !path.endsWith(PATH_SUFFIX)) {
				return Optional.empty();
			}
			
			path = path.substring(PATH_PREFIX.length() + 1); // Account for slash
			path = path.substring(0, path.length() - PATH_SUFFIX.length());
			
			if (path.isEmpty()) return Optional.empty();
			return Optional.of(resourceId.withPath(path));
		}

		@Override
		public void reload(ResourceManager manager) {
			Map<Identifier, Resource> resources = manager.findResources(PATH_PREFIX,
				identifier -> identifier.getPath().endsWith(PATH_SUFFIX));
			
			for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
				Optional<Identifier> idResult = resolveStyleId(entry.getKey());
				if (idResult.isEmpty()) {
					LOGGER.error("Invalid style json resource id: {}", entry.getKey());
					continue;
				}
				
				Identifier id = idResult.get();
				Resource resource = entry.getValue();
				
				try (BufferedReader reader = resource.getReader()) {
					JsonElement element = JsonHelper.deserialize(reader);
					DataResult<LayoutStyleSheet> result = LayoutStyleSheet.CODEC.parse(JsonOps.INSTANCE, element);
					if (result.isSuccess()) {
						LOGGER.info("Loaded style sheet for {}", id);
						StyleManager.this.styleSheets.put(id, result.getOrThrow());
					} else if (result.error().isPresent()) {
						LOGGER.error("Failed to load style sheet for {}: {}", id, result.error().get());
					} else {
						LOGGER.error("Failed to load style sheet for {}!", id);
					}
				} catch (IOException e) {
					LOGGER.error("Failed to load style sheet for {}!", id, e);
				}
			}
		}
	}
}
