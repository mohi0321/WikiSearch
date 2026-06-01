package wikisearch.mohi.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ImageLoader {
    private static final HttpClient client = HttpClient.newHttpClient();

    public static CompletableFuture<ResourceLocation> loadImage(String urlStr) {
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
            Minecraft mc = Minecraft.getInstance();
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation id = ResourceLocation.parse("wikisearch:dynamic_image_" + System.currentTimeMillis());
            mc.getTextureManager().register(id, texture);
            return id;
        }, Minecraft.getInstance()::execute);
    }
}
