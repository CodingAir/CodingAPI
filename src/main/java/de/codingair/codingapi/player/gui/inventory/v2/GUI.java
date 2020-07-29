package de.codingair.codingapi.player.gui.inventory.v2;

import com.google.common.base.Preconditions;
import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.*;
import de.codingair.codingapi.tools.Callback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Objects;

/**
 * GUI only for static purpose. Buttons cannot be moved (except with different pages which is not recommended).
 */
public class GUI extends InventoryBuilder {
    protected GUIListener listener;
    protected GUI fallback;

    protected final HashMap<Class<? extends Page>, Page> pages = new HashMap<>();
    protected Page active;
    Callback<Player> closing;
    protected boolean waiting = false; //for AnvilGUI e.g. (when this GUI has to be closed for a certain amount of time)

    public GUI(Player player, JavaPlugin plugin) {
        super(player, plugin);
    }

    public GUI(Player player, JavaPlugin plugin, int size, String title) {
        super(player, plugin);
        buildInventory(size, title);
    }

    public void open() throws AlreadyOpenedException, NoPageException, IsWaitingException {
        if(waiting) throw new IsWaitingException();
        if(isOpen()) throw new AlreadyOpenedException();
        if(active == null) throw new NoPageException();
        System.out.println("open");

        this.listener = new GUIListener(this);
        Bukkit.getPluginManager().registerEvents(this.listener, plugin);

        Callback<Player> callback = new Callback<Player>() {
            @Override
            public void accept(Player player) {
                System.out.println("register");
                API.addRemovable(GUI.this);
                player.openInventory(inventory);
            }
        };

        GUI gui = API.getRemovable(player, GUI.class);
        if(gui != null) {
            try {
                gui.close(callback);
            } catch(AlreadyClosedException ignored) {
            }
        } else callback.accept(player);
    }

    void continueGUI() throws IsNotWaitingException {
        if(!waiting) throw new IsNotWaitingException();

        waiting = false;
        System.out.println("continueGUI");
        player.openInventory(inventory);
    }

    public void close() throws AlreadyClosedException {
        close(null);
    }

    public void close(Callback<Player> callback) throws AlreadyClosedException {
        if(!isOpen()) throw new AlreadyClosedException();
        GUIListener listener = this.listener;
        this.listener = null;
        System.out.println("close");

        closing = new Callback<Player>() {
            @Override
            public void accept(Player player) {
                forceClose(listener, callback);

                if(fallback != null) {
                    try {
                        fallback.continueGUI();
                    } catch(IsNotWaitingException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        player.closeInventory();
    }

    void forceClose(GUIListener listener, Callback<Player> callback) {
        HandlerList.unregisterAll(listener);
        this.listener = null;

        System.out.println("unregister");
        API.removeRemovable(GUI.this);
        if(callback != null) callback.accept(player);
        closing = null;
    }

    public void registerPage(Page page) {
        registerPage(page, false);
    }

    public void registerPage(Page page, boolean active) {
        pages.put(page.getClass(), page);
        if(active) this.active = page;
    }

    public void switchTo(Class<? extends Page> pageClass) throws PageAlreadyOpenedException {
        switchTo(pages.get(pageClass));
    }

    public void switchTo(Page page) throws PageAlreadyOpenedException {
        Preconditions.checkNotNull(page);

        if(active == null) {
            active = page;
            page.apply(true);
            return;
        }

        if(active.equals(page)) throw new PageAlreadyOpenedException();

        boolean basic = !Objects.equals(active.getBasic(), page.getBasic());
        this.active.clear(basic);
        page.apply(basic);
    }

    @Override
    public void destroy() {
        super.destroy();

        this.pages.forEach((clazz, p) -> p.destroy());
        this.pages.clear();
    }

    public GUI getFallback() {
        return fallback;
    }

    public void setFallback(GUI fallback) {
        this.fallback = fallback;
    }

    public boolean isOpen() {
        return this.listener != null;
    }

    public Page getActive() {
        return active;
    }
}
