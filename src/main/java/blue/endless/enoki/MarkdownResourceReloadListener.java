package blue.endless.enoki;

import blue.endless.enoki.gui.Size;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.TextureContents;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MarkdownResourceReloadListener implements SimpleSynchronousResourceReloadListener {
	private static final Logger LOGGER = LogManager.getLogger(MarkdownResourceReloadListener.class);
	private static final Map<Identifier, Size> IMAGE_SIZES = new HashMap<>();
	
	public static Size getImageSize(Identifier imageId) {
		return IMAGE_SIZES.get(imageId);
	}

	@Override
	public Identifier getFabricId() {
		return Identifier.of(Enoki.MOD_ID, "markdown_resources");
	}

	@Override
	public void reload(ResourceManager resourceManager) {
		TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
		Map<Identifier, Resource> resources = resourceManager.findResources(
			"textures/markdown_images",
			id -> id.getPath().endsWith(".png"));
		
		IMAGE_SIZES.clear();
		
		for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
			loadImage(resourceManager, textureManager, entry.getKey());
		}
	}
	
	private void loadImage(ResourceManager resourceManager, TextureManager textureManager, Identifier id) {
		LOGGER.info("Loading resource {}", id);

		ResourceTexture texture = new ResourceTexture(id);
		textureManager.registerTexture(id, texture);
		
		TextureContents textureContents;
		try {
			textureContents = texture.loadContents(resourceManager);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		
		NativeImage image = textureContents.image();
		IMAGE_SIZES.put(id, new Size(image.getWidth(), image.getHeight()));
	}
}
