package org.scale7.portability;

import org.slf4j.Logger;

public interface ILoggerFactory {
	@SuppressWarnings("unchecked")
	Logger getLogger(Class clazz);
}
