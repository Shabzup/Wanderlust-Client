package dev.shabzup.wanderlust.mixin;

import dev.shabzup.wanderlust.SessionAccess;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.FriendsService;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.RemoteFriendListUpdateHandler;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.telemetry.ClientTelemetryManager;
import org.spongepowered.asm.mixin.*;

@Mixin(Minecraft.class)
abstract class SessionMixin implements SessionAccess {
    @Shadow @Final @Mutable private User user;
    @Shadow @Final @Mutable private UserApiService userApiService;
    @Shadow @Final @Mutable private CompletableFuture<ProfileResult> profileFuture;
    @Shadow @Final @Mutable private CompletableFuture<UserApiService.UserProperties> userPropertiesFuture;
    @Shadow @Final @Mutable private ProfileKeyPairManager profileKeyPairManager;
    @Shadow @Final @Mutable private PlayerSocialManager playerSocialManager;
    @Shadow @Final @Mutable private RemoteFriendListUpdateHandler remoteFriendListUpdateHandler;
    @Shadow @Final @Mutable private ClientTelemetryManager telemetryManager;
    @Shadow private ReportingContext reportingContext;

    @Override public Session wanderlust$capture() {
        return new Session(user, userApiService, profileFuture, userPropertiesFuture,
            new com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService(((Minecraft)(Object)this).getProxy())
                .createFriendsService(user.getAccessToken()));
    }

    @Override public void wanderlust$apply(Session next) {
        Minecraft mc = (Minecraft)(Object)this;
        if (mc.level != null || mc.getConnection() != null) throw new IllegalStateException("Disconnect before switching accounts.");
        remoteFriendListUpdateHandler.close();
        telemetryManager.close();
        user = next.user();
        userApiService = next.api();
        profileFuture = next.profile();
        userPropertiesFuture = next.properties();
        profileKeyPairManager = ProfileKeyPairManager.create(userApiService, user, mc.gameDirectory.toPath());
        reportingContext = ReportingContext.create(ReportEnvironment.local(), userApiService);
        remoteFriendListUpdateHandler = new RemoteFriendListUpdateHandler(next.friends(), mc);
        playerSocialManager = new PlayerSocialManager(mc, userApiService, next.friends(), remoteFriendListUpdateHandler);
        telemetryManager = new ClientTelemetryManager(mc, userApiService, user);
        if (playerSocialManager.isFriendListEnabled()) remoteFriendListUpdateHandler.start();
    }
}
