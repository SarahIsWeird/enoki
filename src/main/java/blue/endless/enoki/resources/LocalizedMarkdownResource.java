package blue.endless.enoki.resources;

import blue.endless.enoki.markdown.DocNode;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

class LocalizedMarkdownResource {
	private final Identifier documentId;
	private final Map<String, DocNode> localizedDocuments = new HashMap<>();

	public LocalizedMarkdownResource(Identifier documentId) {
		this.documentId = documentId;
	}

	public void addLocalizedDocument(String language, DocNode document) {
		this.localizedDocuments.put(language, document);
	}

	@Nullable
	public DocNode getLocalizedDocument(String language) {
		return this.localizedDocuments.get(language);
	}

	@NotNull
	public DocNode getLocalizedOrFallbackDocument(String language, String fallbackLanguage) {
		DocNode localizedDoc = this.getLocalizedDocument(language);
		if (localizedDoc != null) return localizedDoc;

		DocNode fallbackDoc = this.getLocalizedDocument(fallbackLanguage);
		if (fallbackDoc != null) return fallbackDoc;
		
		String message = String.format("Could not find fallback locale %s for document %s!",
			fallbackLanguage, documentId);
		throw new IllegalStateException(message);
	}
}
