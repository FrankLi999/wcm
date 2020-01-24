package io.digitalstate.camunda.prometheus.collectors.custom;

import io.digitalstate.camunda.prometheus.config.yaml.CustomMetricsConfig;
import org.camunda.bpm.engine.ProcessEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.script.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CamundaCustomMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamundaCustomMetrics.class);
    private static final Logger ScriptLOGGER = LoggerFactory.getLogger("CamundaCustomMetrics-ScriptLOGGER");

    // @TODO Review usage of Script Usage for performance and caching
    private static ScriptEngineManager engineManager = new ScriptEngineManager();
    private static ScriptEngine engine = engineManager.getEngineByName("Groovy");


    /**
     * Constructor for starting the Prometheus Camunda Custom Metrics Collectors
     * @param processEngine
     */
    public CamundaCustomMetrics(List<CustomMetricsConfig> customMetricsConfigs, ProcessEngine processEngine){

        LOGGER.info("Starting instance of Prometheus Camunda Custom Metrics");

        customMetricsConfigs.forEach(config ->
                processCustomMetricConfig(config, processEngine ));
    }

    private void processCustomMetricConfig(CustomMetricsConfig config, ProcessEngine processEngine) {
        if (config.getEnable()) {
            Bindings bindings = engine.createBindings();
            bindings.put("config", config);
            bindings.put("processEngine", processEngine);
            bindings.put("LOGGER", ScriptLOGGER);

            Resource scriptFile = config.getCollector();

            try {
                LOGGER.debug("Attempting to Compile Prometheus Custom Metric groovy script: " + scriptFile.toString());
                CompiledScript compiledScript = ((Compilable) engine).compile(new InputStreamReader(scriptFile.getInputStream()));

                String timerName = "Custom Timer: " + config.getCollector().getDescription();

                new Timer(timerName, true).schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            LOGGER.debug("Executing Custom Metric: " + scriptFile.getDescription());

                            compiledScript.eval(bindings);

                        } catch (ScriptException e) {
                            LOGGER.error("Prometheus Custom Metric groovy script threw exception!" + scriptFile.toString(), e);
                        }
                    }
                }, config.getStartDelay(), config.getFrequency());

            } catch (FileNotFoundException e) {
                LOGGER.error("Cannot find Groovy Script File." + scriptFile.getDescription(), e);
            } catch (ScriptException e) {
                LOGGER.error("Prometheus Custom Metric groovy script cannot compile." + scriptFile.getDescription(), e);
            } catch (IOException e) {
                LOGGER.error("Could not compile script, unable to get InputStream: " + scriptFile.getDescription(), e);
            }
        }
    }
}
