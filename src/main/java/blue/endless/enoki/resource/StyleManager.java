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

	public class ReloadListener implements SimpleSynchronousResourceReloadListener {
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

		@Override
		public void reload(ResourceManager manager) {
			styleSheets.clear();
			
			Map<Identifier, Resource> resources = manager.findResources(
				RESOURCE_PATH,
				identifier -> identifier.getPath().endsWith(PATH_SUFFIX)
			);
			
			for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
				processResource(entry.getKey(), entry.getValue());
			}
		}
		
		private void processResource(Identifier resourceId, Resource resource) {
			loadStyleSheet(resourceId, resource)
				.ifSuccess(StyleManager.this::registerStyleSheet)
				.ifError(error -> {
					LOGGER.error("Failed to load style sheet for {}:", resourceId);
					LOGGER.error(error);
				});
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
	}
}
