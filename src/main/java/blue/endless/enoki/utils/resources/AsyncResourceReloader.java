package blue.endless.enoki.utils.resources;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class AsyncResourceReloader<R> implements SimpleResourceReloadListener<List<AsyncResource<R>>> {
	protected abstract String getResourcePath();
	protected abstract boolean isResourcePertinent(Identifier resourceId);
	
	protected Optional<Identifier> toInGameId(Identifier resourceId) {
		return Optional.of(resourceId);
	}
	
	protected abstract Optional<R> load(Identifier inGameId, Resource resource);
	
	protected void beforeApply() {}
	protected abstract void apply(Identifier inGameId, R data);
	protected void afterApply() {}
	
	@Override
	public CompletableFuture<List<AsyncResource<R>>> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> 
				manager.findResources(getResourcePath(), this::isResourcePertinent)
					.entrySet()
					.stream()
					
					// We want to work with AsyncResources, since its API is nicer than Map.Entry
					.map(AsyncResource::of)
					
					// Resolve the in game ids, and only continue if the optional is present
					.<AsyncResource<Resource>>mapMulti((resource, consumer) ->
						this.toInGameId(resource.id())
							.map(resource::withId)
							.ifPresent(consumer))
					
					// Let the child load the resource, and only continue if the optional is present
					.<AsyncResource<R>>mapMulti((resource, consumer) ->
						this.load(resource.id(), resource.resource())
							.map(resource::withResource)
							.ifPresent(consumer))
					.toList(),
			executor);
	}

	@Override
	public CompletableFuture<Void> apply(List<AsyncResource<R>> resources, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			this.beforeApply();
			
			resources.forEach(resource -> this.apply(resource.id(), resource.resource()));
			
			this.afterApply();
		}, executor);
	}
}
