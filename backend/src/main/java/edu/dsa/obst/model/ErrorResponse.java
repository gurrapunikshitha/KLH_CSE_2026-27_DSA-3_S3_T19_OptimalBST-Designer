package edu.dsa.obst.model;

import java.util.List;

/**
 * Body of every 400 response. All problems found are listed together, so the user can
 * fix them in one go.
 */
public record ErrorResponse(int status, List<String> errors) {
}
