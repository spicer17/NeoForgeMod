package com.spice.welcomegg;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(WelcomeGG.MODID)
public class WelcomeGG {
    public static final String MODID = "welcomegg";
    private static final Logger LOGGER = LogUtils.getLogger();

    public WelcomeGG() {
        LOGGER.info("WelcomeGG Mod initialized.");

        // Register event handlers
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new ChatEventHandler());
    }

    public static class ChatEventHandler {

        // Define your regex pattern
        private static final Pattern WELCOME_PATTERN = Pattern.compile("welcome (.+) to baconetworks!");
        private static final Pattern RANK_PATTERN = Pattern.compile("(.+) has reached the rank of (.+)!");

        @SubscribeEvent
        public void onChatMessage(ClientChatReceivedEvent event) {
            // Log the received message
            String message = event.getMessage().getString();
            LOGGER.info("Chat event received: {}", message);

            // Ensure it's a system/server message
            if (event.isSystem()) {
                // Check for a match with the welcome pattern
                Matcher welcomeMatcher = WELCOME_PATTERN.matcher(message.toLowerCase());
                if (welcomeMatcher.matches()) {
                    String playerName = welcomeMatcher.group(1);
                    scheduleResponse(() -> sendMessage("Welcome!"));
                    return;
                }

                // Check for a match with the rank pattern
                Matcher rankMatcher = RANK_PATTERN.matcher(message.toLowerCase());
                if (rankMatcher.matches()) {
                    String playerName = rankMatcher.group(1);
                    String rank = rankMatcher.group(2);
                    scheduleResponse(() -> sendMessage("GG!"));
                }
            }
        }

        private void scheduleResponse(Runnable response) {
            // Schedule the response with a delay between 1.5 and 3 seconds
            double delay = 1.5 + (Math.random() * 1.5); // Random delay between 1.5 and 3 seconds
            Executors.newSingleThreadScheduledExecutor()
                    .schedule(response, (long) (delay * 1000), TimeUnit.MILLISECONDS);
        }

        private static void sendMessage(String message) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.connection.sendChat(message); // Send the message to the server chat
                LOGGER.info("Sent chat message: {}", message); // Log the sent message
            } else {
                LOGGER.warn("Player instance is null. Unable to send message.");
            }
        }
    }
}
