package ascob.server.util;

public interface UnsafeConsumer <T,X extends Throwable>{

    void accept(T t) throws X;
}
