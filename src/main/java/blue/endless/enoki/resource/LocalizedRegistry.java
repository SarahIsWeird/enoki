package blue.endless.enoki.resource;

public class LocalizedRegistry<T> extends MiniRegistry<T> {
	protected final String locale;
	
	public LocalizedRegistry(String locale) {
		this.locale = locale;
	}
	
	public String locale() {
		return locale;
	}
}
