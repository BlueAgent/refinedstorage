package refinedstorage.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.input.Keyboard;
import refinedstorage.RefinedStorage;
import refinedstorage.apiimpl.autocrafting.preview.CraftingPreviewStack;
import refinedstorage.network.MessageGridCraftingStart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GuiCraftingPreview extends GuiBase {
    private static final int VISIBLE_ROWS = 8;

    private List<CraftingPreviewStack> stacks;
    private GuiScreen parent;

    private int hash;
    private int quantity;

    private GuiButton startButton;
    private GuiButton cancelButton;

    public GuiCraftingPreview(GuiScreen parent, Collection<CraftingPreviewStack> stacks, int hash, int quantity) {
        super(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer player) {
                return false;
            }
        }, 176, 181);

        this.stacks = new ArrayList<>(stacks);
        this.parent = parent;

        this.hash = hash;
        this.quantity = quantity;

        this.scrollbar = new Scrollbar(157, 20, 12, 59);
    }

    @Override
    public void init(int x, int y) {
        startButton = addButton(x + 100, y + 150, 50, 20, t("misc.refinedstorage:start"));
        cancelButton = addButton(x + 20, y + 150, 50, 20, t("gui.cancel"));

        startButton.enabled = stacks.stream().filter(CraftingPreviewStack::cantCraft).count() == 0;
    }

    @Override
    public void update(int x, int y) {
        scrollbar.setEnabled(getRows() > VISIBLE_ROWS);
        scrollbar.setMaxOffset(getRows() - VISIBLE_ROWS);
    }

    @Override
    public void drawBackground(int x, int y, int mouseX, int mouseY) {
        bindTexture("gui/crafting_preview.png");

        drawTexture(x, y, 0, 0, width, height);

        x += 15;
        y += 20;

        int slot = scrollbar.getOffset() * 2;
        for (int i = 0; i < 8; ++i) {
            if (slot < stacks.size()) {
                CraftingPreviewStack stack = stacks.get(slot);

                if (stack.cantCraft()) {
                    drawTexture(x, y, 0, 185, 67, 29);
                }
            }

            if (i % 2 == 1) {
                x -= 68;
                y += 30;
            } else {
                x += 68;
            }

            slot++;
        }
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        drawString(7, 7, t("gui.refinedstorage:crafting_preview"));

        int x = 22;
        int y = 22;

        int slot = scrollbar.getOffset() * 2;

        RenderHelper.enableGUIStandardItemLighting();

        ItemStack hoveringStack = null;

        for (int i = 0; i < 8; ++i) {
            if (slot < stacks.size()) {
                CraftingPreviewStack stack = stacks.get(slot);

                drawItem(x, y + 5, stack.getStack());

                float scale = 0.5f;

                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);

                if (stack.needsCrafting()) {
                    String format = stack.cantCraft() ? "gui.refinedstorage:crafting_preview.missing"  : "gui.refinedstorage:crafting_preview.to_craft";
                    drawString(calculateOffsetOnScale(x + 20, scale), calculateOffsetOnScale(y + 8, scale), t(format, stack.getToCraft()));
                }

                if (stack.getStock() > 0) {
                    drawString(calculateOffsetOnScale(x + 20, scale), calculateOffsetOnScale(y + 13, scale), t("gui.refinedstorage:crafting_preview.available", stack.getStock()));
                }

                GlStateManager.popMatrix();

                if (inBounds(x, y, 16, 16, mouseX, mouseY)) {
                    hoveringStack = stack.getStack();
                }
            }

            if (i%2 == 1) {
                x -= 68;
                y += 30;
            } else {
                x += 68;
            }

            slot++;
        }

        if (hoveringStack != null) {
            drawTooltip(mouseX, mouseY, hoveringStack.getTooltip(Minecraft.getMinecraft().thePlayer, false));
        }
    }

    @Override
    protected void keyTyped(char character, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_RETURN && startButton.enabled) {
            startRequest();
        } else if (keyCode == Keyboard.KEY_ESCAPE) {
            close();
        } else {
            super.keyTyped(character, keyCode);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == startButton.id) {
            startRequest();
        } else if (button.id == cancelButton.id) {
            close();
        }
    }

    private void startRequest() {
        RefinedStorage.INSTANCE.network.sendToServer(new MessageGridCraftingStart(hash, quantity));

        close();
    }

    private int getRows() {
        return Math.max(0, (int) Math.ceil((float) stacks.size() / 2F));
    }

    private void close() {
        FMLClientHandler.instance().showGuiScreen(parent);
    }
}
