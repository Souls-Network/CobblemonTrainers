package com.selfdot.cobblemontrainers.forge

import dev.architectury.platform.forge.EventBuses
import com.selfdot.cobblemontrainers.CobblemonTrainers
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import java.util.*
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent

@Mod(CobblemonTrainers.MODID)
class CobblemonTrainersForge {

    init {
        with(thedarkcolour.kotlinforforge.forge.MOD_BUS) {
            EventBuses.registerModEventBus(CobblemonTrainers.MODID, this)
            addListener(this@CobblemonTrainersForge::initialize)
            addListener(this@CobblemonTrainersForge::serverInit)
        }
    }

    private fun serverInit(event: FMLDedicatedServerSetupEvent) { }

    private fun initialize(event: FMLCommonSetupEvent) {
        CobblemonTrainers.initialize()
        CobblemonTrainers.disableTrainerPokemonSendOutAnimation = true
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER) { SetPermissionValidatorRunnable() }
        println("CobblemonTrainers Forge initialized")
    }

}
