package cats.net.core.utils;

import cats.net.core.Core;

public final class CoreUtils {

    private CoreUtils(){}

    public static synchronized void print(final Exception ex){
        if(Core.verboseExceptions)
            ex.printStackTrace();
    }

    public static synchronized void print(final String format, final Object... args){
        if(!Core.verbose)
            return;
        final String name = Thread.currentThread().getStackTrace()[2].getClassName();
        System.out.printf("[%s] %s\n", name, String.format(format, args));
    }
}
