package xyz.cottageindustries.cottfur.client.debugShit

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.text.Text
import xyz.cottageindustries.cottfur.data.PlayerModelDataManager

class GetPlayerStatsClient {
    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("cottfur_stats_client")
                    .then(
                        ClientCommandManager.argument("playerName", StringArgumentType.word())
                            .suggests { context, builder ->
                                context.source.world.players.forEach { builder.suggest(it.name.string) }
                                builder.buildFuture()
                            }
                            .executes { context ->
                                val playerName = StringArgumentType.getString(context, "playerName")
                                val player = context.source.world.players.find { it.name.string.equals(playerName, ignoreCase = false) }

                                if (player == null) {
                                    context.source.sendError(Text.literal("Player '$playerName' not found."))
                                    return@executes 0
                                }

                                val config = PlayerModelDataManager.getConfig(player.uuid)

                                context.source.sendFeedback(Text.literal("§eCottFur Stats for ${player.name.string}:"))
                                context.source.sendFeedback(Text.literal("§7Model: §f${config.modelTypeId}"))
                                context.source.sendFeedback(Text.literal("§7Pattern: §f${config.patternId ?: "None"}"))
                                context.source.sendFeedback(Text.literal("§7Texture: §f${config.customTextureId ?: "Default"}"))
                                context.source.sendFeedback(Text.literal("§7Primary: §f#${Integer.toHexString(config.primaryColor).uppercase()}"))
                                context.source.sendFeedback(Text.literal("§7Secondary: §f#${Integer.toHexString(config.secondaryColor).uppercase()}"))
                                context.source.sendFeedback(Text.literal("§7Accent: §f#${Integer.toHexString(config.accentColor).uppercase()}"))

                                Command.SINGLE_SUCCESS
                            }
                    )
            )
        }
    }
}