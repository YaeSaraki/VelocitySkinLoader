package com.molean.velocityskinloader.client;

import com.molean.velocityskinloader.model.mineskin.Delay;
import com.molean.velocityskinloader.model.mineskin.GenerateByUrl;
import com.molean.velocityskinloader.model.mineskin.GenerateByUser;
import com.molean.velocityskinloader.model.mineskin.SkinInfo;
import com.molean.velocityskinloader.model.mineskin.exception.MineSkinAPIException;
import com.molean.velocityskinloader.model.mineskin.exception.RequestTooSoonException;
import com.molean.velocityskinloader.model.mineskin.exception.SkinGenerateException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileOutputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class MineSkinClient extends ApiClient {
    private static MineSkinClient instance = new MineSkinClient();

    public static MineSkinClient instance() {
        return instance;
    }

    private MineSkinClient() {
        super("https://api.mineskin.org");
    }

    @Override
    protected void errorHandle(HttpResponse<?> response) throws MineSkinAPIException {
        switch (response.statusCode()) {
            case 400, 500 -> {
//                throw gson.fromJson(((HttpResponse<String>) (response)).body(), SkinGenerateException.class);
                throw new RuntimeException("MineSkin Error: " + response.body());
            }
            case 429 -> {
//                throw gson.fromJson(((HttpResponse<String>) (response)).body(), RequestTooSoonException.class);
                throw new RuntimeException("MineSkin Error: " + response.body());

            }
            case 200 -> {
            }
            default -> throw new MineSkinAPIException();
        }
    }

    public SkinInfo generateByUpload(@Nullable String variant, @Nullable String name, @Nullable Long visibility, byte @NotNull [] skinImg) throws MineSkinAPIException {
        try {
            Path path = Files.createTempFile(UUID.randomUUID().toString(), "png");
            try (FileOutputStream fileOutputStream = new FileOutputStream(path.toFile())) {
                fileOutputStream.write(skinImg);
            }
            HashMap<String, Object> map = new HashMap<>();
            if (variant != null) {
                map.put("variant", variant);
            }
            if (name != null) {
                map.put("name", name);
            }
            if (visibility != null) {
                map.put("visibility", visibility);
            }
            map.put("file", path);
            HttpResponse<String> post = postFormData("/generate/upload", map);
            errorHandle(post);
            return gson.fromJson(post.body(), SkinInfo.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SkinInfo generateByUrl(GenerateByUrl generateByUrl) throws MineSkinAPIException, InterruptedException {
        int retry = 3;
        while (retry-- > 0) {
            try {
                HttpResponse<String> post = postJson("/generate/url", gson.toJson(generateByUrl));
                errorHandle(post);
                return gson.fromJson(post.body(), SkinInfo.class);
            } catch (RequestTooSoonException e) {
//                System.out.println("请求过快，等待 5 秒再试");
            } catch (Exception e) {
//                throw new RuntimeException(e);
                  Thread.sleep(5000); // 可以从 e 中提取真正的 delay
            }
        }
        throw new RuntimeException("多次重试后依然失败");
    }

    public SkinInfo generateByUser(GenerateByUser generateByUser) throws MineSkinAPIException {
        HttpResponse<String> post;
        try {
            post = postJson("/generate/user", gson.toJson(generateByUser));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        errorHandle(post);
        return gson.fromJson(post.body(), SkinInfo.class);

    }

    public Delay getDelay() throws Exception {
        HttpResponse<String> response = get("/get/delay");
        return gson.fromJson(response.body(), Delay.class);
    }

    public SkinInfo getUUID(UUID uuid) throws Exception {
        HttpResponse<String> response = get("/get/uuid/" + uuid);
        return gson.fromJson(response.body(), SkinInfo.class);
    }
}
