package wikisearch.mohi.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wikisearch.mohi.network.WikiFetcher;
import wikisearch.mohi.render.ImageLoader;

import java.util.List;

public class WikiBookScreen extends Screen {
    private static final Identifier BOOK_TEXTURE = Identifier.of("minecraft", "textures/gui/book.png");
    private TextFieldWidget searchField;
    private ButtonWidget searchButton;

    private String currentPageText = "Enter a search term above.";
    private Identifier currentImageId = null;
    private boolean isLoading = false;

    public WikiBookScreen() {
        super(Text.literal("Wiki Search"));
    }

    @Override
    protected void init() {
        super.init();
        int bookWidth = 192;
        int x = (this.width - bookWidth) / 2;
        int y = (this.height - 192) / 2;

        this.searchField = new TextFieldWidget(this.textRenderer, x + 20, y + 20, 100, 20, Text.literal("Search..."));
        this.addDrawableChild(this.searchField);

        this.searchButton = ButtonWidget.builder(Text.literal("Search"), button -> {
            performSearch(this.searchField.getText());
        }).dimensions(x + 125, y + 20, 50, 20).build();
        this.addDrawableChild(this.searchButton);
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        int bookWidth = 192;
        int bookHeight = 192;
        int x = (this.width - bookWidth) / 2;
        int y = (this.height - bookHeight) / 2;

        context.drawTexture(BOOK_TEXTURE, x, y, 0, 0, bookWidth, bookHeight);

        super.render(context, mouseX, mouseY, delta);

        if (!this.currentPageText.isEmpty()) {
            List<net.minecraft.text.OrderedText> lines = this.textRenderer.wrapLines(Text.literal(this.currentPageText), bookWidth - 40);
            int textY = y + 50; // below the search bar
            for (int i = 0; i < lines.size(); i++) {
                if (textY > y + bookHeight - 30 - (this.currentImageId != null ? 64 : 0)) break; // Don't overflow the page, leave space for image
                context.drawText(this.textRenderer, lines.get(i), x + 20, textY, 0x000000, false);
                textY += this.textRenderer.fontHeight;
            }
        }

        if (this.currentImageId != null) {
            context.drawTexture(this.currentImageId, x + (bookWidth - 64) / 2, y + bookHeight - 80, 0, 0, 64, 64, 64, 64);
        }
    }
}
