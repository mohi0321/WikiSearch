package wikisearch.mohi.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import wikisearch.mohi.screen.WikiBookScreen;

public class WikiSearchClient implements ClientModInitializer {
    private static KeyBinding wikiKeyBinding;

    @Override
    public void onInitializeClient() {
        wikiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.wikisearch.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.wikisearch.keys"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (wikiKeyBinding.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new WikiBookScreen());
                }
            }
        });
    }
}
