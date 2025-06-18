package blue.endless.enoki.resource;

import blue.endless.enoki.resource.impl.ReloaderBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A resource manager capable of providing resources based on game locales.
 * 
 * @param <T> The resource type the manager provides
 * @see #LocalizedResourceManager(Logger, Object) Creation
 * @see #get(Identifier, String) Resource resolution
 */
public class LocalizedResourceManager<T> {
	/**
	 * The fallback locale used by {@link #get(Identifier, String)}. Since the default language
	 * of Minecraft is US-American English, this is the same.
	 */
	public static final String FALLBACK_LOCALE = "en_us";
	
	protected final Map<String, LocalizedRegistry<T>> registries = new HashMap<>();
	protected final Logger logger;
	protected final T defaultValue;

	/**
	 * Creates a new manager with the default logger and {@code null} as the default value, i.e.,
	 * {@link Optional#empty()} is returned on resolution failure.
	 * 
	 * @see #LocalizedResourceManager(Logger)
	 * @see #LocalizedResourceManager(Logger, T)
	 */
	public LocalizedResourceManager() {
		this(LoggerFactory.getLogger("LocalizedResourceManager"));
	}

	/**
	 * Creates a new manager with the specified logger and {@code null} as the default value, i.e.,
	 * {@link Optional#empty()} is returned on resolution failure.
	 * 
	 * @param logger The logger to use
	 * @see #LocalizedResourceManager(Logger, Object)
	 */
	public LocalizedResourceManager(Logger logger) {
		this(logger, null);
	}

	/**
	 * Creates a new manager. If {@code defaultValue} is {@code null}, then {@link #get(Identifier, String)} will return
	 * {@link Optional#empty()}.
	 * 
	 * @param logger The logger to use
	 * @param defaultValue The default value to use
	 */
	public LocalizedResourceManager(Logger logger, T defaultValue) {
		this.logger = logger;
		this.defaultValue = defaultValue;
	}

	/**
	 * Resolves a localized resource by its identifier:
	 * <ol>
	 *     <li>The resource is resolved in the registry for the specified locale.</li>
	 *     <li>If that fails, the resource is resolved in the registry for {@link #FALLBACK_LOCALE}.</li>
	 *     <li>If that fails and {@link #defaultValue} is not null, that is returned.</li>
	 *     <li>Otherwise, {@link Optional#empty()} is returned.</li>
	 * </ol>
	 * 
	 * @param id The identifier to resolve
	 * @param locale The desired locale
	 * @return An {@link Optional} possibly containing a version of the resource
	 */
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
		
		return Optional.ofNullable(defaultValue);
	}

	/**
	 * Resolves a localized resource by its identifier using the current in-game locale. The resolution
	 * steps are explained {@link #get(Identifier, String) here}.
	 * 
	 * @param id The identifier to resolve
	 * @return An {@link Optional} possibly containing a version of the resource
	 * @see #get(Identifier, String)
	 */
	@Environment(EnvType.CLIENT)
	public Optional<T> get(Identifier id) {
		String locale = MinecraftClient.getInstance().getLanguageManager().getLanguage();
		return get(id, locale);
	}

	/**
	 * Creates a new {@link ReloaderBuilder} for this resource manager.
	 * 
	 * @param type The resource type to create the builder for
	 * @return A builder instance
	 */
	public ReloaderBuilder<T> reloaderBuilder(ResourceType type) {
		return new ReloaderBuilder<>(type, logger, registries);
	}
}
