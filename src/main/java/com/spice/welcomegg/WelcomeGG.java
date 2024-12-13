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

        // Define your regex patterns
        private static final Pattern WELCOME_PATTERN = Pattern.compile("welcome ([a-zA-Z0-9_]+) to baconetworks", Pattern.CASE_INSENSITIVE);
        private static final Pattern RANK_PATTERN = Pattern.compile("([a-zA-Z0-9_]+) has reached the rank of ([a-zA-Z0-9_]+)", Pattern.CASE_INSENSITIVE);

        @SubscribeEvent
        public void onChatMessage(ClientChatReceivedEvent event) {
            // Get the raw message
            String rawMessage = event.getMessage().getString();

            // Clean the message
            String cleanedMessage = cleanMessage(rawMessage);
            LOGGER.info("Cleaned chat message: {}", cleanedMessage);

            // Ensure it's a system/server message
            if (event.isSystem()) {
                // Check for a match with the welcome pattern
                Matcher welcomeMatcher = WELCOME_PATTERN.matcher(cleanedMessage);
                if (welcomeMatcher.matches()) {
                    scheduleResponse(() -> sendMessage("Welcome!"));
                    return;
                }

                // Check for a match with the rank pattern
                Matcher rankMatcher = RANK_PATTERN.matcher(cleanedMessage);
                if (rankMatcher.matches()) {
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
                mc.player.sendSystemMessage(Component.literal(message));
                LOGGER.info("Sent message: {}", message); // Log the sent message
            } else {
                LOGGER.warn("Player instance is null. Unable to send message.");
            }
        }

        private static String cleanMessage(String message) {
            // Remove any Minecraft color codes or special characters
            return message.replaceAll("\\u00A7[0-9a-fk-or]", "").replaceAll("[^a-zA-Z0-9_ ]", "").toLowerCase();
        }
    }
}
