package com.vocably.learning;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vocably.auth.UserPrincipal;
import com.vocably.common.error.ApiErrorResponse;
import com.vocably.learning.dto.LearningAnswerResponse;
import com.vocably.learning.dto.LearningPoolResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/learning")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Learning", description = "Daily practice pool and answer scoring, scoped to the authenticated user")
public class LearningController {

    private final LearningService learningService;

    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    @GetMapping("/words")
    @Operation(
            summary = "Get today's words to practise",
            description = "Returns up to 25 words for one day from a single dictionary of the authenticated user. "
                    + "SEED words dominate, then SPROUT, then BLOOM, shuffled together. The set is stable for the "
                    + "whole day (UTC) and rotates the next day; words already answered correctly today are left out "
                    + "and are not replaced, so the daily limit cannot be exceeded by calling again."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Today's remaining words, possibly none"),
        @ApiResponse(responseCode = "400", description = "dictionaryId is missing or not a UUID (code INVALID_PARAMETER)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "The dictionary does not exist or is not yours (code NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public LearningPoolResponse getDailyPool(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "UUID of the dictionary to practise") @RequestParam UUID dictionaryId
    ) {
        return learningService.getDailyPool(principal.getId(), dictionaryId);
    }

    @PostMapping("/words/{id}/result")
    @Operation(
            summary = "Report the result of one attempt",
            description = "Adds one point to the word on a successful attempt and removes one on a failed attempt "
                    + "(never below zero). The word becomes SPROUT at 100 points and BLOOM at 150, and falls back the "
                    + "same way when points are lost. Only a successful attempt marks the word as practised, so a "
                    + "failed word stays in today's pool."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attempt recorded"),
        @ApiResponse(responseCode = "400", description = "successful is missing or not a boolean (code INVALID_PARAMETER)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Word not found, or not yours (code NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public LearningAnswerResponse submitResult(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "UUID of the word that was practised") @PathVariable UUID id,
            @Parameter(description = "Whether the attempt was correct") @RequestParam boolean successful
    ) {
        return learningService.submitResult(principal.getId(), id, successful);
    }
}
