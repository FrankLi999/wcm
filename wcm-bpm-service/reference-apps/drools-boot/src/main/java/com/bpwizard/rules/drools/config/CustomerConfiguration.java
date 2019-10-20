package com.bpwizard.rules.drools.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

/**
 * Via kie-ci
 */
public class CustomerConfiguration {

	private static final String RULES_PATH = "customer_discount_rule.xls";
	
	private KieServices kieServices = KieServices.Factory.get();

	private KieFileSystem getKieFileSystem() throws IOException {
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		List<String> rules = Arrays.asList(RULES_PATH);
		for (String rule : rules) {
			kieFileSystem.write(ResourceFactory.newClassPathResource(rule));
		}
		return kieFileSystem;

	}

	public KieContainer getKieContainer() throws IOException {
		getKieRepository();

		KieBuilder kb = kieServices.newKieBuilder(getKieFileSystem());
		kb.buildAll();

		KieModule kieModule = kb.getKieModule();
		KieContainer kContainer = kieServices.newKieContainer(kieModule.getReleaseId());

		return kContainer;

	}

	private void getKieRepository() {
		final KieRepository kieRepository = kieServices.getRepository();
		kieRepository.addKieModule(new KieModule() {
			public ReleaseId getReleaseId() {
				return kieRepository.getDefaultReleaseId();
			}
		});
	}

	public KieSession getKieSession() {
		getKieRepository();
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_PATH));

		KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
		kb.buildAll();
		KieModule kieModule = kb.getKieModule();

		KieContainer kContainer = kieServices.newKieContainer(kieModule.getReleaseId());

		return kContainer.newKieSession();

	}

}