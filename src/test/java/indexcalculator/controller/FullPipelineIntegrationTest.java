package indexcalculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import indexcalculator.Application;
import indexcalculator.dto.output.IndexDetailsDto;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class FullPipelineIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void createIndexMakeAdjustmentsCheckFinalResult() throws Exception {
        File creationFile = new File("src/test/resources/requests/indexFullPipeline/createIndex.json");
        String createIndexJson = Files.contentOf(creationFile, StandardCharsets.UTF_8);

        File additionAdjustmentFile = new File("src/test/resources/requests/indexFullPipeline/additionAdjustment.json");
        String additionAdjustmentJson = Files.contentOf(additionAdjustmentFile, StandardCharsets.UTF_8);

        File deletionAdjustmentFile = new File("src/test/resources/requests/indexFullPipeline/deletionAdjustment.json");
        String deletionAdjustmentJson = Files.contentOf(deletionAdjustmentFile, StandardCharsets.UTF_8);

        File dividendAdjustmentFile = new File("src/test/resources/requests/indexFullPipeline/dividendAdjustment.json");
        String dividendAdjustmentJson = Files.contentOf(dividendAdjustmentFile, StandardCharsets.UTF_8);

        mvc
                .perform(
                        post("/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createIndexJson))
                .andExpect(status().isCreated())
                .andReturn();

        mvc
                .perform(
                        post("/indexAdjustment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(additionAdjustmentJson))
                .andExpect(status().isCreated())
                .andReturn();

        mvc
                .perform(
                        post("/indexAdjustment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(deletionAdjustmentJson))
                .andExpect(status().isOk())
                .andReturn();

        mvc
                .perform(
                        post("/indexAdjustment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(dividendAdjustmentJson))
                .andExpect(status().isOk())
                .andReturn();

        File expectedResultFile = new File("src/test/resources/responses/indexFullPipeline/expectedResult.json");
        String expectedJson = Files.contentOf(expectedResultFile, StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();

        IndexDetailsDto expectedResult = objectMapper.readValue(expectedJson, IndexDetailsDto.class);

        MvcResult mvcResult = mvc
                .perform(get("/indexState"))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        IndexDetailsDto actualResult = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), IndexDetailsDto.class);

        assert (expectedResult.equals(actualResult));
    }
}
