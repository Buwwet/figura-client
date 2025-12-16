package org.figuramc.figura_client;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import org.figuramc.figura_core.minecraft_interop.ConsoleOutput;
import org.figuramc.figura_core.text.FormattedText;
import org.figuramc.figura_core.util.exception.FiguraException;

public class ConsoleOutputImpl implements ConsoleOutput {
    private static final CommandArgumentParser<Tag> TAG_PARSER = SnbtGrammar.createParser(NbtOps.INSTANCE);

    @Override
    public void logSimple(String message) {
        // TODO
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
        Minecraft.getInstance().player.displayClientMessage(text, false);
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
