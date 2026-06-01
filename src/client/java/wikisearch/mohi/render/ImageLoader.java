package wikisearch.mohi.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ImageLoader {
    private static final HttpClient client = HttpClient.newHttpClient();

    public static CompletableFuture<Identifier> loadImage(String urlStr) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder().uri(URI.create(urlStr)).build();
                HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

                return NativeImage.read(resp.body());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenApplyAsync(image -> {
            if (image == null) return null;
            MinecraftClient mc = MinecraftClient.getInstance();
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            Identifier id = Identifier.of("wikisearch", "dynamic_image_" + System.currentTimeMillis());
            mc.getTextureManager().registerTexture(id, texture);
            return id;
        }, MinecraftClient.getInstance()::executeTask);
    }
}
