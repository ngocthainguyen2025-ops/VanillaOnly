package vn.vanillaonly;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientPluginMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;

public class VanillaOnly extends JavaPlugin {

    @Override
    public void onEnable() {
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract() {
            @Override
            public void onPacketReceive(PacketReceiveEvent e) {
                String channel;
                byte[] data;

                if (e.getPacketType() == PacketType.Configuration.Client.PLUGIN_MESSAGE) {
                    WrapperConfigClientPluginMessage w = new WrapperConfigClientPluginMessage(e);
                    channel = w.getChannelName();
                    data = w.getData();
                } else if (e.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
                    WrapperPlayClientPluginMessage w = new WrapperPlayClientPluginMessage(e);
                    channel = w.getChannelName();
                    data = w.getData();
                } else {
                    return;
                }

                if (!channel.equals("minecraft:brand") && !channel.equals("MC|Brand")) return;

                // Bỏ qua người chơi Bedrock (prefix Floodgate mặc định là ".")
                String name = e.getUser().getName();
                if (name != null && name.startsWith(".")) return;

                String brand = readBrand(data);
                if (!brand.equalsIgnoreCase("vanilla")) {
                    e.setCancelled(true);
                    e.getUser().closeConnection();
                    getLogger().info("Chan " + name + " (brand: " + brand + ")");
                }
            }
        });
    }

    private static String readBrand(byte[] data) {
        int i = 0;
        while (i < data.length && (data[i] & 0x80) != 0) i++;
        i++;
        if (i >= data.length) return "";
        return new String(data, i, data.length - i, StandardCharsets.UTF_8);
    }
}
