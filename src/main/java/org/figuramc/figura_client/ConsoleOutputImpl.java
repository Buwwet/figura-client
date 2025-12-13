package org.figuramc.figura_client;

import org.figuramc.figura_core.minecraft_interop.ConsoleOutput;
import org.figuramc.figura_core.text.FormattedText;
import org.figuramc.figura_core.util.exception.FiguraException;

public class ConsoleOutputImpl implements ConsoleOutput {

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

    @Override
    public void reportError(FiguraException e) {
        FiguraClient.LOGGER.error("Figura Exception occurred:", e);
    }

    @Override
    public void reportUnexpectedError(Throwable throwable) {
        FiguraClient.LOGGER.error("Unexpected internal Figura error! Please report to devs!", throwable);
    }
}
