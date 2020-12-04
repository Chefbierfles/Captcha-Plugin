package nl.chefbierfles.captcha.module;

import nl.chefbierfles.captcha.Captcha;
import nl.chefbierfles.captcha.helpers.constants.Permissions;
import nl.chefbierfles.captcha.managers.CaptchaManager;
import nl.chefbierfles.captcha.models.menus.CaptchaMenu;
import nl.chefbierfles.captcha.module.base.BaseModule;
import org.apache.commons.lang3.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class CaptchaModule extends BaseModule {

    private CaptchaManager captchaManager;

    public CaptchaModule() {
        name = this.getClass().getName();
        isEnabled = true;
        captchaManager = new CaptchaManager();
    }

    /*
    Open inventory
     */
    public void openCaptchaMenu(Player player) {
        CaptchaMenu captchaMenu = captchaManager.getCaptchaMenu(player);
        player.openInventory(captchaMenu.getInventory());
    }

    /*
    When player joins and should do a captcha
     */
    public void onPlayerJoinHandler(Player player) {

        if (!isEnabled()) return;

        if (player.hasPermission(Permissions.PERMISSION_CAPTCHA_BYPASS)) return;

        CompletableFuture.supplyAsync(() -> getModuleManager().getDatabaseModule().getCaptchaData(player))
                .thenAccept(lastDoneDate -> {
                    if (lastDoneDate == null || new Date().after(DateUtils.addMonths(lastDoneDate, 1))) {
                        openCaptchaMenu(player);
                    }
                });
    }

    /*
    When player quits
     */
    public void onPlayerQuitHandler(Player player) {
        if (!isEnabled()) return;

        captchaManager.removeCaptcha(player.getUniqueId());
    }

    /*
    When a player tries to click a inventory slot
     */
    public boolean onInventoryClickHandler(InventoryClickEvent event) {

        if (!isEnabled()) return false;

        if (!(event.getWhoClicked() instanceof Player)) return false;

        if (event.getClickedInventory() == null) return false;

        if (!captchaManager.hasCaptcha(event.getWhoClicked().getUniqueId())) return false;

        CaptchaMenu captchaMenu = captchaManager.getCaptchaMenu((Player) event.getWhoClicked());

        //Check inventory is a captcha Inventory
        if (event.getClickedInventory().getTitle() != captchaMenu.getInventory().getTitle()) return false;

        ItemStack clickedItem = event.getCurrentItem();

        //Check if capatchaItem is clicked
        if (clickedItem.getItemMeta().getDisplayName() == captchaMenu.getInvalidItem().getItemMeta().getDisplayName()) {

            //Check of maximaal bereikt is
            if (captchaMenu.getMistakesMade() >= captchaMenu.getMaxMistakes()) {
                ((Player) event.getWhoClicked()).kickPlayer(ChatColor.RED + "Te veel ongeldige pogingen!");
                captchaManager.removeCaptcha(event.getWhoClicked().getUniqueId());
                return true;
            }

            captchaMenu.addMistake();
            captchaManager.updateCaptchaMenu((Player) event.getWhoClicked(), captchaMenu);
        }

        if (clickedItem.getItemMeta().getDisplayName() == captchaMenu.getCorrectItem().getItemMeta().getDisplayName()) {

            if (captchaMenu.replaceCorrectItem(event.getSlot(), (Player) event.getWhoClicked()) == 0) {
                //Captcha is done
                captchaManager.finishCaptcha(event.getWhoClicked().getUniqueId());

                event.getWhoClicked().closeInventory();
                event.getWhoClicked().sendMessage(ChatColor.GREEN + "Je hebt de captcha succesvol afgerond.");
            }
        }

        return true;
    }

    /*
    When a player tries to speak
     */
    public boolean onAsyncPlayerChatHandler(Player player) {

        if (!isEnabled()) return false;

        //Check if player still needs to do captcha
        if (!captchaManager.hasCaptcha(player.getUniqueId())) return false;

        CaptchaMenu captchaMenu = captchaManager.getCaptchaMenu(player);

        //Check if menu is open
        if (player.getOpenInventory().getTitle() != captchaMenu.getInventory().getTitle()) {
            //Re-open menu
            if (captchaMenu.getInventoryClosed() >= captchaMenu.getMaxInventoryClosed()) {
                Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(Captcha.class), () -> {
                    player.kickPlayer(ChatColor.RED + "Te veel ongeldige pogingen!");
                });
                return true;
            }
            captchaMenu.addInventoryClosed();
            openCaptchaMenu(player);
        }

        return true;
    }

    /*
    When a player tries to interact
     */
    public boolean onPlayerInteractHandler(Player player) {

        if (!isEnabled()) return false;

        //Check if player still needs to do captcha
        if (!captchaManager.hasCaptcha(player.getUniqueId())) return false;

        CaptchaMenu captchaMenu = captchaManager.getCaptchaMenu(player);

        //Check if menu is open
        if (player.getOpenInventory().getTitle() != captchaMenu.getInventory().getTitle()) {
            //Re-open menu
            if (captchaMenu.getInventoryClosed() >= captchaMenu.getMaxInventoryClosed()) {
                player.kickPlayer(ChatColor.RED + "Te veel ongeldige pogingen!");
                return true;
            }
            captchaMenu.addInventoryClosed();
            openCaptchaMenu(player);
        }

        return true;
    }

    /*
    When a player tries to move
     */
    public boolean onPlayerMoveHandler(Player player) {

        if (!isEnabled()) return false;

        //Check if player still needs to do captcha
        if (!captchaManager.hasCaptcha(player.getUniqueId())) return false;

        CaptchaMenu captchaMenu = captchaManager.getCaptchaMenu(player);

        //Check if menu is open
        if (player.getOpenInventory().getTitle() != captchaMenu.getInventory().getTitle()) {
            //Re-open menu
            if (captchaMenu.getInventoryClosed() >= captchaMenu.getMaxInventoryClosed()) {
                player.kickPlayer(ChatColor.RED + "Te veel ongeldige pogingen!");
                return true;
            }
            captchaMenu.addInventoryClosed();
            openCaptchaMenu(player);
        }

        return true;
    }

}
