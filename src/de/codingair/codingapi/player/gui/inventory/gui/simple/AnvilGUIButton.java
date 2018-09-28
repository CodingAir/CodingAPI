package de.codingair.codingapi.player.gui.inventory.gui.simple;

import de.codingair.codingapi.player.gui.anvil.AnvilClickEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilCloseEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.anvil.AnvilListener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class AnvilGUIButton extends Button {
    private ItemStack anvilItem;

    public AnvilGUIButton(int slot, ItemStack item, ItemStack anvilItem) {
        super(slot, item);
        this.anvilItem = anvilItem;
    }

    public AnvilGUIButton(int x, int y, ItemStack item, ItemStack anvilItem) {
        super(x, y, item);
        this.anvilItem = anvilItem;
    }

    @Override
    public void onClick(InventoryClickEvent e, Player player) {
        if(interrupt()) return;

        getInterface().setClosingByButton(true);

        AnvilGUI.openAnvil(getInterface().getPlugin(), player, new AnvilListener() {
            @Override
            public void onClick(AnvilClickEvent e) {
                e.setCancelled(true);
                e.setClose(false);

                AnvilGUIButton.this.onClick(e);
            }

            @Override
            public void onClose(AnvilCloseEvent e) {
                AnvilGUIButton.this.onClose(e);

                if(e.getPost() == null) {
                    getInterface().reinitialize();
                    e.setPost(() -> getInterface().open());
                }
            }
        }, this.anvilItem);
    }

    public boolean interrupt() {
        return false;
    }

    public abstract void onClick(AnvilClickEvent e);

    public abstract void onClose(AnvilCloseEvent e);
}
