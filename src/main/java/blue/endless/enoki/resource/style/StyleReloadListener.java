package blue.endless.enoki.resource.style;

import blue.endless.enoki.Enoki;
import blue.endless.enoki.markdown.styles.LayoutStyleSheet;
import blue.endless.enoki.util.resources.AsyncResourceReloader;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

@ApiStatus.Internal
public class StyleReloadListener extends AsyncResourceReloader<LayoutStyleSheet> {
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

	@Override
	protected String getResourcePath() {
		return RESOURCE_PATH;
	}

	@Override
	protected boolean shouldLoad(Identifier resourceId) {
		return resourceId.getPath().endsWith(PATH_SUFFIX);
	}

	@Override
	protected Optional<Identifier> toRegistryId(Identifier resourceId) {
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
	protected Optional<LayoutStyleSheet> load(Identifier id, Resource resource) {
		JsonElement element;
		try (BufferedReader reader = resource.getReader()) {
			element = JsonHelper.deserialize(reader);
		} catch (IOException e) {
			LOGGER.error("Failed to load style sheet for resource id {}:", id, e);
			return Optional.empty();
		}
		
		return LayoutStyleSheet.CODEC.parse(JsonOps.INSTANCE, element)
			.ifSuccess(sheet -> LOGGER.info("Loaded style sheet {}.", id))
			.ifError(error -> {
				LOGGER.error("Failed to load style sheet for id {}:", id);
				LOGGER.error(error);
			})
			.result();
	}

	@Override
	protected void beforeApply() {
		this.styleRegistry.clear();
	}

	@Override
	public void apply(Identifier id, LayoutStyleSheet styleSheet) {
		this.styleRegistry.registerStyleSheet(id,  styleSheet);
	}
}
