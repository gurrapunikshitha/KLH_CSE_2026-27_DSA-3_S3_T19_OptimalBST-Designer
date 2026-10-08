package edu.dsa.obst.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.dsa.obst.model.SolveRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates the raw request and turns it into clean, sorted arrays for {@link ObstSolver}.
 *
 * Rules:
 * <ul>
 *   <li>keys and frequencies are both present and have the same length</li>
 *   <li>1 &lt;= n &lt;= 15 (the DP tables stay readable at that size)</li>
 *   <li>every key is a whole number, and keys are distinct</li>
 *   <li>every frequency is a positive whole number</li>
 *   <li>unsorted keys are sorted together with their frequencies, and a notice says so</li>
 * </ul>
 * All errors are collected and reported together. Rows are numbered from 1, as in the UI.
 */
@Component
public class InputNormalizer {

    public static final int MIN_KEYS = 1;
    public static final int MAX_KEYS = 15;

    /** Clean input: keys strictly increasing, frequencies aligned with them. */
    public record NormalizedInput(int[] keys, int[] frequencies, List<String> notices) {
    }

    public NormalizedInput normalize(SolveRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            throw new ValidationException(List.of("Request body is required."));
        }
        if (request.keys() == null) {
            errors.add("\"keys\" is required.");
        }
        if (request.frequencies() == null) {
            errors.add("\"frequencies\" is required.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        List<JsonNode> rawKeys = request.keys();
        List<JsonNode> rawFreqs = request.frequencies();

        // Length checks.
        if (rawKeys.size() != rawFreqs.size()) {
            errors.add("Keys and frequencies must have the same length (got "
                    + rawKeys.size() + " keys and " + rawFreqs.size() + " frequencies).");
        }
        if (rawKeys.size() < MIN_KEYS) {
            errors.add("At least " + MIN_KEYS + " key is required.");
        }
        if (rawKeys.size() > MAX_KEYS) {
            errors.add("At most " + MAX_KEYS + " keys are allowed (got " + rawKeys.size() + ").");
        }

        // Each key must be a whole number that fits in an int.
        int[] keys = new int[rawKeys.size()];
        for (int row = 0; row < rawKeys.size(); row++) {
            JsonNode node = rawKeys.get(row);
            if (!isInt(node)) {
                errors.add("Key in row " + (row + 1) + " must be a whole number (got " + describe(node) + ").");
            } else {
                keys[row] = node.intValue();
            }
        }

        // Each frequency must be a positive whole number.
        int[] freqs = new int[rawFreqs.size()];
        for (int row = 0; row < rawFreqs.size(); row++) {
            JsonNode node = rawFreqs.get(row);
            if (!isInt(node) || node.intValue() <= 0) {
                errors.add("Frequency in row " + (row + 1) + " must be a positive integer (got " + describe(node) + ").");
            } else {
                freqs[row] = node.intValue();
            }
        }

        // Keys must be distinct. Report each duplicated value once.
        Set<Integer> seen = new LinkedHashSet<>();
        Set<Integer> duplicates = new LinkedHashSet<>();
        for (int row = 0; row < rawKeys.size(); row++) {
            if (isInt(rawKeys.get(row)) && !seen.add(keys[row])) {
                duplicates.add(keys[row]);
            }
        }
        for (int dup : duplicates) {
            errors.add("Keys must be distinct: " + dup + " appears more than once.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        // Sort (key, frequency) pairs by key, if they are not already in order.
        List<String> notices = new ArrayList<>();
        Integer[] order = new Integer[keys.length];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingInt(i -> keys[i]));

        boolean alreadySorted = true;
        for (int i = 0; i < order.length; i++) {
            if (order[i] != i) {
                alreadySorted = false;
                break;
            }
        }
        if (alreadySorted) {
            return new NormalizedInput(keys, freqs, notices);
        }

        int[] sortedKeys = new int[keys.length];
        int[] sortedFreqs = new int[keys.length];
        for (int i = 0; i < order.length; i++) {
            sortedKeys[i] = keys[order[i]];
            sortedFreqs[i] = freqs[order[i]];
        }
        notices.add("Keys were not in increasing order, so they were sorted. Each frequency stays with its key.");
        return new NormalizedInput(sortedKeys, sortedFreqs, notices);
    }

    private static boolean isInt(JsonNode node) {
        return node != null && node.isIntegralNumber() && node.canConvertToInt();
    }

    private static String describe(JsonNode node) {
        return node == null || node.isNull() ? "nothing" : node.toString();
    }
}
