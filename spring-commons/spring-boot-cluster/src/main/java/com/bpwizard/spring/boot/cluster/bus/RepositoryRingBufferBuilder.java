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

import java.util.concurrent.Executor;

import com.bpwizard.spring.boot.cluster.common.collection.ring.Consumer;
import com.bpwizard.spring.boot.cluster.common.collection.ring.Cursor;
import com.bpwizard.spring.boot.cluster.common.collection.ring.RingBuffer;
import com.bpwizard.spring.boot.cluster.common.collection.ring.RingBufferBuilder;
import com.bpwizard.spring.boot.cluster.common.collection.ring.WaitStrategy;

/**
 * An extension to the {@link RingBufferBuilder} which adds functionality required by the repository.
 * @param <T> the type of entries stored in the buffer
 * @param <C> the type of consumer
 * @see RingBufferBuilder
 */
public final class RepositoryRingBufferBuilder<T,C> extends RingBufferBuilder<T,C> {

    /**
     * Create a builder for ring buffers that use the supplied {@link Executor} to create consumer threads and the supplied
     * {@link RingBuffer.ConsumerAdapter} to adapt to custom consumer implementations. The ring buffer will allow entries to be added from
     * multiple threads.
     *
     * @param executor the executor that should be used to create threads to run {@link Consumer consumers}; may not be null
     * @param adapter the adapter to the desired consumer interface; may not be null
     * @param statistics a {@link RepositoryStatistics} instance; may be null.
     * @return the builder for ring buffers; never null
     * 
     * @see RingBufferBuilder#withMultipleProducers(Executor, RingBuffer.ConsumerAdapter) 
     */
    public static <T, C> RingBufferBuilder<T, C> withMultipleProducers( Executor executor,
                                                                        RingBuffer.ConsumerAdapter<T, C> adapter) {
        return new RepositoryRingBufferBuilder<>(executor, adapter).multipleProducers();
    }

    
    protected RepositoryRingBufferBuilder( Executor executor,
                                           RingBuffer.ConsumerAdapter<T, C> adapter) {
        super(executor, adapter);
    }

    @Override
    protected Cursor defaultCursor( int bufferSize, WaitStrategy waitStrategy ) {
        return new RepositoryCursor(bufferSize, waitStrategy);
    }
}
