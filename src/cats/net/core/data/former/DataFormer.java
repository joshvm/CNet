package cats.net.core.data.former;

import cats.net.core.data.Data;
import cats.net.core.utils.CoreUtils;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DataFormer {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Former{}

    protected Data data;

    public abstract short[] getOpcodes();

    public final Data form(final short opcode, final Object... args){
        final Object[] realArgs = realArgs(args);
        final Class[] argTypes = types(realArgs);
        try{
           final Method method = Arrays.stream(getClass().getDeclaredMethods()).filter(
                   m -> m.isAnnotationPresent(Former.class)
                           && matches(argTypes, m.getParameterTypes())
                           && m.getReturnType().equals(Data.class)
           ).findFirst().orElse(null);
            CoreUtils.print("data former method for opcode %d: %s", opcode, method);
           if(method == null)
               return null;
           if(!method.isAccessible())
               method.setAccessible(true);
            data = new Data(opcode);
            return (Data)method.invoke(this, realArgs);
        }catch(Exception ex){
            CoreUtils.print(ex);
            return null;
        }
    }

    private static Object[] realArgs(final Object[] args){
        final List<Object> list = new ArrayList<>();
        for(final Object arg : args){
            if(arg instanceof Object[])
                list.addAll(Arrays.asList(realArgs((Object[])arg)));
            else
                list.add(arg);
        }
        return list.toArray();
    }

    private static Class[] types(final Object[] args){
        return Arrays.stream(args).map(Object::getClass).collect(Collectors.toList()).toArray(new Class[args.length]);
    }

    private static boolean matches(final Class[] argTypes, final Class[] paramTypes){
        CoreUtils.print("Argtypes: %s | ParamTypes: %s", Arrays.toString(argTypes), Arrays.toString(paramTypes));
        if(Arrays.equals(argTypes, paramTypes))
            return true;
        for(int i = 0; i < argTypes.length; i++)
            if(!paramTypes[i].isAssignableFrom(argTypes[i]))
                return false;
        return true;
    }
}
