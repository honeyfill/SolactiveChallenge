package indexcalculator.controller;

import indexcalculator.dto.input.IndexAdjustmentDto;
import indexcalculator.dto.input.IndexCreationDto;
import indexcalculator.exception.IndexRuntimeException;
import indexcalculator.service.IndexService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    private final static Logger logger = LogManager.getLogger(IndexController.class);

    private final IndexService indexService;

    public IndexController(@Autowired IndexService indexService) {
        this.indexService = indexService;
    }

    @PostMapping(path = "/create", consumes = "application/json")
    public ResponseEntity<Void> createIndex(@Valid @RequestBody IndexCreationDto indexCreationDto) {
        indexService.createIndex(indexCreationDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(path = "/indexAdjustment", consumes = "application/json")
    public ResponseEntity<Void> adjustIndex(@RequestBody IndexAdjustmentDto indexAdjustmentDto) {
        if (indexAdjustmentDto.addition() != null) {
            indexService.adjustIndexByAddition(indexAdjustmentDto.addition());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        if (indexAdjustmentDto.deletion() != null) indexService.adjustIndexByDeletion(indexAdjustmentDto.deletion());
        if (indexAdjustmentDto.dividend() != null) indexService.adjustIndexByDividend(indexAdjustmentDto.dividend());
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/indexState", produces = "application/json")
    public ResponseEntity<String> getIndexState() {
        return ResponseEntity.ok(indexService.getIndexState());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private void handleValidationException(MethodArgumentNotValidException exception) {
        logger.error(exception);
    }

    @ExceptionHandler(IndexRuntimeException.class)
    private ResponseEntity<Void> handleServerException(IndexRuntimeException exception) {
        logger.error(exception);
        return ResponseEntity.status(exception.getStatusCode()).build();
    }
}
