package win.somereason.utils.object_copier.entity;

/**
 * on 2017/4/21.
 */
public class MappingException extends RuntimeException{
    public MappingException(String message){
        super(message);
    }
    public MappingException(String message,Throwable cause){
        super(message,cause);
    }
}
