package com.mertout.benchmark.collectors;

import com.mertout.benchmark.api.MetricCollector;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class NetworkCollector implements MetricCollector, Listener {
    private final AtomicLong packetsSent = new AtomicLong(0);
    private final AtomicLong packetsReceived = new AtomicLong(0);
    private final String HANDLER_NAME = "spica_packet_handler";

    @Override
    public void collect() {
        // Netty verileri AtomicLong ile anlık toplandığı için
        // burada ekstra bir işlem yapmaya gerek yok, veriler zaten güncel.
    }

    @Override
    public double calculateScore() {
        // Basit bir eşik: Saniyede 10.000 paketten fazlası (oyuncu sayısına göre) risklidir.
        long total = packetsSent.get() + packetsReceived.get();
        if (total == 0) return 100;
        double score = 100 - (total / 5000.0);
        return Math.max(0, Math.min(100, score));
    }

    @Override
    public Map<String, Object> getRawMetrics() {
        Map<String, Object> m = new HashMap<>();
        m.put("Sent", packetsSent.get());
        m.put("Received", packetsReceived.get());
        return m;
    }

    @Override
    public String getName() { return "Network System"; }

    // --- Netty Injection Logic ---

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        injectPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    private void injectPlayer(Player player) {
        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                packetsReceived.incrementAndGet();
                super.channelRead(ctx, msg);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                packetsSent.incrementAndGet();
                super.write(ctx, msg, promise);
            }
        };

        Channel pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        pipeline.pipeline().addBefore("packet_handler", HANDLER_NAME, handler);
    }

    private void removePlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(HANDLER_NAME);
            return null;
        });
    }
}