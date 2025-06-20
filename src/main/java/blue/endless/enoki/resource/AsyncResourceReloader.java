package blue.endless.enoki.resource;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import blue.endless.enoki.util.NotNullByDefault;

/**
 * A Fabric ResourceReloadListener which handles all the complicated parts for you.
 * 
 * To use this class, subclasses must implement {@link #getResourcePath() getResourcePath},
 * {@link #shouldLoad(Identifier) shouldLoad}, {@link #load(Identifier, Resource) load}, and
 * {@link #apply(Identifier, R) apply}. In getResourcePath, specify the path inside the resource root to search (e.g.
 * "blocks"). In shouldLoad, filter resources based on whether they look like they should load (e.g.
 * {@code resourceId.getPath().endsWith(".foo")} ).
 * 
 * @param <R>
 */
@SuppressWarnings("ClassEscapesDefinedScope")
@NotNullByDefault
public abstract class AsyncResourceReloader<R> implements IdentifiableResourceReloadListener {
	
	/**
	 * Gets the base path that this ReloadListener will search inside for resources. For example, "models/block", or
	 * "textures/map/decorations". Subfolders inside this path will also be searched.
	 * @return The path where the search for resources will begin.
	 */
	protected abstract String getResourcePath();
	protected abstract boolean shouldLoad(Identifier resourceId);
	
	protected Optional<Identifier> toRegistryId(Identifier resourceId) {
		return Optional.of(resourceId);
	}
	
	protected abstract Optional<R> load(Identifier registryId, Resource resource);
	
	protected void beforeApply() {}
	protected abstract void apply(Identifier registryId, R data);
	protected void afterApply() {}
	
	@SuppressWarnings("null") // Arguments can't infer to nonnull
	public final CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
		return CompletableFuture.supplyAsync(() -> prepareResources(manager), prepareExecutor)
				.thenCompose(synchronizer::whenPrepared)
				.thenAcceptAsync(this::applyResources, applyExecutor);
	}
	
	public final List<IdentifiedResource<R>> prepareResources(ResourceManager manager) {
		Map<Identifier, Resource> resources = manager.findResources(getResourcePath(), this::shouldLoad);
		List<IdentifiedResource<R>> result = new ArrayList<>();
		resources.forEach((Identifier id, Resource res) -> {
			Optional<Identifier> registryId = toRegistryId(id);
			if (registryId.isEmpty()) return;
			Optional<R> domainObject = load(registryId.get(), res);
			if (domainObject.isEmpty()) return;
			
			result.add(new IdentifiedResource<>(registryId.get(), domainObject.get()));
		});
		
		return result;
	}
	
	public final void applyResources(List<IdentifiedResource<R>> resources) {
		this.beforeApply();
		
		resources.forEach(resource -> this.apply(resource.id(), resource.resource()));
		
		this.afterApply();
	}
	
	private record IdentifiedResource<R>(Identifier id, R resource) {}
}
