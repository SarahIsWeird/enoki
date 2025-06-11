package blue.endless.enoki.gui.widgets.link;

import org.jetbrains.annotations.Nullable;

import org.commonmark.node.Link;

public record LinkInfo(@Nullable String title, @Nullable String destination) {
	public static LinkInfo of(Link link) {
		return new LinkInfo(link.getTitle(), link.getDestination());
	}
}
