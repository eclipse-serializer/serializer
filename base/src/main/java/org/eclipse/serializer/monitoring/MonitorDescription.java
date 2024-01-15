package org.eclipse.serializer.monitoring;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.management.DescriptorKey;

@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface MonitorDescription {
	@DescriptorKey("description")
    String value();
}
