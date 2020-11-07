package com.tiffnix.miniblocks;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTListCompound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class HeadUtil {
    private static final ConcurrentHashMap<UUID, String> playerSkinCache = new ConcurrentHashMap<>();

    public static String getSkin(UUID uuid) {
        String cached = playerSkinCache.get(uuid);
        if (cached != null) {
            return cached;
        }
        Logger logger = MiniBlocks.INSTANCE.getLogger();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString()).openConnection();
            connection.connect();
            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                Gson gson = new Gson();
                JsonObject object = gson.fromJson(reader, JsonObject.class);
                JsonArray properties = object.get("properties").getAsJsonArray();
                for (JsonElement item : properties) {
                    JsonObject property = item.getAsJsonObject();
                    JsonElement name = property.get("name");
                    if (name != null && name.getAsString().equalsIgnoreCase("textures")) {
                        // The timestamp field needs to be removed so that heads will stack across server restarts.
                        String base64 = property.get("value").getAsString();
                        String json = new String(Base64.decode(base64), StandardCharsets.UTF_8);
                        JsonObject texture = gson.fromJson(json, JsonObject.class);
                        texture.remove("timestamp");
                        String newJson = gson.toJson(texture);
                        String result = Base64.encode(newJson.getBytes(StandardCharsets.UTF_8));
                        playerSkinCache.put(uuid, result);
                        return result;
                    }
                }
            } else {
                logger.warning("sessionserver.mojang.com returned " + connection.getResponseCode() + " " + connection.getResponseMessage() + ", failed to fetch player skin");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ItemStack createPlayerHead(String itemName, List<String> itemLore, String playerName, UUID playerUuid) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        if (itemName != null) {
            meta.setDisplayName(itemName);
        }
        if (itemLore != null) {
            meta.setLore(itemLore);
        }
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        NBTCompound owner = nbtItem.addCompound("SkullOwner");
        owner.setUUID("Id", playerUuid);
        owner.setString("Name", playerName);
        NBTListCompound texture = owner.addCompound("Properties").getCompoundList("textures").addCompound();
        String skinValue = getSkin(playerUuid);
        texture.setString("Value", skinValue);

        return nbtItem.getItem();
    }

    public static ItemStack createPlayerHead(String name, List<String> lore, Player player) {
        return createPlayerHead(name, lore, player.getName(), player.getUniqueId());
    }
}