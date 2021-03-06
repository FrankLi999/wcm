/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bpwizard.spring.boot.cluster.bus;

import com.bpwizard.spring.boot.cluster.change.ChangeSetListener;
import com.bpwizard.spring.boot.cluster.change.Observable;

/**
 * A generic interface for an event bus which handles changes.
 */
public interface ChangeBus extends ChangeSetListener, Observable {

    /**
     * Starts up the change bus.
     *
     * @throws Exception if anything unexpected fails during startup.
     */
    public void start() throws Exception;

    /**
     * Shuts down the change bus, closing and clearing resources created during {@link com.bpwizard.spring.boot.cluster.bus.ChangeBus#start()}
     */
    public void shutdown();

    /**
     * Checks if there are any observers registered with the bus.
     *
     * @return {@code true} if there are any registered observers, {@code false} otherwise
     */
    public boolean hasObservers();

    /**
     * Register the supplied observer which will be always notified in the same thread as the bus instance.
     * This method does nothing if the observer reference is null.
     *
     * @param observer the observer to be added; may be null
     * @return true if the observer was added, or false if the observer was null, if the observer was already registered, or if
     *         the observer could not be added
     */
    boolean registerInThread( ChangeSetListener observer);
}
