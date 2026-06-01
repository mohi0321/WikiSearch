package wikisearch.mohi.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import wikisearch.mohi.network.WikiFetcher;
import wikisearch.mohi.render.ImageLoader;

import java.util.List;

public class WikiBookScreen extends Screen {
    private static final ResourceLocation BOOK_TEXTURE = ResourceLocation.parse("minecraft:textures/gui/book.png");
    private EditBox searchField;
    private Button searchButton;

    private String currentPageText = "Enter a search term above.";
    private ResourceLocation currentImageId = null;
    private boolean isLoading = false;

    public WikiBookScreen() {
        super(Component.literal("Wiki Search"));
    }

    @Override
    protected void init() {
        super.init();
        int bookWidth = 192;
        int x = (this.width - bookWidth) / 2;
        int y = (this.height - 192) / 2;

        this.searchField = new EditBox(this.font, x + 20, y + 20, 100, 20, Component.literal("Search..."));
        this.addRenderableWidget(this.searchField);

        this.searchButton = Button.builder(Component.literal("Search"), button -> {
            performSearch(this.searchField.getValue());
        }).bounds(x + 125, y + 20, 50, 20).build();
        this.addRenderableWidget(this.searchButton);
    }

    private void performSearch(String query) {
        if (query.isEmpty()) return;
        this.isLoading = true;
        this.currentPageText = "Loading...";
        this.currentImageId = null;

        WikiFetcher.fetchPageInfo(query).thenAccept(info -> {
            this.isLoading = false;
            this.currentPageText = info.text;
            if (info.imageUrl != null) {
                ImageLoader.loadImage(info.imageUrl).thenAccept(id -> {
                    this.currentImageId = id;
                });
            }
        });
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        int bookWidth = 192;
        int bookHeight = 192;
        int x = (this.width - bookWidth) / 2;
        int y = (this.height - bookHeight) / 2;

        context.blit(BOOK_TEXTURE, x, y, 0, 0, bookWidth, bookHeight);

        super.render(context, mouseX, mouseY, delta);

        if (!this.currentPageText.isEmpty()) {
            List<FormattedCharSequence> lines = this.font.split(Component.literal(this.currentPageText), bookWidth - 40);
            int textY = y + 50; 
            for (int i = 0; i < lines.size(); i++) {
                if (textY > y + bookHeight - 30 - (this.currentImageId != null ? 64 : 0)) break; 
                context.drawString(this.font, lines.get(i), x + 20, textY, 0x000000, false);
                textY += this.font.lineHeight;
            }
        }

        if (this.currentImageId != null) {
            context.blit(this.currentImageId, x + (bookWidth - 64) / 2, y + bookHeight - 80, 0, 0, 64, 64, 64, 64);
        }
    }
}
