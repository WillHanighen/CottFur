package xyz.cottageindustries.cottfur.mixin.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.cottageindustries.cottfur.CottfurConstants;
import xyz.cottageindustries.cottfur.client.model.AnthroModelType;
import xyz.cottageindustries.cottfur.data.PlayerModelDataManager;

/**
 * Mixin to intercept player rendering and replace with anthro models when applicable.
 * 
 * In Minecraft 1.21.11, the rendering uses a two-phase approach:
 * 1. updateRenderState - prepares the render state from the entity
 * 2. render - actually renders using the prepared state (inherited from LivingEntityRenderer)
 * 
 * For now, we only hook into updateRenderState to track which players have anthro models.
 * The actual rendering replacement will require a different approach (possibly using
 * GeckoLib's built-in entity replacement or a render layer).
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    /**
     * Capture the player entity during updateRenderState
     * This allows us to track which player is being rendered and their anthro model settings
     */
    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void cottfur$onUpdateRenderState(
        PlayerLikeEntity player,
        PlayerEntityRenderState state,
        float tickDelta,
        CallbackInfo ci
    ) {
        if (player instanceof AbstractClientPlayerEntity clientPlayer) {
            // Check if this player has an anthro model
            var config = PlayerModelDataManager.INSTANCE.getConfig(clientPlayer.getUuid());
            var modelType = AnthroModelType.Companion.fromId(config.getModelTypeId());
            
            if (modelType.isAnthroModel()) {
                state.stuckArrowCount = 3; // test
            }
        }
    }
}
