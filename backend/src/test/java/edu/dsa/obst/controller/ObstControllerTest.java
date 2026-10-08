package edu.dsa.obst.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ObstControllerTest {

    @Autowired
    private MockMvc mvc;

    private ResultActions solve(String json) throws Exception {
        return mvc.perform(post("/api/obst/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    void textbookCaseReturnsFullResponse() throws Exception {
        solve("{\"keys\":[10,12,20],\"frequencies\":[34,8,50]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.n").value(3))
                .andExpect(jsonPath("$.minCost").value(142))
                .andExpect(jsonPath("$.totalFrequency").value(92))
                .andExpect(jsonPath("$.expectedCost").value(closeTo(142.0 / 92.0, 1e-9)))
                .andExpect(jsonPath("$.notices", hasSize(0)))
                // Tables: n x n, null below the diagonal.
                .andExpect(jsonPath("$.costTable[0][2]").value(142))
                .andExpect(jsonPath("$.costTable[1][0]").value(nullValue()))
                .andExpect(jsonPath("$.rootTable[0][2]").value(3))
                .andExpect(jsonPath("$.weightTable[0][2]").value(92))
                // Tree.
                .andExpect(jsonPath("$.tree.key").value(20))
                .andExpect(jsonPath("$.tree.depth").value(1))
                .andExpect(jsonPath("$.tree.interval", contains(1, 3)))
                .andExpect(jsonPath("$.tree.left.key").value(10))
                .andExpect(jsonPath("$.tree.left.right.key").value(12))
                .andExpect(jsonPath("$.tree.right").value(nullValue()))
                // Candidates and fill order.
                .andExpect(jsonPath("$.candidates['1-3']", hasSize(3)))
                .andExpect(jsonPath("$.candidates['1-3'][2].rootKey").value(20))
                .andExpect(jsonPath("$.candidates['1-3'][2].chosen").value(true))
                .andExpect(jsonPath("$.candidates['1-3'][0].totalCost").value(158))
                .andExpect(jsonPath("$.fillOrder", hasSize(6)))
                .andExpect(jsonPath("$.fillOrder[5].i").value(1))
                .andExpect(jsonPath("$.fillOrder[5].j").value(3))
                // Balanced comparison tree.
                .andExpect(jsonPath("$.balanced.tree.key").value(12))
                .andExpect(jsonPath("$.balanced.minCost").value(176))
                // Course-style table: diagonals 0..n, c(0,3) = 142, r(0,3) = 3.
                .andExpect(jsonPath("$.courseTable.diagonals", hasSize(4)))
                .andExpect(jsonPath("$.courseTable.diagonals[3][0].c").value(142))
                .andExpect(jsonPath("$.courseTable.diagonals[3][0].r").value(3))
                .andExpect(jsonPath("$.courseTable.diagonals[3][0].terms", hasSize(3)));
    }

    @Test
    void unsortedKeysAreSortedWithTheirFrequencies() throws Exception {
        solve("{\"keys\":[20,10,12],\"frequencies\":[50,34,8]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys", contains(10, 12, 20)))
                .andExpect(jsonPath("$.frequencies", contains(34, 8, 50)))
                .andExpect(jsonPath("$.notices", hasSize(1)))
                .andExpect(jsonPath("$.notices[0]", containsString("sorted")))
                .andExpect(jsonPath("$.minCost").value(142));
    }

    @Test
    void lengthMismatch() throws Exception {
        solve("{\"keys\":[1,2,3],\"frequencies\":[1,2]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0]", containsString("same length")));
    }

    @Test
    void duplicateKeys() throws Exception {
        solve("{\"keys\":[5,7,5],\"frequencies\":[1,2,3]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", containsString("5 appears more than once")));
    }

    @Test
    void nonPositiveAndNonIntegerFrequencies() throws Exception {
        solve("{\"keys\":[1,2,3,4],\"frequencies\":[0,-2,2.5,\"x\"]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(4)))
                .andExpect(jsonPath("$.errors[0]", containsString("row 1")))
                .andExpect(jsonPath("$.errors[2]", containsString("2.5")));
    }

    @Test
    void nonIntegerKey() throws Exception {
        solve("{\"keys\":[1,1.5],\"frequencies\":[1,1]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", containsString("Key in row 2")));
    }

    @Test
    void tooManyAndTooFewKeys() throws Exception {
        solve("{\"keys\":[],\"frequencies\":[]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", containsString("At least 1")));

        String sixteen = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]";
        solve("{\"keys\":" + sixteen + ",\"frequencies\":" + sixteen + "}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", containsString("At most 15")));
    }

    @Test
    void fifteenKeysIsAllowed() throws Exception {
        String fifteen = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15]";
        solve("{\"keys\":" + fifteen + ",\"frequencies\":" + fifteen + "}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fillOrder", hasSize(120)));
    }

    @Test
    void missingFieldsAndBadJson() throws Exception {
        solve("{\"keys\":[1,2]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", containsString("frequencies")));

        solve("{not json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", containsString("must be JSON")));

        mvc.perform(post("/api/obst/solve").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void corsAllowsViteDevServer() throws Exception {
        mvc.perform(options("/api/obst/solve")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
