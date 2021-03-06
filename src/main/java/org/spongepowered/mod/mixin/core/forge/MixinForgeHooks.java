/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.mixin.core.forge;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.util.StaticMixinHelper;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooks {

    /**
     * @author bloodmc
     *
     * @reason Since we capture blocks as they are set in the world, getting
     * the current blockstate will not work during Forge's BlockBreak event
     * as the state will be post. In order to workaround this issue, we set
     * the captured extended state before creating the event.
     */
    @Overwrite
    public static boolean canHarvestBlock(Block block, EntityPlayer player, IBlockAccess world, BlockPos pos) {
        if (block.getMaterial().isToolNotRequired()) {
            return true;
        }

        ItemStack stack = player.inventory.getCurrentItem();
        // Sponge start - If we are currently sending a break event for Forge, use the event's extended state.
        IBlockState state = null;
        if (StaticMixinHelper.breakEventExtendedState != null) {
            state = StaticMixinHelper.breakEventExtendedState;
        } else {
            state = world.getBlockState(pos);
            state = state.getBlock().getActualState(state, world, pos);
        }

        String tool = block.getHarvestTool(state);
        // Sponge end

        if (stack == null || tool == null) {
            return player.canHarvestBlock(block);
        }

        int toolLevel = stack.getItem().getHarvestLevel(stack, tool);
        if (toolLevel < 0) {
            return player.canHarvestBlock(block);
        }

        return toolLevel >= block.getHarvestLevel(state);
    }
}
