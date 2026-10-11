package com.electro.libraryofwonders.utils.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class CommandHelper {
    public static class Context {
        /**
         * Safely checks if the command was executed by a real player in-game.
         * Sends a formatted error if it was a command block or server console.
         */
        public static ServerPlayer getPlayerOrError(CommandContext<CommandSourceStack> context) {
            try {
                return context.getSource().getPlayerOrException();
            } catch (Exception e) {
                Feedback.error(context, "This command can only be run by a player!");
                return null;
            }
        }
    }

    public static class Feedback {
        /** Sends a basic green success message to the command sender. */
        public static int success(CommandContext<CommandSourceStack> context, String message) {
            context.getSource().sendSuccess(() -> Component.literal(message).withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        /** Sends a red error message to the command sender. */
        public static int error(CommandContext<CommandSourceStack> context, String message) {
            context.getSource().sendFailure(Component.literal(message).withStyle(ChatFormatting.RED));
            return 0;
        }

        /**
         * Sends a highly stylized message with a bracketed prefix.
         * Example: [Mythos] Skill unlocked!
         */
        public static int styledInfo(CommandContext<CommandSourceStack> context, String prefix, ChatFormatting prefixColor, String message, ChatFormatting messageColor) {
            MutableComponent comp = Component.literal("[" + prefix + "] ").withStyle(prefixColor)
                    .append(Component.literal(message).withStyle(messageColor));

            context.getSource().sendSuccess(() -> comp, false);
            return 1;
        }
    }
}
