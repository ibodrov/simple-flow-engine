package com.github.ibodrov.simpleflowengine.elements;

/*-
 * *****
 * Simple Flow Engine
 * -----
 * Copyright (C) 2019 Ivan Bodrov
 * -----
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
 * =====
 */

import com.github.ibodrov.simpleflowengine.RuntimeContext;
import com.github.ibodrov.simpleflowengine.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sleep implements Element {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(Sleep.class);

    private final long ms;

    public Sleep(long ms) {
        this.ms = ms;
    }

    @Override
    public void eval(RuntimeContext ctx, State state) {
        log.info("Sleep -> {}ms...", ms);
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
