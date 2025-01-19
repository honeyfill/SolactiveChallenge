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
public class IndexCreateIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private IndexService indexService;

    @Test
    public void creationExistingIndexThen409() throws Exception {

        ShareCreationDto shareCreationDtoFirst = new ShareCreationDto("SHARE_1", BigDecimal.TEN, BigDecimal.TEN);
        ShareCreationDto shareCreationDtoSecond = new ShareCreationDto("SHARE_2", BigDecimal.TEN, BigDecimal.TEN);
        IndexCreationDto indexCreationDto = new IndexCreationDto("EXISTING_INDEX", List.of(shareCreationDtoSecond, shareCreationDtoFirst));
        indexService.createIndex(indexCreationDto);

        File file = new File("src/test/resources/requests/creationExistingIndexThen409.json");
        String indexJson = Files.contentOf(file, StandardCharsets.UTF_8);
        mvc
                .perform(
                        post("/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(indexJson))
                .andExpect(status().isConflict()).andReturn();
    }

    @Test
    public void creationIndexThen201() throws Exception {
        File file = new File("src/test/resources/requests/creationIndexThen201.json");
        String indexJson = Files.contentOf(file, StandardCharsets.UTF_8);
        mvc
                .perform(
                        post("/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(indexJson)
                )
                .andExpect(status().isCreated());
    }
}
