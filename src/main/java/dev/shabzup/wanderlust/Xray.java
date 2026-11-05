package dev.shabzup.wanderlust;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Live model filter for Xray. The whitelist is cached because chunk meshes compile off-thread. */
public final class Xray {
    private static volatile String source = "";
    private static volatile Set<Block> targets = Set.of();
    private static String renderedConfig="";

    private static Set<Block> targets() {
        String value = Settings.text("xray.blocks");
        if (!value.equals(source)) synchronized (Xray.class) {
            if (!value.equals(source)) {
                Set<Block> parsed = new HashSet<>();
                for (String entry : value.split(",")) {
                    Identifier id = Identifier.tryParse(entry.trim());
                    if (id != null) {
                        Block block = BuiltInRegistries.BLOCK.getValue(id);
                        if (block != null && BuiltInRegistries.BLOCK.getKey(block).equals(id)) parsed.add(block);
                    }
                }
                targets = Set.copyOf(parsed);
                source = value;
            }
        }
        return targets;
    }

    public static boolean matches(BlockState state) { return targets().contains(state.getBlock()); }

    static void tick(Minecraft mc){
        if(mc.level!=null&&mc.gui.screen()==null&&Settings.on("xray")&&!renderedConfig.equals(Settings.text("xray.blocks")))refreshGeometry();
    }

    static void refreshGeometry() {
        Minecraft mc = Minecraft.getInstance();
        Runnable refresh = () -> {
            renderedConfig=Settings.text("xray.blocks");
            if (mc.level != null) mc.levelExtractor.allChanged();
        };
        if (mc.isSameThread()) refresh.run(); else mc.execute(refresh);
    }
}
