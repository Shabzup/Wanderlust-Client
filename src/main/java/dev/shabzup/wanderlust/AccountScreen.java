package dev.shabzup.wanderlust;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

final class AccountScreen extends Screen {
    private final Screen parent;
    private int page;
    private boolean wasBusy;
    private int count;
    AccountScreen(Screen parent) { super(Component.literal("Accounts")); this.parent = parent; }

    private Button button(int x, int y, int width, String text, Runnable action) {
        return addRenderableWidget(Button.builder(Component.literal(text), b -> action.run()).bounds(x,y,width,20).build());
    }

    @Override protected void init() {
        wasBusy = Accounts.busy;
        count = Accounts.entries.size();
        int x = width / 2 - 150, y = Math.max(32, height / 2 - 104);
        var name = addRenderableWidget(new EditBox(font, x, y, 198, 20, Component.literal("Offline username")));
        name.setMaxLength(16);
        name.setHint(Component.literal("Offline username"));
        button(x + 202, y, 98, "Add offline", () -> { Accounts.addOffline(name.getValue().trim()); rebuildWidgets(); });
        button(x, y + 24, 148, "Microsoft sign-in", Accounts::login).active = Accounts.canSwitch() && !Accounts.busy;
        button(x + 152, y + 24, 148, "Restore launcher", Accounts::restore).active = Accounts.canSwitch() && !Accounts.busy;
        int rows = Math.max(1, Math.min(4, (height - 190) / 24));
        int pages = Math.max(1, (count + rows - 1) / rows);
        page = Math.clamp(page, 0, pages - 1);
        for (int i = page * rows; i < Math.min(count, (page + 1) * rows); i++) {
            var entry = Accounts.entries.get(i);
            int row = y + 52 + (i % rows) * 24;
            button(x, row, 268, entry.name() + (entry.auth() == null ? " (offline)" : " (Microsoft)"), () -> Accounts.use(entry))
                .active = Accounts.canSwitch() && !Accounts.busy;
            button(x + 272, row, 28, "X", () -> { Accounts.entries.remove(entry); Accounts.saveNames(); rebuildWidgets(); });
        }
        int bottom = y + 56 + rows * 24;
        button(x, bottom, 35, "<", () -> { page--; rebuildWidgets(); }).active = page > 0;
        button(x + 39, bottom, 35, ">", () -> { page++; rebuildWidgets(); }).active = page + 1 < pages;
        button(x + 78, bottom, 108, "Open sign-in page", () -> {
            if (Accounts.verificationUrl.startsWith("https://")) Util.getPlatform().openUri(Accounts.verificationUrl);
        });
        button(x + 190, bottom, 110, Accounts.busy ? "Cancel sign-in" : "Back", () -> {
            if (Accounts.busy) { Accounts.cancel(); rebuildWidgets(); } else onClose();
        });
    }

    @Override public void tick() {
        if (wasBusy != Accounts.busy || count != Accounts.entries.size()) rebuildWidgets();
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int x, int y, float delta) {
        g.fill(0, 0, width, height, 0xF5192422);
        g.centeredText(font, "Accounts / " + minecraft.getUser().getName(), width / 2, 7, 0xFF85E7B6);
        g.centeredText(font, Accounts.canSwitch() ? "Offline names work in offline-mode servers and single-player." : "Disconnect to switch accounts.", width / 2, 20, 0xFFFFFFFF);
        g.centeredText(font, Accounts.status, width / 2, height - 36, 0xFFFFFFFF);
        g.centeredText(font, Accounts.busy ? "Microsoft code: " + Accounts.verificationCode : "Saved accounts are encrypted for your Windows login.", width / 2, height - 21, 0xFFB0C8BD);
        super.extractRenderState(g, x, y, delta);
    }
    @Override public void onClose() { Accounts.cancel(); minecraft.gui.setScreen(parent); }
}
