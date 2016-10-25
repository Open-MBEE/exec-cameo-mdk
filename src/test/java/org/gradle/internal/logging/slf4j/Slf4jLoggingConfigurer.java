/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.logging.slf4j;

import org.gradle.api.logging.LogLevel;
import org.gradle.internal.logging.config.LoggingConfigurer;
import org.gradle.internal.logging.events.OutputEventListener;
//import org.slf4j.LoggerFactory;

/**
 * A {@link LoggingConfigurer} implementation which configures custom slf4j binding to route logging events to a provided {@link
 * OutputEventListener}.
 *
 * Override prevents a ClassCastException caused by the OSGi library forcefully loading its own libraries first, which breaks
 * Gradle's slf4j hack to redirect it by loading its own {@link org.slf4j.impl.StaticLoggerBinder}. The consequences of this patch
 * are likely benign and disables slf4j log redirection.
 *
 * @author igomes
 */
public class Slf4jLoggingConfigurer implements LoggingConfigurer {
    private final OutputEventListener outputEventListener;

    public Slf4jLoggingConfigurer(OutputEventListener outputListener) {
        outputEventListener = outputListener;
    }

    public void configure(LogLevel logLevel) {
        /*
        if (logLevel == currentLevel) {
            return;
        }

        OutputEventListenerBackedLoggerContext context = (OutputEventListenerBackedLoggerContext) LoggerFactory.getILoggerFactory();

        if (currentLevel == null) {
            context.setOutputEventListener(outputEventListener);
        }

        currentLevel = logLevel;
        context.setLevel(logLevel);
        */
    }
}
