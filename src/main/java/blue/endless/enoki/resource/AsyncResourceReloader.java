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

@SuppressWarnings("ClassEscapesDefinedScope")
public abstract class AsyncResourceReloader<R> implements IdentifiableResourceReloadListener {
	protected abstract String getResourcePath();
	protected abstract boolean shouldLoad(Identifier resourceId);
	
	protected Optional<Identifier> toRegistryId(Identifier resourceId) {
		return Optional.of(resourceId);
	}
	
	protected abstract Optional<R> load(Identifier registryId, Resource resource);
	
	protected void beforeApply() {}
	protected abstract void apply(Identifier registryId, R data);
	protected void afterApply() {}
	
	public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
		return CompletableFuture.supplyAsync(() -> prepareResources(manager), prepareExecutor)
				.thenCompose(synchronizer::whenPrepared)
				.thenAcceptAsync(this::applyResources, applyExecutor);
	}
	
	public List<IdentifiedResource<R>> prepareResources(ResourceManager manager) {
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
	
	public void applyResources(List<IdentifiedResource<R>> resources) {
		this.beforeApply();
		
		resources.forEach(resource -> this.apply(resource.id(), resource.resource()));
		
		this.afterApply();
	}
	
	private record IdentifiedResource<R>(Identifier id, R resource) {}
}
