package com.vibefolio.service;

import com.vibefolio.persistence.repo.GenerationRepo;
import org.springframework.stereotype.Service;

/**
 * Records and queries AI generation audit events.
 * Provides data for rate limiting and cost accounting.
 *
 * <p>Implementation stub — logic to be filled in M1 AI pipeline phase.
 */
@Service
public class GenerationService {

    private final GenerationRepo generationRepo;

    public GenerationService(GenerationRepo generationRepo) {
        this.generationRepo = generationRepo;
    }
}
