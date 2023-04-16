package ascob.impl;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Import(AscobImplConfigurationClass.class)
public @interface EnableAscobImpl {

}
