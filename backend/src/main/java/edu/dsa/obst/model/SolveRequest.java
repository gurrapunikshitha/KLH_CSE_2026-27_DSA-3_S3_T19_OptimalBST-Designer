package edu.dsa.obst.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Request body of POST /api/obst/solve, e.g. { "keys": [10,20,30], "frequencies": [3,5,2] }.
 *
 * The elements are kept as raw JSON values (JsonNode) instead of int. Otherwise Jackson
 * would silently turn 2.5 into 2 or "7" into 7, and we could not tell the user which row
 * was wrong. {@link edu.dsa.obst.service.InputNormalizer} checks each value itself.
 */
public record SolveRequest(List<JsonNode> keys, List<JsonNode> frequencies) {
}
