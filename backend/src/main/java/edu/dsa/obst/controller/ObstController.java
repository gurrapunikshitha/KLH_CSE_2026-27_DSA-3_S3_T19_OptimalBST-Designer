package edu.dsa.obst.controller;

import edu.dsa.obst.model.SolveRequest;
import edu.dsa.obst.model.SolveResponse;
import edu.dsa.obst.service.ObstService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API of the OBST Designer.
 *
 * POST /api/obst/solve with { "keys": [...], "frequencies": [...] }
 *   200: {@link SolveResponse} with the DP tables, the tree, candidates and fill order
 *   400: {@link edu.dsa.obst.model.ErrorResponse} listing every validation problem
 */
@RestController
@RequestMapping("/api/obst")
public class ObstController {

    private final ObstService service;

    public ObstController(ObstService service) {
        this.service = service;
    }

    @PostMapping("/solve")
    public SolveResponse solve(@RequestBody(required = false) SolveRequest request) {
        return service.solve(request);
    }
}
