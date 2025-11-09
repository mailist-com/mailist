package com.mailist.mailist.automation.domain.strategy;

import com.mailist.mailist.automation.domain.strategy.impl.DefaultStepStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory for selecting the appropriate step parsing strategy based on node type
 */
@Component
public class StepParsingStrategyFactory {

    private final List<StepParsingStrategy> strategies;
    private final DefaultStepStrategy defaultStrategy;

    public StepParsingStrategyFactory(List<StepParsingStrategy> strategies, DefaultStepStrategy defaultStrategy) {
        this.strategies = strategies;
        this.defaultStrategy = defaultStrategy;
    }

    /**
     * Get the appropriate strategy for the given node type
     * Returns DefaultStepStrategy if no specific strategy is found
     */
    public StepParsingStrategy getStrategy(String nodeType) {
        return strategies.stream()
            .filter(strategy -> !(strategy instanceof DefaultStepStrategy)) // Exclude default from initial search
            .filter(strategy -> strategy.canHandle(nodeType))
            .findFirst()
            .orElse(defaultStrategy);
    }
}
