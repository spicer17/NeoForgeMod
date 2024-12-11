package com.spice.welcomegg;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Mod(WelcomeGG.MODID)
public class WelcomeGG {
    public static final String MODID = "welcomegg";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final File LOG_FILE = new File(Minecraft.getInstance().gameDirectory, "server_messages.log");

    public WelcomeGG() {
        LOGGER.info("WelcomeGG Mod initialized.");

        // Register event handlers
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new ChatEventHandler());
        setupLogFile();
    }

    private void setupLogFile() {
        try {
            if (LOG_FILE.exists() || LOG_FILE.createNewFile()) {
                LOGGER.info("Server message log file initialized at: {}", LOG_FILE.getAbsolutePath());
            } else {
                LOGGER.warn("Failed to initialize server message log file.");
            }
        } catch (IOException e) {
            LOGGER.error("Error creating server message log file: ", e);
        }
    }

    public static class ChatEventHandler {

        @SubscribeEvent
        public void onChatMessage(ClientChatReceivedEvent event) {
            // Log the received message
            String message = event.getMessage().getString();
            LOGGER.info("Chat event received: {}", message);

            // Save the message to the log file
            saveMessageToFile(message);

            // Ensure it's a system/server message
            if (event.isSystem()) {
                String lowerCaseMessage = message.toLowerCase();

                // Check for trigger phrases
                if (lowerCaseMessage.contains("welcome")) {
                    scheduleResponse(() -> sendMessage("Welcome!"));
                } else if (lowerCaseMessage.contains("reached")) {
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

        private static void saveMessageToFile(String message) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                writer.write(message);
                writer.newLine();
            } catch (IOException e) {
                LOGGER.error("Failed to write message to log file: ", e);
            }
        }
    }
}
