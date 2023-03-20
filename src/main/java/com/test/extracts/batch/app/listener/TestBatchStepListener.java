package com.test.extracts.batch.app.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;


public interface TestBatchStepListener {

    Logger logger = LoggerFactory.getLogger(TestBatchStepListener.class);


    default ChunkListener chunkListener() {
        return new ChunkListener() {

            @Override
            public void beforeChunk(ChunkContext context) {
                logger.info("beforeChunk  {}", context);

            }

            @Override
            public void afterChunk(ChunkContext context) {
                logger.info("afterChunk  {}", context);

            }

            @Override
            public void afterChunkError(ChunkContext context) {
                logger.error("afterChunkError {}", context);
            }
        };
    }
}
