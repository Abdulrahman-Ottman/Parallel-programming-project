package com.example.concurrencylab.controller;

import com.example.concurrencylab.service.LoadBalancerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/load-balancer")
public class LoadBalancerController {

    private final LoadBalancerService loadBalancerService;

    public LoadBalancerController(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }

    @GetMapping("/run")
    public List<String> runSimulation(
            @RequestParam(defaultValue = "30") int tasks,
            @RequestParam(defaultValue = "roundrobin") String algo) { // متغير جديد لتحديد الخطة

        return loadBalancerService.runRealSimulation(tasks, algo);
    }
}