/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.elasticsearch.session;

import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.elasticsearch.index.ElasticSearchIndexStrategy;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.interceptor.Session;

/**
 * @author Daniel Meyer
 *
 */
public class ElasticSearchSession implements Session {

  protected ElasticSearchIndexStrategy indexingStrategy;

  protected List<HistoryEvent> historyEvents = new LinkedList<HistoryEvent>();

  public ElasticSearchSession(ElasticSearchIndexStrategy indexingStrategy) {
    this.indexingStrategy = indexingStrategy;
  }

  public void addHistoryEvent(HistoryEvent historyEvent) {
    historyEvents.add(historyEvent);
  }

  public void flush() {
    indexingStrategy.executeRequest(historyEvents);
  }

  public void close() {

  }

}
