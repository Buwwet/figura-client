package org.figuramc.figura_client.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura_client.FiguraClient;
import org.figuramc.figura_core.minecraft_interop.ClientTranslatables;
import org.figuramc.figura_core.minecraft_interop.ConsoleOutput;
import org.figuramc.figura_core.text.FormattedText;
import org.figuramc.figura_core.util.exception.FiguraException;
import org.figuramc.figura_translations.Language;
import org.figuramc.figura_translations.TranslatableItems;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ConsoleOutputImpl implements ConsoleOutput {
    private static final CommandArgumentParser<Tag> TAG_PARSER = SnbtGrammar.createParser(NbtOps.INSTANCE);
    private static final Component MISSING_ENTITY = Component.literal(
            // TODO: use game language
            ClientTranslatables.LOG_MISSING_ENTITY.translate(Language.EN_US, TranslatableItems.Items0.INSTANCE)
    ).withStyle(
            Style.EMPTY.withColor(ChatFormatting.GRAY)
    );

    private static Component getEntityNameComponent(@Nullable UUID source) {
        Entity entity = null;
        locate: {
            if (source == null) break locate;
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) break locate;
            Player maybePlayer = level.getPlayerByUUID(source);
            if (maybePlayer != null) {
                entity = maybePlayer;
                break locate;
            }
            entity = level.getEntity(source);
        }
        MutableComponent text;
        if (entity != null) text = entity.getName().copy().withStyle(Style.EMPTY.withHoverEvent(
                new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(entity.getType(), source, entity.getName()))
        ));
        else {
            if (source != null)
                text = MISSING_ENTITY.copy().withStyle(Style.EMPTY.withHoverEvent(
                        new HoverEvent.ShowText(Component.literal(source + " ?"))
                ));
            else
                text = MISSING_ENTITY.copy().withStyle(Style.EMPTY.withHoverEvent(
                        new HoverEvent.ShowText(Component.literal(
                                // TODO: use game language
                                ClientTranslatables.LOG_NO_SOURCE.translate(Language.EN_US, TranslatableItems.Items0.INSTANCE)
                        ))
                ));
        }
        return text;
    }

    @Override
    public void logSimple(@Nullable UUID source, String message) {
        Component entityRef = getEntityNameComponent(source);
        throw new AssertionError("Not implemented");
    }

    @Override
    public void logFormatted(FormattedText text) {
        // TODO
        throw new AssertionError("Not implemented");
    }

    /**
     * tellraw/sNBT
     * equivalent of printJson on 0.1.x
     */
    @Override
    public void logNativeFormatted(String formatted) {
        Component text;
        try {
            Tag t = TAG_PARSER.parseForCommands(new StringReader(formatted));
            DataResult<Pair<Component, Tag>> decode = ComponentSerialization.CODEC.decode(NbtOps.INSTANCE, t);
            text = decode.getOrThrow().getFirst();
        } catch (CommandSyntaxException | IllegalStateException ignored) {
            // fallback to just printing the raw text
            text = Component.literal(formatted);
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(text, false);
        }
    }

    @Override
    public void reportError(FiguraException e) {
        FiguraClient.LOGGER.error("Figura Exception occurred:", e);
    }

    @Override
    public void reportUnexpectedError(Throwable throwable) {
        FiguraClient.LOGGER.error("Unexpected internal Figura error! Please report to devs!", throwable);
    }
}
