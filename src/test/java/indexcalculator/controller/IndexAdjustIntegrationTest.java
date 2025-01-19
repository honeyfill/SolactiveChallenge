package indexcalculator.controller;

import indexcalculator.Application;
import indexcalculator.dto.input.IndexCreationDto;
import indexcalculator.dto.input.ShareCreationDto;
import indexcalculator.service.IndexService;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class IndexAdjustIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private IndexService indexService;


    @Test
    public void additionAdjustingExistingShare() throws Exception {

        ShareCreationDto shareCreationDtoFirst = new ShareCreationDto("SHARE_ONE", BigDecimal.TEN, BigDecimal.TEN);
        ShareCreationDto shareCreationDtoSecond = new ShareCreationDto("SHARE_TWO", BigDecimal.TEN, BigDecimal.TEN);
        IndexCreationDto indexCreationDto = new IndexCreationDto(
                "INDEX_FOR_ADD_TEST", List.of(shareCreationDtoSecond, shareCreationDtoFirst)
        );
        indexService.createIndex(indexCreationDto);

        File file = new File("src/test/resources/requests/additionAdjustingExistingShareThen202.json");
        String additionJson = Files.contentOf(file, StandardCharsets.UTF_8);
        mvc
                .perform(
                        post("/indexAdjustment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(additionJson))
                .andExpect(status().isAccepted()).andReturn();
    }
}
