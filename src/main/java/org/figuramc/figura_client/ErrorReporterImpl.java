package org.figuramc.figura_client;

import org.figuramc.figura_core.minecraft_interop.ErrorReporter;
import org.figuramc.figura_core.util.exception.FiguraException;

public class ErrorReporterImpl implements ErrorReporter {

    @Override
    public void report(FiguraException e) {
        FiguraClient.LOGGER.error("Figura Exception occurred:", e);
    }

    @Override
    public void reportUnexpected(Throwable throwable) {
        FiguraClient.LOGGER.error("Unexpected internal Figura error! Please report to devs!", throwable);
    }
}
