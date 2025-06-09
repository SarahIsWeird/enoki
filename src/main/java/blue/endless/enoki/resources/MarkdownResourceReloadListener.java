package blue.endless.enoki.resources;

import blue.endless.enoki.Enoki;
import blue.endless.enoki.gui.Size;
import blue.endless.enoki.markdown.DocNode;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.TextureContents;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import org.commonmark.node.Node;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MarkdownResourceReloadListener implements SimpleSynchronousResourceReloadListener {
	private static final Logger IMAGE_LOGGER = LogManager.getLogger("Enoki/Image loader");
	private static final Logger SOURCE_LOGGER = LogManager.getLogger("Enoki/Markdown loader");

	@Override
	public Identifier getFabricId() {
		return Identifier.of(Enoki.MOD_ID, "markdown_resources");
	}

	@Override
	public void reload(ResourceManager resourceManager) {
		loadImages(resourceManager);
		loadMarkdownSources(resourceManager);
	}
	
	private void loadImages(ResourceManager resourceManager) {
		TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
		Map<Identifier, Resource> resources = resourceManager.findResources(
			"textures/markdown_images",
			id -> id.getPath().endsWith(".png"));

		MarkdownResources.clearImageSizes();

		for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
			loadImage(resourceManager, textureManager, entry.getKey());
		}
	}
	
	private void loadImage(ResourceManager resourceManager, TextureManager textureManager, Identifier id) {
		IMAGE_LOGGER.info("Loading resource {}", id);

		ResourceTexture texture = new ResourceTexture(id);
		textureManager.registerTexture(id, texture);
		
		TextureContents textureContents;
		try {
			textureContents = texture.loadContents(resourceManager);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		
		NativeImage image = textureContents.image();
		MarkdownResources.addImageSize(id, new Size(image.getWidth(), image.getHeight()));
	}
	
	private void loadMarkdownSources(ResourceManager resourceManager) {
		Map<Identifier, Resource> resources = resourceManager.findResources(
			"markdown", id -> id.getPath().endsWith(".md"));

		MarkdownResources.clearSources();
			
		for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
			loadMarkdownSource(entry.getKey(), entry.getValue());
		}
	}
	
	private void loadMarkdownSource(Identifier id, Resource resource) {
		String language = getDocumentLanguage(id);
		Identifier documentId = getDocumentId(id);
		if (language == null || documentId == null) {
			SOURCE_LOGGER.error("Invalid markdown resource path {}!", id);
			return;
		}
		
		String source;
		try (InputStream inputStream = resource.getInputStream()) {
			source = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			SOURCE_LOGGER.error("Failed to open markdown resource {}", id, e);
			return;
		}
		
		Node rawDocument = Enoki.DEFAULT_PARSER.parse(source);
		DocNode document = DocNode.of(rawDocument);

		MarkdownResources.addLocalizedDocument(documentId, language, document);
		
		SOURCE_LOGGER.info("Loaded markdown resource {} ({})", documentId, language);
	}
	
	@Nullable
	private String getDocumentLanguage(Identifier resourceId) {
		String[] parts = resourceId.getPath().split("/");
		if (parts.length < 2) return null;
		return parts[1];
	}

	/**
	 * Creates a document id from the resource id.
	 * <p>
	 * It strips the leading {@code markdown/} prefix, as well as the trailing {@code .md} suffix, i.e., it turns
	 * {@code enoki:markdown/en_us/foo/test.md} into {@code enoki:foo/test}.
	 *
	 * @param resourceId The resource id given by the {@link ResourceManager}
	 * @return The document id to be used from now on
	 */
	@Nullable
	private Identifier getDocumentId(Identifier resourceId) {
		String path = resourceId.getPath();
		path = path.substring(0, path.length() - ".md".length());
		
		int languageSeparatorIndex = path.indexOf('/', "markdown/".length());
		if (languageSeparatorIndex == -1 || (languageSeparatorIndex + 1 >= path.length())) return null;
		
		path = path.substring(languageSeparatorIndex + 1);
		return resourceId.withPath(path);
	}
}
