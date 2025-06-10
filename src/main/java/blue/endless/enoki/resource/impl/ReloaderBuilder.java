package blue.endless.enoki.resource.impl;

import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;

import com.google.common.base.Predicates;

import blue.endless.enoki.resource.LocalizedRegistry;
import blue.endless.enoki.resource.ResourceDecoder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class ReloaderBuilder<T> {
	private Identifier id;
	private String basePath;
	private Predicate<Identifier> predicate = Predicates.alwaysTrue();
	private boolean stripExtension = true;
	private boolean freeze = true;
	private ResourceDecoder<T> decoder;
	private final Map<String, LocalizedRegistry<T>> registryMap;
	private final Logger logger;
	private final ResourceType resourceType;
	
	public ReloaderBuilder(ResourceType type, Logger logger, Map<String, LocalizedRegistry<T>> registryMap) {
		this.registryMap = registryMap;
		this.resourceType = type;
		this.logger = logger;
	}
	
	public ReloaderBuilder<T> id(Identifier id) {
		this.id = id;
		return this;
	}
	
	public ReloaderBuilder<T> basePath(String basePath) {
		this.basePath = basePath;
		return this;
	}
	
	public ReloaderBuilder<T> resourcePredicate(Predicate<Identifier> resourcePredicate) {
		this.predicate = resourcePredicate;
		return this;
	}
	
	public ReloaderBuilder<T> stripExtension(boolean stripExtension) {
		this.stripExtension = stripExtension;
		return this;
	}
	
	public ReloaderBuilder<T> freeze(boolean freeze) {
		this.freeze = freeze;
		return this;
	}
	
	public ReloaderBuilder<T> decoder(ResourceDecoder<T> decoder) {
		this.decoder = decoder;
		return this;
	}
	
	public void register() {
		LocalizedResourceReloader<T> reloader = new LocalizedResourceReloader<>(
				id, basePath, predicate, stripExtension, freeze, decoder, registryMap, logger
				);
		ResourceManagerHelper.get(resourceType).registerReloadListener(reloader);
	}
}