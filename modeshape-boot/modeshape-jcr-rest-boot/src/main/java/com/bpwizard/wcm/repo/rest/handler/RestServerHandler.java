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

package com.bpwizard.wcm.repo.rest.handler;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Repository;
import javax.jcr.Value;

import org.springframework.stereotype.Component;

import com.bpwizard.wcm.repo.rest.modeshape.model.RestRepositories;

/**
 * An class which returns POJO-based rest model instances.
 */
@Component
public class RestServerHandler extends AbstractHandler {

    public void addRepository( // HttpServletRequest request,
    		String workspacesUrl,
    		String backupUrl,
    		String restoreUrl,
                                RestRepositories repositories,
                                String repositoryName ) {
        RestRepositories.Repository repository = repositories.addRepository(repositoryName, workspacesUrl, backupUrl, restoreUrl);
        try {
            Repository jcrRepository = this.repositoryManager.getRepository(repositoryName);
            repository.setActiveSessionsCount(((org.modeshape.jcr.api.Repository)jcrRepository).getActiveSessionsCount());
            for (String metadataKey : jcrRepository.getDescriptorKeys()) {
                Value[] descriptorValues = jcrRepository.getDescriptorValues(metadataKey);
                if (descriptorValues != null) {
                    List<String> values = new ArrayList<String>(descriptorValues.length);
                    for (Value descriptorValue : descriptorValues) {
                        values.add(descriptorValue.getString());
                    }
                    repository.addMetadata(metadataKey, values);
                }
            }
        } catch (Exception e) {
            logger.error(e, e.getMessage());
        }
    }

}
