/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bpwizard.wcm.repo.config;

import javax.transaction.TransactionManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modeshape.jcr.api.txn.TransactionManagerLookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;

import com.bpwizard.wcm.repo.jcr.ModeShapeRepositoryFactory;
import com.bpwizard.wcm.repo.jcr.ModeShapeSessionFactory;
import com.bpwizard.wcm.repo.jcr.ModeshapeTransactionManagerLookup;

@Configuration
public class ModeshapeConfig {
	private static final Logger log = LogManager.getLogger(ModeshapeConfig.class);
	@Autowired 
	private TransactionManager transactionManager;
	
	@Bean
	@DependsOn({"transactionManager", "transactionManagerLookup"})
    public ModeShapeRepositoryFactory repositoryFactory() {
		ModeShapeRepositoryFactory factory = new ModeShapeRepositoryFactory();
        factory.setConfiguration(new ClassPathResource("repository-config.json"));
        log.debug("Created repositoryFactory");
        return factory;
    }
	
	@Bean
	public ModeShapeSessionFactory sessionRepository() throws Exception {
		ModeShapeSessionFactory sessionFactory = new ModeShapeSessionFactory(repositoryFactory().getObject());
		return sessionFactory;
	}
	
    @Bean
    public TransactionManagerLookup transactionManagerLookup(TransactionManager transactionManager) {
    	ModeshapeTransactionManagerLookup transactionManagerLookup = new ModeshapeTransactionManagerLookup();
    	//transactionManagerLookup.setTransactionManager(this.atomikosTransactionManager());
    	transactionManagerLookup.setTransactionManager(this.transactionManager);
    	return transactionManagerLookup;
    }
}