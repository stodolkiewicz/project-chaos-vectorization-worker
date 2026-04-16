package com.stodo.projectchaos.embedding;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppTokenTextSplitter {

    public List<Document> split(List<Document> documents) {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(200)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(1000)
                .withKeepSeparator(true)
                .build();

        return splitter.apply(documents);
    }
}
