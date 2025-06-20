package blue.endless.enoki.resource.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import blue.endless.enoki.resource.LocalizedRegistry;
import blue.endless.enoki.resource.LocalizedResourceManager;
import blue.endless.enoki.resource.ResourceDecoder;
import blue.endless.enoki.util.NotNullByDefault;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
@NotNullByDefault
public record LocalizedResourceReloader<T>(
		Identifier id,
		String basePath,
		Predicate<Identifier> predicate,
		boolean stripExtension,
		boolean freeze,
		ResourceDecoder<T> decoder,
		Map<String, LocalizedRegistry<T>> registryMap,
		Logger logger
		) implements IdentifiableResourceReloadListener {
	
	@Override
	public Identifier getFabricId() {
		return id;
	}
	
	@SuppressWarnings("null") // Params from ResourceReloader aren't tagged NotNull
	@Override
	public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
		CompletableFuture<List<LocalizedResource<T>>> future = CompletableFuture.supplyAsync(() -> prepareResources(manager), prepareExecutor);
		var combinedFuture = future
				.thenCompose(synchronizer::whenPrepared)
				.thenAcceptAsync(this::applyResources, applyExecutor);
		
		return combinedFuture;
	}

	public List<LocalizedResource<T>> prepareResources(ResourceManager manager) {
		List<LocalizedResource<T>> result = new ArrayList<>();
		
		Map<Identifier, Resource> resources = manager.findResources(basePath, predicate);
		for(Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
			Identifier id = entry.getKey();
			String path = id.getPath();
			
			if (path.startsWith("/")) path = path.substring(1);
			if (path.startsWith(basePath)) {
				path = path.substring(basePath.length());
			}
			
			if (path.startsWith("/")) path = path.substring(1);
			
			// Grab locale
			int localeEnd = path.indexOf("/");
			if (localeEnd == -1) {
				// Skip resource that is not inside a locale folder
				continue;
			}
			String locale = path.substring(0, localeEnd);
			String remainingPath = path.substring(localeEnd+1);
			if (stripExtension) {
				int dot = remainingPath.lastIndexOf('.');
				if (dot != -1) remainingPath = remainingPath.substring(0, dot);
			}
			
			Identifier registrationId = Identifier.of(id.getNamespace(), remainingPath);
			
			try {
				// Load the resource
				Optional<T> t = decoder.decode(registrationId, entry.getValue());
				if (t.isPresent()) result.add(new LocalizedResource<T>(locale, registrationId, t.get()));
			} catch (Throwable t) {
				logger.warn("Malfunctioning ResourceDecoder.", t);
			}
		}
		
		return result;
	}
	
	public void applyResources(List<LocalizedResource<T>> resources) {
		registryMap.clear();
		registryMap.computeIfAbsent(LocalizedResourceManager.FALLBACK_LOCALE, LocalizedRegistry::new);
		
		for(LocalizedResource<T> resource : resources) {
			LocalizedRegistry<T> localeRegistry = registryMap.computeIfAbsent(resource.locale(), LocalizedRegistry::new);
			localeRegistry.register(resource.id(), resource.value());
		}
		
		if (freeze) for(LocalizedRegistry<T> registry : registryMap.values()) {
			registry.freeze();
		}
	}
	
	private static record LocalizedResource<T>(String locale, Identifier id, T value) {}
}