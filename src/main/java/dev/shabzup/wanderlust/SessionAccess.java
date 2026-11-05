package dev.shabzup.wanderlust;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.FriendsService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.User;

public interface SessionAccess {
    record Session(User user, UserApiService api, CompletableFuture<ProfileResult> profile,
                   CompletableFuture<UserApiService.UserProperties> properties, FriendsService friends) {}
    Session wanderlust$capture();
    void wanderlust$apply(Session session);
}
