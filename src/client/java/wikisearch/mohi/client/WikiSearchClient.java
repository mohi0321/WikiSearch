package wikisearch.mohi.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import wikisearch.mohi.screen.WikiBookScreen;

public class WikiSearchClient implements ClientModInitializer {
    private static KeyMapping wikiKeyBinding;

    @Override
    public void onInitializeClient() {
        wikiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.wikisearch.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.wikisearch.keys"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (wikiKeyBinding.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new WikiBookScreen());
                }
            }
        });
    }
}
