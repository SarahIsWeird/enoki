package blue.endless.enoki.gui.widgets.link;

import blue.endless.enoki.markdown.NodeStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;

public class ExternalLinkClickEventHandler implements NodeStyle.ClickEventHandler {
	private final String destination;
	private final MinecraftClient client;
	
	public ExternalLinkClickEventHandler(String destination) {
		this.destination = destination;
		this.client = MinecraftClient.getInstance();
	}

	@Override
	public void handle(double mouseX, double mouseY) {
		if (client.options.getChatLinksPrompt().getValue()) {
			this.openConfirmationPrompt();
		} else {
			this.openLink();
		}
	}
	
	private void openConfirmationPrompt() {
		Screen previousScreen = client.currentScreen;
		
		client.setScreen(new ConfirmLinkScreen(confirmed -> {
			if (confirmed) this.openLink();
			client.setScreen(previousScreen);
		}, this.destination, false));
	}
	
	private void openLink() {
		Util.getOperatingSystem().open(this.destination);
	}
}
