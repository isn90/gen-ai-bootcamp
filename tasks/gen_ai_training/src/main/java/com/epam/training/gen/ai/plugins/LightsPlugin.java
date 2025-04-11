package com.epam.training.gen.ai.plugins;

import com.epam.training.gen.ai.model.LightModel;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class LightsPlugin {
    private final Map<Integer, LightModel> lights = new HashMap<>();

    public LightsPlugin() {
        lights.put(1, new LightModel(1, "Table Lamp", false, LightModel.Brightness.MEDIUM, "#FFFFFF"));
        lights.put(2, new LightModel(2, "Porch light", false, LightModel.Brightness.HIGH, "#FF0000"));
        lights.put(3, new LightModel(3, "Chandelier", true, LightModel.Brightness.LOW, "#FFFF00"));
    }

    @DefineKernelFunction(name = "get_lights", description = "Gets a list of lights and their current state")
    public List<LightModel> getLights() {
        log.info("Getting lights");
        return new ArrayList<>(lights.values());
    }

    @DefineKernelFunction(name = "change_state", description = "Changes the state of the light")
    public LightModel changeState(
            @KernelFunctionParameter(name = "id", description = "The ID of the light to change") int id,
            @KernelFunctionParameter(name = "isOn", description = "The new state of the light") boolean isOn) {
        log.info("Changing light {} {}", id, isOn);
        if (!lights.containsKey(id)) {
            throw new IllegalArgumentException("Light not found");
        }

        lights.get(id).setIsOn(isOn);
        return lights.get(id);
    }
}
