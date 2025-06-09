package blue.endless.enoki.resources;

import blue.endless.enoki.gui.Size;
import blue.endless.enoki.markdown.DocNode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Environment(EnvType.CLIENT)
public class MarkdownResources {
	private MarkdownResources() {}

	private static final Map<Identifier, LocalizedMarkdownResource> SOURCES = new HashMap<>();
	private static final Map<Identifier, Size> IMAGE_SIZES = new HashMap<>();
	
	static void clearSources() {
		SOURCES.clear();
	}
	
	static void clearImageSizes() {
		IMAGE_SIZES.clear();
	}

	/**
	 * Returns the cached image size for a given texture id, or {@code null} if unknown.
	 */
	@Nullable
	public static Size getImageSize(Identifier imageId) {
		return IMAGE_SIZES.get(imageId);
	}

	/**
	 * Adds an image size to the image size cache.
	 * <p>
	 * This does not need to be called, but <i>might</i> speed up size retrieval for large images in
	 * non-standard places.
	 * 
	 * @param imageId The texture id to register the size for
	 * @param size The size to be registered
	 */
	public static void addImageSize(@NotNull Identifier imageId, @NotNull Size size) {
		IMAGE_SIZES.put(requireNonNull(imageId), requireNonNull(size));
	}

	/**
	 * Retrieves a document specified by the document id, localized to the current in-game locale.
	 * It is equivalent to {@code getDocument(documentId, null)}.
	 * 
	 * @param documentId The id of the document to retrieve
	 * @return The document localized to the current locale, or {@code null} if not registered
	 */
	@Nullable
	public static DocNode getDocument(Identifier documentId) {
		return getDocument(documentId, null);
	}

	/**
	 * Retrieves a document specified by the document id, localized to a given locale.
	 * If {@code locale} is null, then the current in-game locale is used.
	 * 
	 * @param documentId The id of the document to retrieve
	 * @param locale The locale to retrieve
	 * @return The document localized to the current locale, or {@code null} if not registered
	 */
	@Nullable
	public static DocNode getDocument(Identifier documentId, @Nullable String locale) {
		if (locale == null) locale = getCurrentLocale();
		
		LocalizedMarkdownResource resource = SOURCES.get(documentId);
		if (resource == null) return null;
		
		return resource.getLocalizedDocument(locale);
	}

	/**
	 * Retrieves a document specified by the document id, localized to the current in-game locale.
	 * If no localization was registered for the current locale, the fallback locale {@code en_us} is used.
	 * 
	 * @param documentId The id of the document to retrieve
	 * @return The document localized to the current locale, or {@code null} if the document id is unknown
	 * @throws IllegalStateException If neither the current nor the fallback localization could be found
	 */
	@Nullable
	public static DocNode getDocumentOrFallback(Identifier documentId) {
		return getDocumentOrFallback(documentId, null);
	}

	/**
	 * Retrieves a document specified by the document id, localized to a given locale.
	 * If {@code locale} is null, then the current in-game locale is used.
	 * <p>
	 * If no localization was registered for the specified locale, the fallback locale {@code en_us} is used.
	 * 
	 * @param documentId The id of the document to retrieve
	 * @param locale The locale to retrieve
	 * @return The document localized to the specified locale, or {@code null} if the document id is unknown
	 * @throws IllegalStateException If neither the current nor the fallback localization could be found
	 */
	@Nullable
	public static DocNode getDocumentOrFallback(Identifier documentId, String locale) {
		if (locale == null) locale = getCurrentLocale();
		
		LocalizedMarkdownResource resource = SOURCES.get(documentId);
		if (resource == null) return null;
		
		return resource.getLocalizedOrFallbackDocument(locale, "en_us");
	}

	/**
	 * Registers a localization for a given document id.
	 * 
	 * @param documentId The document id to register the document for
	 * @param language The language to register
	 * @param source The document to register
	 */
	public static void addLocalizedDocument(Identifier documentId, String language, DocNode source) {
		SOURCES.putIfAbsent(documentId, new LocalizedMarkdownResource(documentId));

		LocalizedMarkdownResource resource = SOURCES.get(documentId);
		resource.addLocalizedDocument(language, source);
	}
	
	private static String getCurrentLocale() {
		return MinecraftClient.getInstance().options.language;
	}
}
