package blue.endless.enoki.resource;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import blue.endless.enoki.resource.impl.ReloaderBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class LocalizedResourceManager<T> {
	public static final String FALLBACK_LOCALE = "en_us";
	
	protected final Map<String, LocalizedRegistry<T>> registries = new HashMap<>();
	protected final Logger logger;
	protected final T defaultValue;
	
	public LocalizedResourceManager() {
		logger = LoggerFactory.getLogger("LocalizedResourceManager");
		defaultValue = null;
	}
	
	public LocalizedResourceManager(Logger logger) {
		this.logger = logger;
		defaultValue = null;
	}
	
	public LocalizedResourceManager(Logger logger, T defaultValue) {
		this.logger = logger;
		this.defaultValue = defaultValue;
	}
	
	public Optional<T> get(Identifier id, String locale) {
		// Try to get the proper registry first
		LocalizedRegistry<T> localRegistry = registries.get(locale);
		if (localRegistry != null) {
			T result = localRegistry.get(id);
			if (result != null) return Optional.of(result);
		}
		
		LocalizedRegistry<T> fallbackRegistry = registries.get(FALLBACK_LOCALE);
		if (fallbackRegistry != null) {
			T result = fallbackRegistry.get(id);
			if (result != null) return Optional.of(result);
		}
		
		return Optional.fromNullable(defaultValue);
	}
	
	@Environment(EnvType.CLIENT)
	public Optional<T> get(Identifier id) {
		String locale = MinecraftClient.getInstance().getLanguageManager().getLanguage();
		return get(id, locale);
	}
	
	public ReloaderBuilder<T> reloader(ResourceType type) {
		return new ReloaderBuilder<T>(type, logger, registries);
	}
}
